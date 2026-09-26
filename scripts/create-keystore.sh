#!/usr/bin/env bash
# Interactive helper: create a release keystore + keystore.properties (gitignored).
# Usage: from repo root → ./scripts/create-keystore.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

KEYSTORE_FILE="${KEYSTORE_FILE:-release.jks}"
PROPS_FILE="${PROPS_FILE:-keystore.properties}"
EXAMPLE_FILE="keystore.properties.example"

if ! command -v keytool >/dev/null 2>&1; then
  echo "error: keytool not found. Install a JDK (17+) and ensure keytool is on PATH." >&2
  exit 1
fi

if [[ -f "$KEYSTORE_FILE" ]]; then
  echo "error: $KEYSTORE_FILE already exists. Move or remove it first." >&2
  exit 1
fi

if [[ -f "$PROPS_FILE" ]]; then
  echo "error: $PROPS_FILE already exists. Move or remove it first." >&2
  exit 1
fi

echo "=== Quick QR — create release keystore ==="
echo "Output: $KEYSTORE_FILE  +  $PROPS_FILE"
echo "Keep both private. Losing the keystore blocks Play Store updates."
echo

read -r -p "Key alias [quickqr]: " KEY_ALIAS
KEY_ALIAS="${KEY_ALIAS:-quickqr}"

read -r -s -p "Store password: " STORE_PASSWORD
echo
read -r -s -p "Key password (same as store is fine): " KEY_PASSWORD
echo
KEY_PASSWORD="${KEY_PASSWORD:-$STORE_PASSWORD}"

read -r -p "Your name / org (CN) [Quick QR]: " CN
CN="${CN:-Quick QR}"

read -r -p "Validity days [10000]: " VALIDITY
VALIDITY="${VALIDITY:-10000}"

keytool -genkeypair \
  -v \
  -keystore "$KEYSTORE_FILE" \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity "$VALIDITY" \
  -storepass "$STORE_PASSWORD" \
  -keypass "$KEY_PASSWORD" \
  -dname "CN=$CN"

cat > "$PROPS_FILE" <<PROPS
storeFile=$KEYSTORE_FILE
storePassword=$STORE_PASSWORD
keyAlias=$KEY_ALIAS
keyPassword=$KEY_PASSWORD
PROPS

# Ensure example stays committed for documentation (no secrets).
if [[ ! -f "$EXAMPLE_FILE" ]]; then
  cat > "$EXAMPLE_FILE" <<'EXAMPLE'
# Copy to keystore.properties (gitignored) and fill in real values.
# Never commit keystore.properties, *.jks, or *.keystore.

storeFile=release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=quickqr
keyPassword=YOUR_KEY_PASSWORD
EXAMPLE
fi

echo
echo "Created:"
echo "  $KEYSTORE_FILE"
echo "  $PROPS_FILE"
echo
echo "Build a signed release APK:"
echo "  ./gradlew assembleRelease"
echo "  → app/build/outputs/apk/release/app-release.apk"
echo
echo "For GitHub Actions signed builds, add repo secrets:"
echo "  SIGNING_KEYSTORE_BASE64  =  base64 -w0 $KEYSTORE_FILE"
echo "  STORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD"
