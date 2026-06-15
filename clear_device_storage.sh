#!/bin/bash

# Script to clear device storage before installing app
echo "🧹 Clearing Android Device Storage..."
echo ""

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No device connected. Please connect your Android device or start emulator."
    exit 1
fi

echo "📱 Device detected!"
echo ""

# 1. Uninstall existing app
echo "1️⃣  Uninstalling existing Casha app..."
adb uninstall com.casha.app 2>/dev/null
if [ $? -eq 0 ]; then
    echo "   ✅ App uninstalled"
else
    echo "   ℹ️  App not installed or already removed"
fi
echo ""

# 2. Check storage before cleanup
echo "2️⃣  Checking storage BEFORE cleanup..."
adb shell df -h | grep "/data"
echo ""

# 3. Clear package manager cache
echo "3️⃣  Clearing package manager caches..."
adb shell pm trim-caches 500M
echo "   ✅ Caches trimmed"
echo ""

# 4. Clear dalvik cache (optional, aggressive)
read -p "4️⃣  Clear dalvik cache? (May take time) [y/N]: " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "   Clearing dalvik cache..."
    adb shell rm -rf /data/dalvik-cache/*
    echo "   ✅ Dalvik cache cleared"
else
    echo "   ⏭️  Skipped dalvik cache clearing"
fi
echo ""

# 5. Check storage after cleanup
echo "5️⃣  Checking storage AFTER cleanup..."
adb shell df -h | grep "/data"
echo ""

# 6. Summary
echo "✅ Storage cleanup complete!"
echo ""
echo "📊 Storage Summary:"
adb shell df -h /data | awk 'NR==1 || /\/data/'
echo ""
echo "Now you can build and install the app:"
echo "  ./gradlew installDebug"
