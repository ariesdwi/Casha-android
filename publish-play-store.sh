#!/bin/bash

# ──────────────────────────────────────────────
# Casha Android - Google Play Store Upload
# (Release AAB via Google Play Developer API)
# ──────────────────────────────────────────────
# Usage:  ./publish-play-store.sh [track]
# Tracks: internal (default), alpha, beta, production
#
# Setup (sekali saja):
#   1. Python dependencies akan diinstall otomatis ke virtual environment
#      (Script akan create .venv/ folder otomatis)
#   2. Buat Service Account di Google Play Console:
#        → Setup → API access → Link Google Cloud Project
#        → Create service account → Grant "Release Manager" role
#        → Download JSON key → simpan sebagai: play-store-credentials.json
#   3. Pastikan play-store-credentials.json sudah di .gitignore ✅
# ──────────────────────────────────────────────

set -e

# ── Configuration ──
PACKAGE_NAME="com.casha.app"
TRACK="${1:-internal}"   # Default track: internal
CREDENTIALS_FILE="casha-30ee7-e52de804def5.json"
AAB_PATH="app/build/outputs/bundle/release/app-release.aab"

echo ""
echo "╔══════════════════════════════════════════════╗"
echo "║     Casha Android → Google Play Store        ║"
echo "╚══════════════════════════════════════════════╝"
echo ""

# ── Validate track ──
if [[ ! "$TRACK" =~ ^(internal|alpha|beta|production)$ ]]; then
  echo "❌ Invalid track: $TRACK"
  echo "   Valid tracks: internal, alpha, beta, production"
  echo "   Usage: ./publish-play-store.sh [internal|alpha|beta|production]"
  exit 1
fi

# ── Validate credentials ──
if [ ! -f "$CREDENTIALS_FILE" ]; then
  echo "❌ Service account credentials tidak ditemukan: $CREDENTIALS_FILE"
  echo ""
  echo "📋 Cara setup (sekali saja):"
  echo "   1. Buka Google Play Console → Setup → API access"
  echo "   2. Klik 'Link to Google Cloud project' (atau buat baru)"
  echo "   3. Di Google Cloud Console → IAM → Service Accounts → Create"
  echo "   4. Kembali ke Play Console → Grant 'Release Manager' role ke service account"
  echo "   5. Download JSON key dari Google Cloud Console"
  echo "   6. Simpan sebagai: play-store-credentials.json (di root project)"
  echo ""
  echo "   ⚠️  Jangan commit file ini ke Git! Sudah masuk .gitignore ✅"
  exit 1
fi

# ── Validate Python dependencies ──
echo "🔍 Checking Python dependencies..."

# Create virtual environment if it doesn't exist
VENV_DIR=".venv"
if [ ! -d "$VENV_DIR" ]; then
  echo "📦 Creating Python virtual environment..."
  python3 -m venv "$VENV_DIR"
  echo "✅ Virtual environment created!"
fi

# Activate virtual environment
source "$VENV_DIR/bin/activate"

# Check if packages are installed
if ! python3 -c "from googleapiclient.discovery import build; from google.oauth2 import service_account" 2>/dev/null; then
  echo "📦 Installing required Python packages..."
  pip3 install google-api-python-client google-auth --quiet
  echo "✅ Dependencies installed!"
else
  echo "✅ Dependencies already installed"
fi

# ── Increment version ──
echo "📈 Incrementing version code..."
./increment-version.sh

# ── Build release AAB ──
echo ""
echo "🔨 Building release AAB..."
./gradlew bundleRelease --quiet

if [ $? -ne 0 ]; then
  echo "❌ Build gagal!"
  echo "💡 Pastikan signing config di build.gradle.kts sudah benar"
  exit 1
fi

echo "✅ Build berhasil!"

# ── Verify AAB exists ──
if [ ! -f "$AAB_PATH" ]; then
  echo "❌ AAB tidak ditemukan di: $AAB_PATH"
  exit 1
fi

AAB_SIZE=$(du -sh "$AAB_PATH" | cut -f1)
echo "📦 AAB: $AAB_SIZE → $AAB_PATH"

# ── Prepare release notes ──
GIT_MSG=$(git log -1 --pretty=%s 2>/dev/null || echo "Manual build")
VERSION_NAME=$(grep -E 'versionName\s*=\s*"' app/build.gradle.kts | grep -oE '"[^"]+"' | tr -d '"')
VERSION_CODE=$(grep -E 'versionCode\s*=\s*[0-9]+' app/build.gradle.kts | grep -oE '[0-9]+')
RELEASE_NOTES="v${VERSION_NAME} (${VERSION_CODE}) | ${GIT_MSG}"

