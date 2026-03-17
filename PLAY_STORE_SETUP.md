# 📦 Cara Setup Google Play Store Publishing

Panduan lengkap untuk menyambungkan project Casha Android ke Google Play Console dan menjalankan upload via script.

---

## Prasyarat

Install Python dependency (sekali saja):
```bash
pip3 install google-api-python-client google-auth
```

---

## Step 1 — Buat Service Account di Google Play Console

1. Buka [Google Play Console](https://play.google.com/console)
2. Pilih **Setup → API access**
3. Klik **"Link to Google Cloud Project"** (atau buat project baru)
4. Di halaman yang terbuka (Google Cloud Console), klik **"Create new service account"**
5. Isi nama, misal: `casha-play-uploader`
6. Klik **Create and Continue**
7. Pilih role **tidak ada** (role dikasih dari Play Console langsung)
8. Klik **Done**

---

## Step 2 — Beri Akses di Play Console

1. Kembali ke **Google Play Console → Setup → API access**
2. Di bawah Service Accounts, cari akun yang baru dibuat
3. Klik **"Grant access"**
4. Set permission: ✅ **Release apps to testing tracks** + ✅ **Release apps to production**
5. Klik **Apply**

---

## Step 3 — Download JSON Key

1. Buka [Google Cloud Console → IAM → Service Accounts](https://console.cloud.google.com/iam-admin/serviceaccounts)
2. Klik service account `casha-play-uploader`
3. Tab **Keys → Add Key → Create new key → JSON**
4. File terdownload otomatis
5. **Rename file → `play-store-credentials.json`**
6. **Pindahkan ke root project** (sejajar dengan `publish-play-store.sh`)

```
Casha-android/
├── play-store-credentials.json  ← letakkan di sini
├── publish-play-store.sh
├── distribute-release.sh
└── ...
```

> ⚠️ File ini sudah masuk `.gitignore` — **JANGAN commit ke Git!**

---

## Step 4 — Jalankan Upload

```bash
# Upload ke Internal Testing (default)
./publish-play-store.sh

# Upload ke Alpha
./publish-play-store.sh alpha

# Upload ke Beta
./publish-play-store.sh beta

# Upload ke Production
./publish-play-store.sh production
```

---

## Perbandingan Script

| Script | Tujuan | Track |
|--------|--------|-------|
| `./distribute.sh` | Firebase App Distribution (debug) | Testers langsung |
| `./distribute-release.sh` | Firebase (release) | Testers langsung |
| `./publish-play-store.sh` | **Google Play Store** | internal/alpha/beta/production |

---

## Troubleshooting

| Error | Solusi |
|-------|--------|
| `credentials tidak ditemukan` | Pastikan `play-store-credentials.json` ada di root project |
| `403 Forbidden` | Pastikan service account sudah diberi akses di Play Console (Step 2) |
| `Build gagal` | Cek signing config di `build.gradle.kts` |
| `Python module not found` | Jalankan: `pip3 install google-api-python-client google-auth` |
| `AAB size too large` | Cek ProGuard rules di `proguard-rules.pro` |
