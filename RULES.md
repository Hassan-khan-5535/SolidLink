# HYPERDROP — PROJECT RULES (DO NOT VIOLATE, DO NOT REINTERPRET)

## 1. Identity
Project: HyperDrop — Android-first, offline-first, privacy-first P2P file transfer
app, now with a cross-platform Android<->iOS transfer path. Core promise:
transfer files between nearby devices at the best practical speed, without ads,
without mandatory cloud upload, with cryptographic verification.

## 2. Locked Technology Stack (Android app)
- Language: Kotlin only. UI: Jetpack Compose only.
- Architecture: MVVM + Clean Architecture (presentation / domain / data layers).
- Async: Kotlin Coroutines + Flow. DI: Hilt.
- Local Android<->Android transport: Nearby Connections API (ConnectionsClient),
  using STREAM or FILE payload types for file content — never BYTES for file
  content (BYTES has a documented ~1,047,552 byte cap; verify current limit).
- File selection: Photo Picker (images/video), Storage Access Framework (docs/
  directories). MANAGE_EXTERNAL_STORAGE is not the default.
- Persistence: Room. Crypto: platform primitives only (Android Keystore /
  Jetpack Security). No hand-written cryptographic algorithms, ever.
- QR CODE GENERATION: ZXing core library (com.google.zxing:core), using
  QRCodeWriter to encode a pairing/session payload into a BitMatrix, rendered
  to a Bitmap for display. Do not hand-roll QR encoding.
- QR CODE SCANNING: CameraX (androidx.camera) for the live camera preview and
  frame stream, combined with ML Kit's Barcode Scanning API
  (com.google.mlkit:barcode-scanning, or the Play-Services-backed
  play-services-mlkit-barcode-scanning variant) for decoding, using CameraX's
  ImageAnalysis + MlKitAnalyzer to bridge the two. Do not hand-roll barcode
  decoding. Build this once as a single reusable scanner component — every
  later phase that needs "scan a QR" (pairing, cross-platform bridge) calls
  that component rather than re-implementing scanning.

## 3. Cross-Platform (Android <-> iOS) Policy — READ BEFORE PHASE 8
Google states Nearby Connections is available on iOS and can talk to Android,
but independent reports (including discussion on the public google/nearby
project) describe iOS as currently supporting far fewer discovery/connection
"mediums" than Android — historically closer to requiring both devices on the
same Wi-Fi LAN, with offline BLE-only discovery to iOS still limited. Because
this is inconsistent across sources and changes over time, HyperDrop's
canonical Android<->iOS path is NOT "use Nearby Connections on both platforms"
by default. Instead:
- CANONICAL PATH: a local Wi-Fi web bridge. The Android device (or either
  device, sender or receiver) runs a small local HTTP server; the other device
  connects over the same Wi-Fi network via a QR-coded URL opened in its
  browser (Safari on iOS) to upload/download the file. No native iOS app
  required. No internet/cloud relay involved — traffic stays on the local
  network.
- STRETCH / EXPERIMENTAL PATH (optional, separate prompt group, never a
  dependency for the MVP or for the competition demo): a native Swift/
  SwiftUI iOS companion app using Nearby Connections' iOS SDK or
  MultipeerConnectivity, built and tested separately in Xcode (Antigravity/
  Manus targets Android; it will not build or run Swift/Xcode projects).
- Whichever path is used, re-verify current Nearby Connections iOS medium
  support against Google's official docs before assuming BLE-only (no shared
  Wi-Fi) transfer to iOS is reliable — do not assume the situation described
  above still holds without checking.

## 4. Non-Negotiable Engineering Rules
1. Never invent an Android/Google/Apple API. If uncertain, stop and ask.
2. Never claim a throughput number without measuring it on-device.
3. Never implement custom cryptography.
4. Never require cloud infrastructure for the core local transfer path,
   Android<->Android or Android<->iOS.
5. Never load multi-gigabyte files fully into memory — stream everything.
6. Never mark a transfer complete before integrity verification passes.
7. Never silently accept an inbound transfer — explicit approval only.
8. Never request broad storage access when a system picker/scoped storage works.
9. Peer-assisted multi-hop distribution (Phase 7) is an advanced research
   feature, never a dependency for the MVP.
10. The native iOS companion app (if attempted) is never a dependency for the
    MVP or the competition demo — the web-bridge path must work standalone.
11. Every major subsystem needs automated tests and at least one failure-path
    test.

## 5. Platform Reality Check — Android Nearby Connections Radios
As of mid-2026, Google has announced Nearby Connections will stop
automatically toggling Wi-Fi/Bluetooth radios on for apps, effective late
2026. The Android<->Android pairing/discovery flow (Phase 2) must include an
explicit, user-facing step to enable required radios rather than assuming the
API does it silently. Verify current behavior before finalizing that flow.

## 6. Scope Discipline
- Work ONLY on the single prompt's task, not the whole phase.
- Do not pre-build later prompts/phases, even partially.
- Do not restructure existing folders without being asked.
- If a prompt conflicts with something already implemented, flag it and ask.

## 7. Definition of Done (every prompt)
- Compiles/builds cleanly.
- That prompt's own acceptance checklist is fully met.
- Tests exist where the prompt calls for them.
- No permission, dependency, or API outside RULES.md/ARCHITECTURE.md was
  introduced without being flagged first.

## 8. Communication Contract
- Ask ONE clarifying question before writing code if something is ambiguous.
- End every response with a checklist mapping work to acceptance criteria
