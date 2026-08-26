package com.hyperdrop.app.data.transport.nearby

import android.util.Log
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Strategy
import com.hyperdrop.app.domain.model.ConnectionType
import com.hyperdrop.app.domain.model.Peer
import com.hyperdrop.app.domain.repository.NearbyTransportRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class NearbyTransportRepositoryImpl @Inject constructor(
    private val connectionsClient: ConnectionsClient
) : NearbyTransportRepository {

    private val strategy = Strategy.P2P_STAR
    private val serviceId = "com.hyperdrop.app.SERVICE_ID"
    private val tag = "NearbyTransport"

    override fun startAdvertising(userName: String): Flow<Boolean> = callbackFlow {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()

        val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
                Log.d(tag, "Connection initiated by $endpointId")
                // Handled in Phase 2
            }
            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                Log.d(tag, "Connection result for $endpointId")
            }
            override fun onDisconnected(endpointId: String) {
                Log.d(tag, "Disconnected from $endpointId")
            }
        }

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
            // Emit empty list initially upon success to indicate discovery is active but no peers yet
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
}
