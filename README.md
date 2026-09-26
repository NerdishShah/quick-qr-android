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

## Release APK

Three ways to get an installable APK. **Never commit** a keystore, `keystore.properties`, or passwords — losing the release keystore permanently blocks Play Store updates for the same app ID.

### Option A — Android Studio

1. **Build → Generate Signed Bundle / APK…**
2. Choose **APK** → **Next**
3. **Create new…** keystore (or use an existing one). Store the `.jks` / `.keystore` file and passwords somewhere safe (password manager + offline backup).
4. Select **release**, finish the wizard
5. Install the generated APK on a device (sideload) or upload an **AAB** later for Play Console

### Option B — CLI (local signed release)

1. Create a keystore (interactive helper):

   ```bash
   ./scripts/create-keystore.sh
   ```

   Or manually:

   ```bash
   keytool -genkeypair -v -keystore release.jks -alias quickqr \
     -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Copy the example properties and fill in real values (file is gitignored):

   ```bash
   cp keystore.properties.example keystore.properties
   # edit storeFile, storePassword, keyAlias, keyPassword
   ```

   Or set env vars instead: `STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

3. Build:

   ```bash
   ./gradlew assembleRelease
   ```

4. APK path:

   ```text
   app/build/outputs/apk/release/app-release.apk
   ```

   If signing props are missing, `assembleRelease` still runs but the APK is **unsigned** (fine for CI smoke tests; not for sideload/Play without signing).

### Option C — GitHub Actions

CI runs on every push to `main`, pull requests, and manual **workflow_dispatch**.

1. Open the repo → **Actions** → **Android CI** → pick a run
2. Download the **app-debug** artifact (`app-debug.apk`) — always built; good for first-time sideload **without** a keystore
3. For a **signed release** APK, add these repository secrets (**Settings → Secrets and variables → Actions**):

   | Secret | Value |
   |--------|--------|
   | `SIGNING_KEYSTORE_BASE64` | Base64 of your `.jks` / `.keystore` (`base64 -w0 release.jks` on Linux) |
   | `STORE_PASSWORD` | Keystore password |
   | `KEY_ALIAS` | Key alias (e.g. `quickqr`) |
   | `KEY_PASSWORD` | Key password |

4. Re-run the workflow (or push). When all four secrets are set, CI also uploads **app-release** (`app-release.apk`).

Without those secrets, CI still succeeds and only uploads the debug APK.

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
