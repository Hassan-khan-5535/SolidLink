package com.hyperdrop.app.presentation.send

import com.hyperdrop.app.domain.model.PairingPayload

/**
 * UI state for the sender pairing screen.
 */
sealed class PairingState {
    /** Generating pairing payload and starting advertising. */
    data object GeneratingPayload : PairingState()

    /** QR code and numeric code are displayed, waiting for receiver to scan/enter. */
    data class ShowingQrAndCode(val payload: PairingPayload) : PairingState()

    /** A peer has initiated a connection — show approval screen. */
    data class PeerConnected(
        val peerName: String,
        val endpointId: String,
        val authenticationDigits: String
    ) : PairingState()

    /** User accepted — waiting for the other side to accept too. */
    data class WaitingForPeerApproval(
        val peerName: String,
        val endpointId: String
    ) : PairingState()

    /** Both sides accepted — pairing complete. */
    data class PairingComplete(
        val peerName: String,
        val endpointId: String
    ) : PairingState()

    /** Pairing was rejected by either side. */
    data object PairingRejected : PairingState()

    /** An error occurred. */
    data class Error(val message: String) : PairingState()
}
