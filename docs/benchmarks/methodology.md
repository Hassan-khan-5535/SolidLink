# HyperDrop Benchmark Methodology

This document outlines the strict benchmark methodologies to be executed in Phase 9. All performance claims must be objectively measured on real devices using these defined scenarios. 

## Benchmark Matrix

| Benchmark Scenario | Exact Metric | Exact Test Condition | Pass/Fail Threshold / Label |
| :--- | :--- | :--- | :--- |
| **100MB Transfer** | Peak Throughput (MB/s) & Total Transfer Time (s) | Uninterrupted 5GHz Wi-Fi Direct connection, 2 modern devices, 1m apart, screen on. | *Informational only* |
| **1GB Transfer** | Peak Throughput (MB/s) & Total Transfer Time (s) | Uninterrupted 5GHz Wi-Fi Direct connection, 2 modern devices, 1m apart, screen on. | *Informational only* |
| **5GB Transfer** | Peak Throughput (MB/s) & Total Transfer Time (s) | Uninterrupted 5GHz Wi-Fi Direct connection, 2 modern devices, 1m apart, screen on. | *Informational only* |
| **10GB Transfer** | Peak Throughput (MB/s) & Total Transfer Time (s) | Uninterrupted 5GHz Wi-Fi Direct connection, 2 modern devices, 1m apart, screen on. | *Informational only* |
| **20GB Transfer** | Sustained Throughput (MB/s) & Total Transfer Time (s) | Uninterrupted 5GHz Wi-Fi Direct connection, 2 modern devices, 1m apart, screen on. | *Informational only* |
| **Interrupted transfer recovery** | Re-connection and resume latency (s) | Start 1GB transfer. Manually disable Sender Wi-Fi/Bluetooth at 500MB, wait 10 seconds, re-enable radios. | **PASS** if transfer resumes automatically within 30 seconds of radio restoration and successfully verifies. |
| **Weak connection degradation** | Sustained Throughput (MB/s) & Disconnect Rate (%) | Devices moved to edge of Wi-Fi range (signal strength < -80dBm) or obstructed by 2 solid walls. | *Informational only* (Observe graceful degradation vs hard crash). |
| **Large folder enumeration** | Time to build FileManifest (s) | Selected directory containing 10,000 mixed small files (images, text) via Storage Access Framework. | **PASS** if UI does not ANR and manifest generation completes in < 15 seconds. |
| **Low storage handling** | Expected Error State / App State | Receiver device has 1GB free storage available. Sender attempts to transfer a 5GB file. | **PASS** if transfer is blocked pre-flight with clear storage error, preventing chunk streaming. |
| **Battery/thermal impact** | Battery drain (%) & Peak device temperature (°C) | Sustained continuous transfer of 50GB data over 45 minutes, screen on at 50% brightness. | *Informational only* (Flag if battery drain > 15% per 30 mins). |
| **Multiple-receiver fairness** | Throughput variance (%) | 1 Sender simultaneously streaming identical 1GB file to 3 Receivers via Hotspot/LAN topology. | **PASS** if all 3 receivers complete within 20% time deviation of one another. |
| **Integrity failure detection** | Error detection rate (%) | Mock test: Manually flip 1 byte of a transmitted chunk in memory prior to final SHA-256 verification hash. | **PASS** if chunk/file correctly fails integrity check and is discarded 100% of the time. |
| **Cross-platform vs Native** | Throughput Ratio (%) | Transfer 1GB file over local 5GHz Wi-Fi router. Compare Android->iOS (web bridge) vs Android->Android (Nearby API). | *Informational only* (Goal: web bridge achieves > 80% of native LAN throughput). |

## Execution Guidelines
- **No Emulators:** All benchmarks must be executed on physical Android hardware.
- **Isolate Variables:** Ensure background syncing (Google Photos, Play Store updates) is paused before starting a run.
- **Reporting:** Each benchmark run will snapshot the commit hash, device models, OS versions, and the timestamp, saving results into the `docs/benchmarks/results/` directory (created during Phase 9).
