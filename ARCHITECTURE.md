Target end-state structure, now including the cross-platform bridge module. Each prompt below creates only
the slice it owns.

```text
hyperdrop/
├── RULES.md
├── ARCHITECTURE.md
├── app/                                        # Android app (Kotlin/Compose)
│   ├── build.gradle.kts
│   ├── src/
│       ├── main/
│           ├── AndroidManifest.xml
│           ├── java/com/hyperdrop/app/
│               ├── HyperDropApplication.kt
│               ├── di/
│               ├── presentation/
│                   ├── send/  receive/  transferdetail/  history/
│                   ├── dropsession/  multidevice/  migration/
│                   ├── crossplatform/          # Android<->iOS bridge UI (QR host/join)
│                   ├── qrcode/                 # Reusable QR generate + scan components
│                   ├── privacydashboard/  performancelab/
│                   ├── common/
│               ├── domain/
│                   ├── model/  usecase/  repository/
│               ├── data/
│                   ├── transport/
│                       ├── nearby/             # Android<->Android
│                       ├── crossplatform/      # local HTTP bridge server+client (Android<->iOS)
│                   ├── transfer/{chunking,verification,scheduler}/
│                   ├── security/  storage/  diagnostics/  repository/
│               ├── util/
│           ├── res/
│       ├── test/
│       ├── androidTest/
├── ios-companion/                              # OPTIONAL / STRETCH — separate Xcode/Swift project
│   ├── README.md                               # marked experimental, not required for MVP or demo
│   ├── HyperDropCompanion/                     # Swift/SwiftUI skeleton, built only if Prompt 37 runs
├── docs/
│   ├── protocol/
│       ├── threat-model.md
│       ├── object-model.md
│       ├── cross-platform-bridge.md            # HTTP bridge protocol spec (Android<->iOS)
│   ├── benchmarks/{methodology.md,results/}
├── scripts/
```
