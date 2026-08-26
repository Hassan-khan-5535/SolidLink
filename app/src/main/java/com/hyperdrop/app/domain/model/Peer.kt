package com.hyperdrop.app.domain.model

enum class ConnectionType {
    NEARBY_CONNECTIONS,
    WIFI_BRIDGE
}

data class Peer(
    val peerId: String,
    val displayName: String,
    val connectionType: ConnectionType,
    val lastSeenMs: Long,
    val isTrusted: Boolean
)
