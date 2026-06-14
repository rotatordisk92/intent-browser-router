#!/bin/bash

# IntentBrowserRouter Build & Sign Script
# Usage: ./build-and-sign.sh

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$PROJECT_DIR/app/build"
OUTPUT_APK="$BUILD_DIR/outputs/apk/release/app-release.apk"
KEYSTORE_FILE="$PROJECT_DIR/keystore.jks"
KEYSTORE_PASSWORD="android"
KEY_ALIAS="android"
KEY_PASSWORD="android"

echo "📦 IntentBrowserRouter Build & Sign"
echo "=================================="

# Check if keystore exists, create if not
if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "🔑 Creating keystore..."
    keytool -genkey -v -keystore "$KEYSTORE_FILE" \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -alias "$KEY_ALIAS" \
        -storepass "$KEYSTORE_PASSWORD" \
        -keypass "$KEY_PASSWORD" \
        -dname "CN=IntentBrowserRouter, O=Developer, C=US" || true
fi

# Build release APK
echo "🔨 Building release APK..."
cd "$PROJECT_DIR"
if command -v gradlew &> /dev/null; then
    ./gradlew assembleRelease
else
    gradle assembleRelease
fi

if [ ! -f "$OUTPUT_APK" ]; then
    echo "❌ Build failed"
    exit 1
fi

# Sign APK
echo "✍️  Signing APK..."
SIGNED_APK="$BUILD_DIR/outputs/apk/release/app-release-signed.apk"
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
    -keystore "$KEYSTORE_FILE" \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEY_PASSWORD" \
    "$OUTPUT_APK" "$KEY_ALIAS"

# Verify signature
echo "✅ Verifying signature..."
jarsigner -verify -verbose "$OUTPUT_APK"

echo ""
echo "✨ Success!"
echo "📱 APK ready: $OUTPUT_APK"
echo ""
echo "To install on your device:"
echo "  adb install -r $OUTPUT_APK"