echo ""
echo "🚀 Uploading ke Google Play Store..."
echo "   📋 Package : $PACKAGE_NAME"
echo "   🎯 Track   : $TRACK"
echo "   🏷️  Version : v${VERSION_NAME} (${VERSION_CODE})"
echo "   📝 Notes   : ${GIT_MSG}"
echo ""

# ── Upload via Google Play Developer API ──
python3 - <<PYTHON
import sys
import httplib2
import google_auth_httplib2
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload
from google.oauth2 import service_account

SCOPES = ['https://www.googleapis.com/auth/androidpublisher']

try:
    credentials = service_account.Credentials.from_service_account_file(
        '$CREDENTIALS_FILE', scopes=SCOPES)
except Exception as e:
    print(f"❌ Gagal membaca credentials: {e}")
    sys.exit(1)

# Use a 10-minute timeout — httplib2 is the correct transport for googleapiclient
http = google_auth_httplib2.AuthorizedHttp(credentials, http=httplib2.Http(timeout=600))
service = build('androidpublisher', 'v3', http=http, cache_discovery=False)

# Create a new edit
edit = service.edits().insert(body={}, packageName='$PACKAGE_NAME').execute()
edit_id = edit['id']
print(f"📝 Edit ID: {edit_id}")

# Upload the AAB
print("⏳ Mengupload AAB... (mungkin butuh beberapa menit)")
media = MediaFileUpload('$AAB_PATH', mimetype='application/octet-stream', resumable=True)
bundle = service.edits().bundles().upload(
    packageName='$PACKAGE_NAME',
    editId=edit_id,
    media_body=media
).execute()
version_code = bundle['versionCode']
print(f"✅ Upload berhasil! Version code: {version_code}")

def set_track_and_commit(status):
    track_body = {
        'releases': [{
            'versionCodes': [version_code],
            'releaseNotes': [{'language': 'id-ID', 'text': '$RELEASE_NOTES'}],
            'status': status
        }]
    }
    service.edits().tracks().update(
        packageName='$PACKAGE_NAME',
        editId=edit_id,
        track='$TRACK',
        body=track_body
    ).execute()
    service.edits().commit(packageName='$PACKAGE_NAME', editId=edit_id).execute()

try:
    set_track_and_commit('completed')
    print(f"🎯 Track diset ke: $TRACK (status: completed)")
    print("🎉 Commit berhasil! Release sudah live di track: $TRACK")
except Exception as e:
    if 'draft app' in str(e).lower() or 'draft' in str(e).lower():
        print("")
        print("⚠️  App masih dalam status DRAFT di Play Console.")
        print("   Menggunakan status 'draft' sebagai fallback...")
        # Re-create edit karena edit sebelumnya sudah error
        edit2 = service.edits().insert(body={}, packageName='$PACKAGE_NAME').execute()
        edit_id2 = edit2['id']
        # Re-upload (tidak bisa reuse edit yang error)
        media2 = MediaFileUpload('$AAB_PATH', mimetype='application/octet-stream', resumable=True)
        bundle2 = service.edits().bundles().upload(
            packageName='$PACKAGE_NAME',
            editId=edit_id2,
            media_body=media2
        ).execute()
        set_track_and_commit.__globals__['edit_id'] = edit_id2
        version_code2 = bundle2['versionCode']
        set_track_and_commit.__globals__['version_code'] = version_code2
        set_track_and_commit('draft')
        print(f"✅ Tersimpan sebagai DRAFT di Play Console (version: {version_code2})")
        print("")
        print("📋 Langkah selanjutnya di Play Console:")
        print("   1. Lengkapi Store Listing (deskripsi, screenshot)")
        print("   2. Isi Content Rating questionnaire")
        print("   3. Set Pricing & Distribution")
        print("   4. Submit draft release untuk review")
        print("   🔗 https://play.google.com/console")
    else:
        print(f"❌ Error: {e}")
        sys.exit(1)
PYTHON

if [ $? -eq 0 ]; then
  echo ""
  echo "╔══════════════════════════════════════════════╗"
  echo "║  ✅ Berhasil publish ke Google Play Store!   ║"
  echo "╚══════════════════════════════════════════════╝"
  echo ""
  echo "   📋 Package : $PACKAGE_NAME"
  echo "   🎯 Track   : $TRACK"
  echo "   🔗 Console : https://play.google.com/console"
  echo ""
  echo "   ℹ️  Untuk publish ke track lain:"
  echo "       ./publish-play-store.sh alpha"
  echo "       ./publish-play-store.sh beta"
  echo "       ./publish-play-store.sh production"
  
  # Deactivate virtual environment
  deactivate
else
  echo ""
  echo "❌ Upload gagal!"
  echo "💡 Cek credentials dan pastikan service account punya akses 'Release Manager'"
  
  # Deactivate virtual environment
  deactivate
  exit 1
fi
