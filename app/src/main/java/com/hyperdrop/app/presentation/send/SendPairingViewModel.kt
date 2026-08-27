package com.hyperdrop.app.presentation.send

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hyperdrop.app.data.transport.nearby.FriendlyNameGenerator
import com.hyperdrop.app.domain.model.ConnectionEvent
import com.hyperdrop.app.domain.model.PairingPayload
import com.hyperdrop.app.domain.repository.NearbyTransportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the sender side of the pairing flow.
 * Generates a pairing payload, starts advertising, and handles
 * incoming connection requests with explicit accept/reject.
 */
@HiltViewModel
class SendPairingViewModel @Inject constructor(
    private val repository: NearbyTransportRepository
) : ViewModel() {

    private val tag = "SendPairingVM"

    private val _state = MutableStateFlow<PairingState>(PairingState.GeneratingPayload)
    val state: StateFlow<PairingState> = _state.asStateFlow()

    private var currentPayload: PairingPayload? = null
    private var pendingEndpointId: String? = null

    init {
        startPairing()
        observeConnectionEvents()
    }

    private fun startPairing() {
        viewModelScope.launch {
            try {
                val deviceName = FriendlyNameGenerator.generateName()
                // We don't have a real endpointId for ourselves before advertising starts.
                // Use the sessionId as a correlation identifier that the receiver
                // will match against discovered endpoints.
                val payload = PairingPayload(
                    deviceName = deviceName,
                    endpointId = "" // populated after advertising; receiver matches via discovery
                )
                currentPayload = payload

                // Start advertising and wait for it to succeed
                val success = repository.startAdvertising(deviceName)
                    .catch { e ->
                        Log.e(tag, "Advertising flow error", e)
                        _state.value = PairingState.Error("Failed to start advertising: ${e.message}")
                    }
                    .first()

                if (success) {
                    _state.value = PairingState.ShowingQrAndCode(payload)
                    Log.d(tag, "Advertising started, showing QR. Code: ${payload.numericCode}")
                } else {
                    _state.value = PairingState.Error("Failed to start advertising")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error starting pairing", e)
                _state.value = PairingState.Error("Error: ${e.message}")
            }
        }
    }

    private fun observeConnectionEvents() {
        viewModelScope.launch {
            repository.connectionEvents.collect { event ->
                when (event) {
                    is ConnectionEvent.ConnectionInitiated -> {
                        if (event.info.isIncomingConnection) {
                            Log.d(tag, "Incoming connection from ${event.info.endpointName}")
                            pendingEndpointId = event.endpointId
                            _state.value = PairingState.PeerConnected(
                                peerName = event.info.endpointName,
                                endpointId = event.endpointId,
                                authenticationDigits = event.info.authenticationDigits
                            )
                        }
                    }
                    is ConnectionEvent.ConnectionResult -> {
                        if (event.isSuccess) {
                            val currentState = _state.value
                            val peerName = when (currentState) {
                                is PairingState.WaitingForPeerApproval -> currentState.peerName
                                is PairingState.PeerConnected -> currentState.peerName
                                else -> "Unknown"
                            }
                            Log.d(tag, "Pairing complete with $peerName")
                            _state.value = PairingState.PairingComplete(
                                peerName = peerName,
                                endpointId = event.endpointId
                            )
                        } else {
                            Log.d(tag, "Pairing rejected for ${event.endpointId}")
                            _state.value = PairingState.PairingRejected
                        }
                    }
                    is ConnectionEvent.Disconnected -> {
                        Log.d(tag, "Peer disconnected: ${event.endpointId}")
                        // Only revert if we're still in a connected state with this endpoint
                        val currentState = _state.value
                        if (currentState is PairingState.PairingComplete &&
                            currentState.endpointId == event.endpointId
                        ) {
                            currentPayload?.let {
                                _state.value = PairingState.ShowingQrAndCode(it)
                            }
                        }
                    }
                }
            }
        }
    }

    /** Accept the incoming pairing request. No bytes move until this is called. */
    fun acceptPairing() {
        val endpointId = pendingEndpointId ?: return
        val currentState = _state.value
        if (currentState is PairingState.PeerConnected) {
            repository.acceptConnection(endpointId)
            _state.value = PairingState.WaitingForPeerApproval(
                peerName = currentState.peerName,
                endpointId = endpointId
            )
            Log.d(tag, "Accepted pairing from $endpointId, waiting for peer approval")
        }
    }

    /** Reject the incoming pairing request. */
    fun rejectPairing() {
        val endpointId = pendingEndpointId ?: return
        repository.rejectConnection(endpointId)
        pendingEndpointId = null
        // Go back to showing QR
        currentPayload?.let {
            _state.value = PairingState.ShowingQrAndCode(it)
        }
        Log.d(tag, "Rejected pairing from $endpointId")
    }

    /** Retry after error — restart advertising. */
    fun retry() {
        _state.value = PairingState.GeneratingPayload
        startPairing()
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopAdvertising()
        pendingEndpointId?.let { repository.disconnect(it) }
        Log.d(tag, "ViewModel cleared, stopped advertising")
    }
}
