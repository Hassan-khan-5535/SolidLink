package com.hyperdrop.app.data.transport.nearby

import android.util.Log
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.hyperdrop.app.domain.model.ConnectionEvent
import com.hyperdrop.app.domain.model.ConnectionType
import com.hyperdrop.app.domain.model.Peer
import com.hyperdrop.app.domain.model.PeerConnectionInfo
import com.hyperdrop.app.domain.repository.NearbyTransportRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class NearbyTransportRepositoryImpl @Inject constructor(
    private val connectionsClient: ConnectionsClient
) : NearbyTransportRepository {

    private val strategy = Strategy.P2P_STAR
    private val serviceId = "com.hyperdrop.app.SERVICE_ID"
    private val tag = "NearbyTransport"

    private val _connectionEvents = MutableSharedFlow<ConnectionEvent>(
        replay = 1,
        extraBufferCapacity = 10
    )
    override val connectionEvents: SharedFlow<ConnectionEvent> = _connectionEvents.asSharedFlow()

    /**
     * Shared connection lifecycle callback used by both advertising and
     * connection-requesting sides. Emits events into [_connectionEvents].
     */
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(tag, "Connection initiated by $endpointId (${info.endpointName}), incoming=${info.isIncomingConnection}")
            _connectionEvents.tryEmit(
                ConnectionEvent.ConnectionInitiated(
                    endpointId = endpointId,
                    info = PeerConnectionInfo(
                        endpointName = info.endpointName,
                        authenticationDigits = info.authenticationDigits,
                        isIncomingConnection = info.isIncomingConnection
                    )
                )
            )
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            val isSuccess = result.status.statusCode == ConnectionsStatusCodes.STATUS_OK
            Log.d(tag, "Connection result for $endpointId: success=$isSuccess (status=${result.status})")
            _connectionEvents.tryEmit(
                ConnectionEvent.ConnectionResult(
                    endpointId = endpointId,
                    isSuccess = isSuccess
                )
            )
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(tag, "Disconnected from $endpointId")
            _connectionEvents.tryEmit(
                ConnectionEvent.Disconnected(endpointId = endpointId)
            )
        }
    }

    override fun startAdvertising(userName: String): Flow<Boolean> = callbackFlow {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()

        connectionsClient.startAdvertising(
            userName,
            serviceId,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            Log.d(tag, "Started advertising as $userName")
            trySend(true)
        }.addOnFailureListener { e ->
            Log.e(tag, "Failed to start advertising", e)
            trySend(false)
            close(e)
        }

        awaitClose {
            connectionsClient.stopAdvertising()
            Log.d(tag, "Stopped advertising")
        }
    }

    override fun stopAdvertising() {
        connectionsClient.stopAdvertising()
        Log.d(tag, "Stopped advertising explicitly")
    }

    override fun startDiscovery(): Flow<List<Peer>> = callbackFlow {
        val discoveredPeers = mutableMapOf<String, Peer>()
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()

        val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                Log.d(tag, "Endpoint found: $endpointId (${info.endpointName})")
                val peer = Peer(
                    peerId = endpointId,
                    displayName = info.endpointName,
                    connectionType = ConnectionType.NEARBY_CONNECTIONS,
                    lastSeenMs = System.currentTimeMillis(),
                    isTrusted = false
                )
                discoveredPeers[endpointId] = peer
                trySend(discoveredPeers.values.toList())
            }

            override fun onEndpointLost(endpointId: String) {
                Log.d(tag, "Endpoint lost: $endpointId")
                discoveredPeers.remove(endpointId)
                trySend(discoveredPeers.values.toList())
            }
        }

        connectionsClient.startDiscovery(
            serviceId,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener {
            Log.d(tag, "Started discovery")
            if (discoveredPeers.isEmpty()) {
                trySend(emptyList())
            }
        }.addOnFailureListener { e ->
            Log.e(tag, "Failed to start discovery", e)
            close(e)
        }

        awaitClose {
            connectionsClient.stopDiscovery()
            Log.d(tag, "Stopped discovery")
        }
    }

    override fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        Log.d(tag, "Stopped discovery explicitly")
    }

    override fun requestConnection(endpointId: String, userName: String): Flow<Boolean> = callbackFlow {
        connectionsClient.requestConnection(
            userName,
            endpointId,
            connectionLifecycleCallback
        ).addOnSuccessListener {
            Log.d(tag, "Connection requested to $endpointId as $userName")
            trySend(true)
        }.addOnFailureListener { e ->
            Log.e(tag, "Failed to request connection to $endpointId", e)
            trySend(false)
            close(e)
        }

        awaitClose {
            Log.d(tag, "Request connection flow closed for $endpointId")
        }
    }

    /**
     * No-op payload callback for the pairing phase.
     * Actual payload handling will be implemented in Prompt 7-8 (file transfer).
     */
    private val noOpPayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.d(tag, "Payload received from $endpointId (type=${payload.type})")
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            Log.d(tag, "Payload transfer update from $endpointId: status=${update.status}")
        }
    }

    override fun acceptConnection(endpointId: String) {
        connectionsClient.acceptConnection(endpointId, noOpPayloadCallback)
            .addOnSuccessListener {
                Log.d(tag, "Accepted connection from $endpointId")
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Failed to accept connection from $endpointId", e)
            }
    }

    override fun rejectConnection(endpointId: String) {
        connectionsClient.rejectConnection(endpointId)
            .addOnSuccessListener {
                Log.d(tag, "Rejected connection from $endpointId")
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Failed to reject connection from $endpointId", e)
            }
    }

    override fun disconnect(endpointId: String) {
        connectionsClient.disconnectFromEndpoint(endpointId)
        Log.d(tag, "Disconnected from $endpointId")
    }
}
