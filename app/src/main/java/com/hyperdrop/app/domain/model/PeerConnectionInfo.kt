package com.hyperdrop.app.domain.model

/**
 * Information about a peer during the connection initiation phase.
 * Maps to data from Nearby Connections' ConnectionInfo.
 */
data class PeerConnectionInfo(
    val endpointName: String,
    val authenticationDigits: String,
    val isIncomingConnection: Boolean
)
