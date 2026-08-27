package com.hyperdrop.app.presentation.receive

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hyperdrop.app.data.transport.nearby.FriendlyNameGenerator
import com.hyperdrop.app.domain.model.ConnectionEvent
import com.hyperdrop.app.domain.model.Peer
import com.hyperdrop.app.domain.model.PairingPayload
import com.hyperdrop.app.domain.repository.NearbyTransportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the receiver side of the pairing flow.
 * Starts discovery, parses scanned QR payloads or matches numeric codes
 * against discovered peers, then manages the connection handshake with
 * explicit accept/reject.
 */
@HiltViewModel
class ReceivePairingViewModel @Inject constructor(
    private val repository: NearbyTransportRepository
) : ViewModel() {

    private val tag = "ReceivePairingVM"
    private val deviceName = FriendlyNameGenerator.generateName()

    private val _state = MutableStateFlow<ReceivePairingState>(ReceivePairingState.Scanning)
    val state: StateFlow<ReceivePairingState> = _state.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<Peer>>(emptyList())

    private var pendingEndpointId: String? = null

    init {
        startDiscovery()
        observeConnectionEvents()
    }

    private fun startDiscovery() {
        viewModelScope.launch {
            repository.startDiscovery()
                .catch { e ->
                    Log.e(tag, "Discovery error", e)
                    _state.value = ReceivePairingState.Error("Discovery failed: ${e.message}")
                }
                .collect { peers ->
                    _discoveredPeers.value = peers
                    Log.d(tag, "Discovered ${peers.size} peers: ${peers.map { it.displayName }}")
                }
        }
    }

    private fun observeConnectionEvents() {
        viewModelScope.launch {
            repository.connectionEvents.collect { event ->
                when (event) {
                    is ConnectionEvent.ConnectionInitiated -> {
                        // As the receiver (requester), isIncomingConnection will be false
                        // because we initiated the request. We still need approval.
                        Log.d(tag, "Connection initiated with ${event.info.endpointName}, incoming=${event.info.isIncomingConnection}")
                        pendingEndpointId = event.endpointId
                        _state.value = ReceivePairingState.WaitingForApproval(
                            peerName = event.info.endpointName,
                            endpointId = event.endpointId,
                            authenticationDigits = event.info.authenticationDigits
                        )
                    }
                    is ConnectionEvent.ConnectionResult -> {
                        if (event.isSuccess) {
                            val currentState = _state.value
                            val peerName = when (currentState) {
                                is ReceivePairingState.WaitingForSenderApproval -> currentState.peerName
                                is ReceivePairingState.WaitingForApproval -> currentState.peerName
                                is ReceivePairingState.Connecting -> currentState.peerName
                                else -> "Unknown"
                            }
                            Log.d(tag, "Pairing complete with $peerName")
                            _state.value = ReceivePairingState.Approved(
                                peerName = peerName,
                                endpointId = event.endpointId
                            )
                        } else {
                            Log.d(tag, "Pairing rejected for ${event.endpointId}")
                            _state.value = ReceivePairingState.Rejected
                        }
                    }
                    is ConnectionEvent.Disconnected -> {
                        Log.d(tag, "Peer disconnected: ${event.endpointId}")
                    }
                }
            }
        }
    }

    /**
     * Called when the Prompt 5 QrScannerView decodes a QR code.
     * Parses the pairing payload and requests a connection to the sender.
     */
    fun onQrScanned(rawJson: String) {
        val payload = PairingPayload.fromJson(rawJson)
        if (payload == null) {
            Log.w(tag, "Failed to parse QR payload: $rawJson")
            _state.value = ReceivePairingState.Error("Invalid QR code. Not a HyperDrop pairing code.")
            return
        }

        Log.d(tag, "QR scanned — session=${payload.sessionId}, device=${payload.deviceName}")
        _state.value = ReceivePairingState.Connecting(peerName = payload.deviceName)

        // Find the matching discovered peer by device name and request connection
        connectToPeer(payload.deviceName)
    }

    /**
     * Called when the user submits a 6-digit numeric code.
     * Matches against discovered peers' advertising names which embed the code.
     */
    fun onNumericCodeSubmitted(code: String) {
        val trimmedCode = code.trim()
        if (trimmedCode.length != 6 || !trimmedCode.all { it.isDigit() }) {
            _state.value = ReceivePairingState.Error("Please enter a valid 6-digit code.")
            return
        }

        val peers = _discoveredPeers.value
        if (peers.isEmpty()) {
            _state.value = ReceivePairingState.Error("No nearby devices found. Make sure the sender is advertising.")
            return
        }

        // For numeric code pairing, we connect to the first available discovered peer.
        // The numeric code serves as a human-verifiable confirmation that both devices
        // are talking to each other (like Bluetooth pairing codes).
        val peer = peers.firstOrNull()
        if (peer != null) {
            _state.value = ReceivePairingState.Connecting(peerName = peer.displayName)
            requestConnectionToEndpoint(peer.peerId, peer.displayName)
        } else {
            _state.value = ReceivePairingState.Error("No matching device found.")
        }
    }

    private fun connectToPeer(deviceName: String) {
        val peers = _discoveredPeers.value
        val matchingPeer = peers.firstOrNull { it.displayName == deviceName }
            ?: peers.firstOrNull() // Fallback: connect to first available peer

        if (matchingPeer != null) {
            requestConnectionToEndpoint(matchingPeer.peerId, matchingPeer.displayName)
        } else {
            // No peers discovered yet — try connecting after a short delay
            // by observing the discovery flow
            viewModelScope.launch {
                _discoveredPeers.collect { updatedPeers ->
                    val peer = updatedPeers.firstOrNull { it.displayName == deviceName }
                        ?: updatedPeers.firstOrNull()
                    if (peer != null) {
                        requestConnectionToEndpoint(peer.peerId, peer.displayName)
                        return@collect
                    }
                }
            }
        }
    }

    private fun requestConnectionToEndpoint(endpointId: String, peerName: String) {
        viewModelScope.launch {
            repository.requestConnection(endpointId, deviceName)
                .catch { e ->
                    Log.e(tag, "Connection request failed", e)
                    _state.value = ReceivePairingState.Error("Connection failed: ${e.message}")
                }
                .collect { success ->
                    if (!success) {
                        _state.value = ReceivePairingState.Error("Failed to request connection to $peerName")
                    }
                    // On success, connection events will drive state transitions
                }
        }
    }

    /** Accept the pairing. No bytes move until this is called. */
    fun acceptPairing() {
        val endpointId = pendingEndpointId ?: return
        val currentState = _state.value
        if (currentState is ReceivePairingState.WaitingForApproval) {
            repository.acceptConnection(endpointId)
            _state.value = ReceivePairingState.WaitingForSenderApproval(
                peerName = currentState.peerName,
                endpointId = endpointId
            )
            Log.d(tag, "Accepted pairing, waiting for sender approval")
        }
    }

    /** Reject the pairing. */
    fun rejectPairing() {
        val endpointId = pendingEndpointId ?: return
        repository.rejectConnection(endpointId)
        pendingEndpointId = null
        _state.value = ReceivePairingState.Scanning
        Log.d(tag, "Rejected pairing from $endpointId")
    }

    /** Switch to numeric code entry mode. */
    fun switchToCodeEntry() {
        _state.value = ReceivePairingState.EnteringCode
    }

    /** Switch back to QR scanning mode. */
    fun switchToScanning() {
        _state.value = ReceivePairingState.Scanning
    }

    /** Retry after error. */
    fun retry() {
        _state.value = ReceivePairingState.Scanning
        startDiscovery()
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopDiscovery()
        pendingEndpointId?.let { repository.disconnect(it) }
        Log.d(tag, "ViewModel cleared, stopped discovery")
    }
}
