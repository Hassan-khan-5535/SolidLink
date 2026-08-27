package com.hyperdrop.app.presentation.receive

/**
 * UI state for the receiver pairing screen.
 */
sealed class ReceivePairingState {
    /** Camera scanner is active, waiting for QR scan. */
    data object Scanning : ReceivePairingState()

    /** User switched to numeric code entry mode. */
    data object EnteringCode : ReceivePairingState()

    /** QR scanned or code entered — connecting to sender. */
    data class Connecting(val peerName: String) : ReceivePairingState()

    /** Connection initiated — show approval screen with Accept/Reject. */
    data class WaitingForApproval(
        val peerName: String,
        val endpointId: String,
        val authenticationDigits: String
    ) : ReceivePairingState()

    /** User accepted — waiting for sender to accept too. */
    data class WaitingForSenderApproval(
        val peerName: String,
        val endpointId: String
    ) : ReceivePairingState()

    /** Both sides accepted — pairing complete. */
    data class Approved(
        val peerName: String,
        val endpointId: String
    ) : ReceivePairingState()

    /** Pairing was rejected by either side. */
    data object Rejected : ReceivePairingState()

    /** An error occurred. */
    data class Error(val message: String) : ReceivePairingState()
}
