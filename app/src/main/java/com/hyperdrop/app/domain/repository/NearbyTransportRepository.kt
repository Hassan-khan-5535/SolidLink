package com.hyperdrop.app.domain.repository

import com.hyperdrop.app.domain.model.Peer
import kotlinx.coroutines.flow.Flow

interface NearbyTransportRepository {
    fun startAdvertising(userName: String): Flow<Boolean>
    fun stopAdvertising()
    
    fun startDiscovery(): Flow<List<Peer>>
    fun stopDiscovery()
}
