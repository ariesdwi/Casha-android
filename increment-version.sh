#!/bin/bash

# Path to the build.gradle.kts file
GRADLE_FILE="app/build.gradle.kts"

# Check if the file exists
if [ ! -f "$GRADLE_FILE" ]; then
    echo "❌ Error: $GRADLE_FILE not found!"
    exit 1
fi

echo "📈 Incrementing versionCode in $GRADLE_FILE..."

# Find the current versionCode line
CURRENT_VERSION_CODE_LINE=$(grep -E '^[[:space:]]*versionCode[[:space:]]+=[[:space:]]+[0-9]+' "$GRADLE_FILE")

if [ -z "$CURRENT_VERSION_CODE_LINE" ]; then
    echo "❌ Error: Could not find 'versionCode = X' in $GRADLE_FILE"
    exit 1
fi

# Extract the current number
CURRENT_VERSION_CODE=$(echo "$CURRENT_VERSION_CODE_LINE" | grep -oE '[0-9]+')
NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))

echo "   Current versionCode: $CURRENT_VERSION_CODE"
echo "   New versionCode:     $NEW_VERSION_CODE"

# Replace the old version code with the new one 
# macOS sed requires an empty string for the backup extension sed -i ''
sed -i '' -E "s/^[[:space:]]*versionCode[[:space:]]+=[[:space:]]+[0-9]+/        versionCode = $NEW_VERSION_CODE/" "$GRADLE_FILE"

echo "✅ Incremented successfully!"
