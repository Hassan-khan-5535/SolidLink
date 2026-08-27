package com.hyperdrop.app.domain.model

/**
 * Sealed class representing connection lifecycle events from Nearby Connections.
 * Observed by ViewModels to drive pairing UI state transitions.
 */
sealed class ConnectionEvent {
    data class ConnectionInitiated(
        val endpointId: String,
        val info: PeerConnectionInfo
    ) : ConnectionEvent()

    data class ConnectionResult(
        val endpointId: String,
        val isSuccess: Boolean
    ) : ConnectionEvent()

    data class Disconnected(
        val endpointId: String
    ) : ConnectionEvent()
}
