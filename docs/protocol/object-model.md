# HyperDrop Object Model

This document defines the core Kotlin data-class signatures (fields + types only) that will be used across the application. These represent the locked protocol object model.

```kotlin
data class TransferSession(
    val sessionId: String,
    val peerId: String,
    val role: Role, // e.g., SENDER or RECEIVER
    val status: SessionStatus,
    val startTimeMs: Long,
    val endTimeMs: Long?,
    val bytesTransferred: Long,
    val totalBytes: Long,
    val securitySession: SecuritySession
)

data class Peer(
    val peerId: String,
    val displayName: String,
    val connectionType: ConnectionType, // e.g., NEARBY_CONNECTIONS or WIFI_BRIDGE
    val lastSeenMs: Long,
    val isTrusted: Boolean
)

data class FileManifest(
    val fileId: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val expectedHash: String, // e.g., SHA-256 for integrity verification
    val chunkManifests: List<ChunkManifest>
)

data class ChunkManifest(
    val chunkId: String,
    val fileId: String,
    val offset: Long,
    val sizeBytes: Long,
    val expectedHash: String
)

data class ChunkState(
    val chunkId: String,
    val status: ChunkTransferStatus, // e.g., PENDING, IN_PROGRESS, COMPLETED, FAILED
    val bytesTransferred: Long,
    val retryCount: Int
)

data class TransferCheckpoint(
    val checkpointId: String,
    val sessionId: String,
    val fileId: String,
    val completedChunks: List<String>, // List of completed chunk IDs
    val timestampMs: Long
)

data class ConnectionSnapshot(
    val peerId: String,
    val activeMedium: ConnectionMedium, // e.g., WIFI, BLE, HOTSPOT
    val signalStrength: Int?,
    val isConnected: Boolean,
    val lastStateChangeMs: Long
)

data class VerificationResult(
    val fileId: String,
    val isVerified: Boolean,
    val actualHash: String?,
    val failureReason: VerificationFailureReason?
)

data class SecuritySession(
    val sessionId: String,
    val ephemeralKeyId: String, // Android Keystore alias reference
    val authCode: String, // OOB authentication code (e.g., from QR)
    val isSecured: Boolean,
    val establishedMs: Long
)

data class TransferPolicy(
    val requireExplicitApproval: Boolean,
    val autoResumeInterrupted: Boolean,
    val maxConcurrentTransfers: Int,
    val requireWifiForLargeFiles: Boolean,
    val maxFileSizeBytes: Long
)
```
