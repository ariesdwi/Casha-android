#!/bin/bash

# ──────────────────────────────────────────────
# Casha Android - Firebase App Distribution
# (Release Variant)
# ──────────────────────────────────────────────
# Usage:  ./distribute-release.sh
# ──────────────────────────────────────────────

# ── Configuration (customize these) ──
APP_ID="1:842221844066:android:e465e9ff1a66a36c18c33b"
TESTERS="ariesdwiprasetiyo4@gmail.com"
# GROUPS="testers"  # Uncomment to use groups instead of individual testers

# ── Build ──
echo "📈 Incrementing version code..."
./increment-version.sh

if [ $? -ne 0 ]; then
  echo "❌ Failed to increment version code!"
  exit 1
fi

echo "🔨 Building release APK..."
./gradlew assembleRelease --quiet

if [ $? -ne 0 ]; then
  echo "❌ Build failed!"
  echo "💡 Make sure your release signing config is set up in build.gradle.kts"
  exit 1
fi

echo "✅ Build successful!"

# ── Distribute ──
APK_PATH="app/build/outputs/apk/release/app-release.apk"

# Verify the APK exists
if [ ! -f "$APK_PATH" ]; then
  echo "❌ APK not found at $APK_PATH"
  echo "💡 Check your build output directory"
  exit 1
fi

NOTES="Release Build #$(date '+%Y%m%d-%H%M') | $(git log -1 --pretty=%s 2>/dev/null || echo 'Manual build')"

echo "🚀 Uploading to Firebase App Distribution..."

firebase appdistribution:distribute "$APK_PATH" \
  --app "$APP_ID" \
  --testers "$TESTERS" \
  --release-notes "$NOTES"

if [ $? -eq 0 ]; then
  echo ""
  echo "🎉 Done! Release APK distributed to: $TESTERS"
else
  echo "❌ Distribution failed!"
  exit 1
fi
