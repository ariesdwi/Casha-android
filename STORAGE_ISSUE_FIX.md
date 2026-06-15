# Android Storage Issue Fix

## Error Message
```
Installation failed due to: 'Error code: 'UNKNOWN', message='Unknown failure: Exception occurred while executing 'install-create':
java.io.IOException: Requested internal only, but not enough space
```

## Root Cause
Device/Emulator tidak memiliki cukup internal storage untuk install APK (app size ~50-100MB).

---

## 🔧 Solutions (Pick One)

### ✅ Solution 1: Uninstall Old App (Fastest)

Jika app sudah ter-install sebelumnya, uninstall dulu:

```bash
# Find adb (Android Debug Bridge)
# Usually located at: ~/Library/Android/sdk/platform-tools/adb

# Check if device connected
~/Library/Android/sdk/platform-tools/adb devices

# Uninstall app
~/Library/Android/sdk/platform-tools/adb uninstall com.casha.app

# Then rebuild and install
./gradlew clean installDebug
```

**Via Android Studio:**
1. Open Android Studio
2. Menu: Build → Clean Project
3. Uninstall app from device/emulator manually
4. Menu: Run → Run 'app'

---

### ✅ Solution 2: Clear Device Cache

```bash
# Clear package manager cache (frees ~200-500MB)
~/Library/Android/sdk/platform-tools/adb shell pm trim-caches 500M

# Or clear all app caches
~/Library/Android/sdk/platform-tools/adb shell pm clear-cache

# Check available space
~/Library/Android/sdk/platform-tools/adb shell df -h | grep "/data"
```

---

### ✅ Solution 3: Wipe Emulator Data (Emulator Only)

**Best solution for emulator - frees all space:**

1. Open **Android Studio**
2. Click **Tools** → **Device Manager** (or AVD Manager)
3. Find your emulator
4. Click **⋮** (three dots) → **Wipe Data**
5. Confirm and restart emulator
6. Rebuild: `./gradlew installDebug`

---

### ✅ Solution 4: Increase Emulator Storage (Emulator Only)

**Permanent fix - increase storage capacity:**

1. Open **Android Studio**
2. Click **Tools** → **Device Manager**
3. Click **✏️ (Edit)** on your emulator
4. Click **Show Advanced Settings**
5. Find **Internal Storage** (default: 800 MB)
6. Change to **4096 MB** (4 GB) or **8192 MB** (8 GB)
7. Click **Finish**
8. Delete old emulator image:
   ```bash
   rm -rf ~/.android/avd/[EMULATOR_NAME].avd
   ```
9. Restart emulator

---

### ✅ Solution 5: Allow External Storage Install

**Added to AndroidManifest.xml** (already done):

```xml
<manifest 
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:installLocation="auto">
```

Options:
- `auto` - System decides (internal or external)
- `preferExternal` - Prefer SD card if available
- `internalOnly` - Force internal (default, causes this error)

Now app can be installed to SD card if internal is full.

---

## 🚀 Quick Commands

### Setup ADB Path (One-time)
```bash
# Add to ~/.zshrc or ~/.bash_profile
echo 'export PATH="$PATH:$HOME/Library/Android/sdk/platform-tools"' >> ~/.zshrc
source ~/.zshrc

# Verify
adb version
```

### Clear & Install Script
```bash
# Make script executable
chmod +x clear_device_storage.sh

# Run script
./clear_device_storage.sh

# Then install
./gradlew installDebug
```

### Manual Quick Fix
```bash
# 1. Uninstall old app
adb uninstall com.casha.app

# 2. Clear caches
adb shell pm trim-caches 500M

# 3. Check space
adb shell df -h | grep "/data"

# 4. Install new version
./gradlew installDebug
```

---

## 📊 Check Storage Status

### Check Available Space
```bash
# Show storage usage
adb shell df -h

# Show only /data partition
adb shell df -h /data

# Human-readable output:
# Filesystem      Size  Used  Avail Use% Mounted on
# /data           2.0G  1.8G  200M  90% /data
#                       ^^^^  ^^^^
#                       Used  Free <- Need >100MB free
```

