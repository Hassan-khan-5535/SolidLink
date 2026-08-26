# ⚡ SolidLink (HyperDrop)

> **Android-First, Offline-First, Privacy-First P2P File Transfer & Cross-Platform Bridge**

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=android&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

SolidLink (HyperDrop) is a high-speed peer-to-peer file transfer system engineered for zero-cloud, privacy-respecting, cross-platform file sharing using Google Nearby Connections and a local Wi-Fi HTTP bridge.

---

## ✨ Features

- **🚀 High-Speed P2P Sharing**: Direct Android-to-Android transfer powered by Nearby Connections API (Wi-Fi Direct / High Bandwidth).
- **🌐 Cross-Platform Web Bridge**: Instant Android-to-iOS / Web transfers via local HTTP web server & QR code host/join mechanism.
- **🔒 Privacy & Integrity**: Cryptographic checksum verification (SHA-256) per chunk, zero telemetry, no cloud servers.
- **📷 Smart QR Scanner**: Integrated CameraX + ML Kit barcode scanner for seamless 1-tap device pairing.
- **📁 Large File Streaming**: Memory-efficient streaming pipeline handling multi-gigabyte transfers smoothly.

---

## 🛠 Tech Stack

| Layer | Technologies |
|---|---|
| **Language & UI** | Kotlin, Jetpack Compose, Material 3 |
| **Architecture** | MVVM + Clean Architecture + Hilt DI |
| **Android P2P** | Google Nearby Connections API (`ConnectionsClient`) |
| **QR Code** | ZXing Core (Generation) + CameraX & ML Kit (Scanning) |
| **Storage & DB** | Room Database, Storage Access Framework (SAF) |

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (or newer)
- **JDK** 17+
- **Android SDK** API 26 (Android 8.0) or higher

### Build & Run (Android Studio)
1. Clone the repository:
   ```bash
   git clone https://github.com/Hassan-khan-5535/SolidLink.git
   ```
2. Open `SolidLink` in **Android Studio**.
3. Connect an Android device via USB (with USB Debugging enabled) or start an **Android Emulator**.
4. Click **Run** (`Shift + F10`) or execute in terminal:
   ```bash
   ./gradlew installDebug
   ```

### 📱 Testing Web Bridge / Mobile View in Browser
1. Start the Cross-Platform Web Bridge inside the app (Hosts local server).
2. Scan the displayed QR Code on an iOS device or open the generated URL (`http://<LOCAL_IP>:8080`) in any desktop browser.
3. Open **DevTools** in browser (`F12` or `Ctrl + Shift + I`).
4. Click **Toggle Device Toolbar** (`Ctrl + Shift + M`) and choose a device (e.g., *iPhone 14* / *Pixel 7*) to test the responsive mobile view.

---

## 📄 License
Distributed under the MIT License. See `LICENSE` for details.
