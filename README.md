# Quick QR

A simple Android QR code scanner built with **Kotlin**, **Jetpack Compose**, **CameraX**, and **ML Kit**.

Scan a code → see the text → **Open** links, **Copy**, or scan again. Optional recent history is included.

## Features

- Full-screen live camera preview with a dark viewfinder overlay
- Runtime **CAMERA** permission with rationale / settings fallback
- QR-only detection via ML Kit Barcode Scanning
- Light haptic feedback on detect; scan lock to avoid duplicates
- **Result** screen with type chip (URL / text / FIDO)
- **Open** — `Intent.ACTION_VIEW` for `http`/`https` and other actionable deep links
- **Copy** to clipboard
- **Scan again**
- Optional **Recent** history (SharedPreferences, last 20)
- Secondary **Pair with passkey** when the payload looks like a FIDO hybrid / device-pairing QR (e.g. `FIDO:/…`)

## Passkey pairing (optional)

Some QR codes used for **FIDO Cross-Device / hybrid** flows start with schemes like `FIDO:/`. Quick QR does **not** implement proprietary crypto or a custom WebAuthn server. When such a code is detected, the app offers **Pair with passkey**, which fires `Intent.ACTION_VIEW` on the raw URI so **Google Play Services** / the system FIDO path can continue the hybrid passkey flow (phone as authenticator), if a handler is installed.

If no handler is found, you get a clear message and can still **Copy** the payload.

## Requirements

- Android Studio Ladybug (2024.2+) or newer recommended
- JDK 17
- Android device or emulator with a camera (minSdk **26**, targetSdk **35**)

## Open in Android Studio

1. Clone this repo
2. **File → Open** and select the project root
3. Let Gradle sync (Android SDK 35 / Build-Tools will be prompted if missing)
4. Run the **app** configuration on a device/emulator

## Build a debug APK

```bash
./gradlew assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Install:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## How Open vs Pair work

| Action | When enabled | What happens |
|--------|----------------|--------------|
| **Open** | `http`/`https` or other actionable non-FIDO URIs | `Intent.ACTION_VIEW` on the scanned string |
| **Pair with passkey** | Payload looks like FIDO hybrid (`FIDO:/`, related prefixes) | Same `ACTION_VIEW` hand-off so Play Services / system FIDO can handle it |
| **Copy** | Always | Clipboard |
| **Scan again** | Always | Returns to camera; scan lock resets |

## Limitations

- Needs a physical or virtual camera
- Passkey / hybrid pairing depends on a compatible **Play Services** (or other) handler — this app is not a FIDO server
- Only **QR** codes are scanned (not 1D barcodes)
- No account sync; history is local only

## Stack

- Kotlin, single-module `app`
- Gradle Kotlin DSL + Version Catalog (`gradle/libs.versions.toml`)
- Jetpack Compose + Material 3
- CameraX + ML Kit Barcode Scanning
- Accompanist Permissions

## License

MIT — see [LICENSE](LICENSE).
