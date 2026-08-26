# HyperDrop Threat Model

This document outlines the formal threat model for HyperDrop and lists the specific mitigations in place to protect the privacy and integrity of the peer-to-peer file transfer system.

## 1. Malicious Peer
**Description:** A connected peer attempts to exploit vulnerabilities in the app, execute arbitrary code, or send harmful content during an active connection.
**Mitigation:** 
- Explicit connection approval and zero silent receiving.
- Strict schema validation of incoming payloads (e.g., `FileManifest` and `ChunkManifest`).
- Incoming files are written directly to sandboxed temporary storage before validation, never fully loaded into memory.

## 2. Rogue Pairing Attempt
**Description:** An unauthorized actor attempts to pair with a device while it is actively discovering or advertising, hoping to establish an unsolicited connection.
**Mitigation:**
- Discovery and pairing require explicit user action (e.g., scanning a QR code). 
- The QR code contains an Out-Of-Band (OOB) authentication token to verify physical proximity and intent.
- Unsolicited connection requests are rejected unless explicitly approved by the user.

## 3. Corrupted Chunk Injection
**Description:** An attacker intercepts or alters file chunks in transit, attempting to corrupt the file or inject malicious payloads.
**Mitigation:**
- All communication goes over encrypted transport (Nearby Connections' encrypted channel or HTTPS for the bridge).
- Cryptographic verification (e.g., SHA-256) is performed per chunk (`ChunkManifest.expectedHash`) and per file (`FileManifest.expectedHash`) before marking the transfer as complete.

## 4. Storage Exhaustion
**Description:** A malicious sender continuously streams infinite data or declares massively oversized files to exhaust the receiver's disk space.
**Mitigation:**
- Pre-flight validation of the `FileManifest` compares `sizeBytes` against available storage.
- Continuous monitoring of available disk space during the transfer, actively pausing or canceling the stream if storage thresholds are breached.

## 5. Replay of an Old Session
**Description:** An attacker captures network traffic from a previous successful transfer and attempts to replay it to re-establish a session or re-send files.
**Mitigation:**
- Use of session-specific ephemeral keys and cryptographically secure nonces for each connection.
- Timestamped payloads and strict state machine transitions invalidate older packets.

## 6. Malicious Actor on the Same Wi-Fi LAN (Cross-Platform Bridge)
**Description:** While using the local HTTP web bridge for cross-platform transfers (e.g., Android <-> iOS), another user on the same Wi-Fi network attempts to guess the local IP/URL, hijack the transfer, or download the hosted file.
**Mitigation:**
- The bridge uses unpredictable, cryptographically random URL paths and tokens (embedded in the QR code).
- The HTTP server mandates explicit one-time authorization using the token provided in the URL.
- The server refuses connections from unrecognized IPs once the initial legitimate peer is connected.
