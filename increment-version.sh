#!/bin/bash

# Path to the build.gradle.kts file
GRADLE_FILE="app/build.gradle.kts"

# Check if the file exists
if [ ! -f "$GRADLE_FILE" ]; then
    echo "❌ Error: $GRADLE_FILE not found!"
    exit 1
fi

echo "📈 Incrementing version in $GRADLE_FILE..."

# Find the current versionCode line
CURRENT_VERSION_CODE_LINE=$(grep -E '^[[:space:]]*versionCode[[:space:]]+=[[:space:]]+[0-9]+' "$GRADLE_FILE")

if [ -z "$CURRENT_VERSION_CODE_LINE" ]; then
    echo "❌ Error: Could not find 'versionCode = X' in $GRADLE_FILE"
    exit 1
fi

# Extract the current versionName (e.g. "1.0.0")
CURRENT_VERSION_NAME=$(grep -E 'versionName\s*=\s*"' "$GRADLE_FILE" | grep -oE '"[^"]+"' | tr -d '"')

if [ -z "$CURRENT_VERSION_NAME" ]; then
    echo "❌ Error: Could not find 'versionName = \"X.Y.Z\"' in $GRADLE_FILE"
    exit 1
fi

# Extract the current number
CURRENT_VERSION_CODE=$(echo "$CURRENT_VERSION_CODE_LINE" | grep -oE '[0-9]+')

# Query Play Store for the highest versionCode across all tracks
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PLAY_STORE_MAX=$(python3 "$SCRIPT_DIR/get-playstore-version.py" 2>/dev/null)

if [ $? -eq 0 ] && [ -n "$PLAY_STORE_MAX" ]; then
    echo "   Play Store max versionCode: $PLAY_STORE_MAX"
    # Use the higher of local vs Play Store, then +1
    if [ "$PLAY_STORE_MAX" -gt "$CURRENT_VERSION_CODE" ]; then
        BASE_VERSION=$PLAY_STORE_MAX
        echo "   ⚠️  Play Store is ahead of local — using Play Store version as base"
    else
        BASE_VERSION=$CURRENT_VERSION_CODE
    fi
else
    echo "   ℹ️  Could not query Play Store (offline or first publish) — using local version"
    BASE_VERSION=$CURRENT_VERSION_CODE
fi

NEW_VERSION_CODE=$((BASE_VERSION + 1))

echo "   Local versionCode:   $CURRENT_VERSION_CODE"
echo "   New versionCode:     $NEW_VERSION_CODE"

# Replace the old version code with the new one 
# macOS sed requires an empty string for the backup extension sed -i ''
sed -i '' -E "s/^[[:space:]]*versionCode[[:space:]]+=[[:space:]]+[0-9]+/        versionCode = $NEW_VERSION_CODE/" "$GRADLE_FILE"

# ── Increment versionName (patch: X.Y.Z → X.Y.Z+1) ──
MAJOR=$(echo "$CURRENT_VERSION_NAME" | cut -d. -f1)
MINOR=$(echo "$CURRENT_VERSION_NAME" | cut -d. -f2)
PATCH=$(echo "$CURRENT_VERSION_NAME" | cut -d. -f3)
NEW_PATCH=$((PATCH + 1))
NEW_VERSION_NAME="${MAJOR}.${MINOR}.${NEW_PATCH}"

sed -i '' -E "s/versionName[[:space:]]*=[[:space:]]*\"[^\"]+\"/versionName = \"$NEW_VERSION_NAME\"/" "$GRADLE_FILE"

echo "   versionName:         $CURRENT_VERSION_NAME → $NEW_VERSION_NAME"
echo "✅ Incremented successfully!"
