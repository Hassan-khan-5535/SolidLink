package com.hyperdrop.app.domain.repository

import com.hyperdrop.app.domain.model.ConnectionEvent
import com.hyperdrop.app.domain.model.Peer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface NearbyTransportRepository {
    fun startAdvertising(userName: String): Flow<Boolean>
    fun stopAdvertising()
    
    fun startDiscovery(): Flow<List<Peer>>
    fun stopDiscovery()

    /** Shared stream of connection lifecycle events for all endpoints. */
    val connectionEvents: SharedFlow<ConnectionEvent>

    /** Request a connection to a discovered endpoint. */
    fun requestConnection(endpointId: String, userName: String): Flow<Boolean>

    /** Accept an initiated connection. */
    fun acceptConnection(endpointId: String)

    /** Reject an initiated connection. */
    fun rejectConnection(endpointId: String)

    /** Disconnect from a connected endpoint. */
    fun disconnect(endpointId: String)
}
