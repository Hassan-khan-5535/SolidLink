package com.hyperdrop.app.domain.usecase

import com.hyperdrop.app.domain.model.Peer
import com.hyperdrop.app.domain.repository.NearbyTransportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DiscoverPeers @Inject constructor(
    private val repository: NearbyTransportRepository
) {
    operator fun invoke(): Flow<List<Peer>> {
        return repository.startDiscovery()
    }
}