### Check App Size
```bash
# Check APK size
ls -lh app/build/intermediates/apk/debug/app-debug.apk

# Typical Casha app size: 50-80 MB
```

### List Installed Apps by Size
```bash
# Show largest apps
adb shell pm list packages -3 | cut -d: -f2 | \
  while read pkg; do 
    size=$(adb shell du -sh /data/data/$pkg 2>/dev/null | cut -f1)
    echo "$size $pkg"
  done | sort -h -r | head -10
```

---

## 🔍 Troubleshooting

### Issue: "adb: command not found"
**Solution:** Add adb to PATH (see Setup ADB Path above)

### Issue: "error: device offline"
**Solution:** 
```bash
adb kill-server
adb start-server
adb devices
```

### Issue: "error: more than one device/emulator"
**Solution:** Specify device
```bash
# List devices
adb devices

# Use specific device (-s flag)
adb -s <device_id> uninstall com.casha.app
```

### Issue: Still not enough space after clearing
**Solutions:**
1. **Emulator:** Wipe data or increase storage size
2. **Physical Device:** Delete apps/photos/videos
3. **Last Resort:** Factory reset device

---

## 📝 Prevention

### For Emulator Development:
- Create emulator with **4GB+ internal storage**
- Wipe data regularly (monthly)
- Use separate emulator for different projects

### For Physical Device:
- Keep 1GB+ free space
- Regularly uninstall test apps
- Clear cache monthly
- Use SD card if available

---

## 🛠️ Files Modified

**app/src/main/AndroidManifest.xml**
- Added `android:installLocation="auto"` attribute
- Allows system to install on external storage if internal is full

**clear_device_storage.sh** (New)
- Automated script to clean device storage
- Safe to run anytime
- Interactive prompts for aggressive cleaning

---

## ✅ Recommended Workflow

For daily development:

```bash
# 1. Check space before building
adb shell df -h /data

# 2. If <200MB free, clear cache
adb shell pm trim-caches 500M

# 3. Build and install
./gradlew installDebug

# 4. If fails, uninstall old app
adb uninstall com.casha.app
./gradlew installDebug
```

---

## 📱 Device Requirements

### Minimum Requirements:
- **Internal Storage Available:** 200MB free
- **Android Version:** 7.0+ (API 24+)
- **RAM:** 2GB+

### Recommended for Development:
- **Internal Storage Available:** 1GB+ free
- **Android Version:** 13+ (API 33+)
- **RAM:** 4GB+
- **Emulator Storage:** 4GB+

---

## 🎯 Success Indicators

After running solutions, you should see:

```bash
$ adb shell df -h /data
Filesystem      Size  Used  Avail Use% Mounted on
/data           2.0G  1.5G  500M  75% /data
                            ^^^^
                            >200MB free ✅
```

And installation succeeds:
```
$ ./gradlew installDebug
...
BUILD SUCCESSFUL in 15s
Installing APK...
✅ Installation succeeded
```

---

## 🆘 Still Having Issues?

If none of these solutions work:

1. **Check APK integrity:**
   ```bash
   ls -lh app/build/intermediates/apk/debug/app-debug.apk
   # Should be 50-100MB, if >200MB something is wrong
   ```

2. **Try different device/emulator:**
   - Create new emulator with 8GB storage
   - Try physical device

3. **Check Android Studio logs:**
   - View → Tool Windows → Logcat
   - Filter: "PackageInstaller"

4. **Report issue** with:
   - Device/Emulator specs
   - Output of `adb shell df -h`
   - Full error log from Android Studio

---

## 📚 References

- [Android: App Install Location](https://developer.android.com/guide/topics/data/install-location)
- [ADB Commands Reference](https://developer.android.com/tools/adb)
- [Emulator Storage Configuration](https://developer.android.com/studio/run/managing-avds)
