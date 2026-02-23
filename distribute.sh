#!/bin/bash

# ──────────────────────────────────────────────
# Casha Android - Firebase App Distribution
# ──────────────────────────────────────────────
# Usage:  ./distribute.sh
# ──────────────────────────────────────────────

# ── Configuration (customize these) ──
APP_ID="1:842221844066:android:fdb101126b760f8918c33b"
TESTERS="ariesdwiprasetiyo4@gmail.com"
# GROUPS="testers"  # Uncomment to use groups instead of individual testers

# ── Build ──
echo "🔨 Building debug APK..."
./gradlew assembleDebug --quiet

if [ $? -ne 0 ]; then
  echo "❌ Build failed!"
  exit 1
fi

echo "✅ Build successful!"

# ── Distribute ──
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
NOTES="Build #$(date '+%Y%m%d-%H%M') | $(git log -1 --pretty=%s 2>/dev/null || echo 'Manual build')"

echo "🚀 Uploading to Firebase App Distribution..."

firebase appdistribution:distribute "$APK_PATH" \
  --app "$APP_ID" \
  --testers "$TESTERS" \
  --release-notes "$NOTES"

if [ $? -eq 0 ]; then
  echo ""
  echo "🎉 Done! APK distributed to: $TESTERS"
else
  echo "❌ Distribution failed!"
  exit 1
fi
