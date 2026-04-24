#!/usr/bin/env python3
"""
Query Google Play Store for the highest versionCode across all tracks.
Prints just the number to stdout. Exits non-zero on failure.
Used by increment-version.sh to avoid versionCode conflicts.
"""
import sys
import os

PACKAGE_NAME = "com.casha.app"
CREDENTIALS_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "casha-30ee7-e52de804def5.json")

try:
    from googleapiclient.discovery import build
    from google.oauth2 import service_account
except ImportError:
    print("missing python deps", file=sys.stderr)
    sys.exit(1)

if not os.path.isfile(CREDENTIALS_FILE):
    print(f"credentials not found: {CREDENTIALS_FILE}", file=sys.stderr)
    sys.exit(1)

try:
    credentials = service_account.Credentials.from_service_account_file(
        CREDENTIALS_FILE,
        scopes=['https://www.googleapis.com/auth/androidpublisher']
    )
    service = build('androidpublisher', 'v3', credentials=credentials)

    # Create a temporary edit (read-only, will be deleted)
    edit = service.edits().insert(body={}, packageName=PACKAGE_NAME).execute()
    edit_id = edit['id']

    # List all tracks and find the highest versionCode
    tracks_response = service.edits().tracks().list(
        packageName=PACKAGE_NAME, editId=edit_id
    ).execute()

    max_version_code = 0
    for track in tracks_response.get('tracks', []):
        for release in track.get('releases', []):
            for vc in release.get('versionCodes', []):
                if int(vc) > max_version_code:
                    max_version_code = int(vc)

    # Cleanup: delete the edit (no commit)
    try:
        service.edits().delete(packageName=PACKAGE_NAME, editId=edit_id).execute()
    except Exception:
        pass  # Best-effort cleanup

    if max_version_code > 0:
        print(max_version_code)
    else:
        print("no releases found on Play Store", file=sys.stderr)
        sys.exit(1)

except Exception as e:
    print(f"api error: {e}", file=sys.stderr)
    sys.exit(1)
