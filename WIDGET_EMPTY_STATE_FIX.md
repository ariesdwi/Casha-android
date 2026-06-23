# Widget Empty State Fix

## Problem
Widget menampilkan kosong/abu-abu dengan hanya text "Casha" di tengah.

## Root Cause Analysis

Widget Casha memiliki 5 state yang berbeda:
1. **NORMAL** - Menampilkan data lengkap (safe spend, budget, dll)
2. **LOGGED_OUT** - User belum login
3. **NOT_PREMIUM** - User bukan premium member
4. **NO_DATA** - Tidak ada data widget di SharedPreferences
5. **HIDDEN** - User mengaktifkan hide balance

Widget akan menampilkan kosong jika berada di salah satu state fallback (2-5).

### State Resolution Flow
```kotlin
fun resolveWidgetState(context: Context): Pair<WidgetState, WidgetSummary?> {
    if (!WidgetPreferences.isLoggedIn(context)) {
        return WidgetState.LOGGED_OUT to null
    }
    if (!WidgetPreferences.isPremium(context)) {
        return WidgetState.NOT_PREMIUM to null
    }
    val summary = WidgetPreferences.getSummary(context)
    if (summary == null || summary.lastUpdatedAt == null) {
        return WidgetState.NO_DATA to null
    }
    if (summary.hideBalance) {
        return WidgetState.HIDDEN to summary
    }
    return WidgetState.NORMAL to summary
}
```

## Solution Implemented

### 1. Premium Status Check at Login
**Problem:** Saat login, hanya `isLoggedIn` yang diset, premium status tidak dicek.

**Fix:** Tambahkan pemeriksaan dan penyimpanan premium status di LoginViewModel:

```kotlin
// ✅ Check and save premium status
val isPremium = try {
    subscriptionManager.isPremium.firstOrNull() ?: false
} catch (_: Exception) { false }
WidgetPreferences.setPremium(context, isPremium)
```

**Files Modified:**
- `app/src/main/java/com/casha/app/ui/feature/auth/LoginViewModel.kt`
  - Added `SubscriptionManager` dependency
  - Added `firstOrNull()` import
  - Check premium status after login (both email and Google login)
  - Save premium status to widget preferences

### 2. Improved Fallback View
**Problem:** Fallback view tidak memberi tahu user untuk tap widget.

**Fix:** Tambahkan clickable action dan hint text:

```kotlin
Column(
    modifier = GlanceModifier
        .fillMaxSize()
        .clickable(
            actionRunCallback<DeepLinkAction>(
                actionParametersOf(DeepLinkAction.DeepLinkKey to "casha://dashboard")
            )
        ),
    // ... other parameters
) {
    // ... icon, title, subtitle
    Spacer(modifier = GlanceModifier.height(WidgetTheme.SpacingSmall))
    Text(
        text = "Tap untuk buka app",
        style = TextStyle(
            fontSize = 10.sp,
            color = ColorProvider(WidgetTheme.TextTertiary)
        )
    )
}
```

**Files Modified:**
- `app/src/main/java/com/casha/app/widget/ui/HomeSmallWidget.kt`
  - Made entire fallback view clickable to open Dashboard
  - Added "Tap untuk buka app" hint text

### 3. Widget Data Update Flow (Already Implemented)
Widget data diupdate di beberapa titik:
1. **Dashboard Load** - `DashboardViewModel.refreshDashboardInternal()` memanggil `updateWidgetData()`
2. **Login** - `LoginViewModel` set login state dan emit update event
3. **Transaction Changes** - `TransactionViewModel` emit update event
4. **Logout** - `ProfileViewModel` reset widget state

## User Instructions

### Jika Widget Masih Kosong:

1. **Pastikan Login:**
   - Buka app Casha
   - Login dengan email/password atau Google

2. **Pastikan Premium:**
   - Widget hanya tersedia untuk Casha Premium members
   - Upgrade ke Premium jika belum

3. **Load Data:**
   - Buka Dashboard (tab Home)
   - Widget akan otomatis ter-update dengan data terbaru

4. **Tap Widget:**
   - Jika widget menampilkan fallback state, tap widget untuk buka app
   - App akan langsung membuka Dashboard dan update data

## Fallback States Messages

| State | Icon | Title | Subtitle |
|-------|------|-------|----------|
| LOGGED_OUT | 👤 | Login ke Casha | Buka app untuk masuk |
| NOT_PREMIUM | 👑 | Casha Premium | Upgrade untuk akses widget |
| NO_DATA | 🔄 | Buka Casha | Menunggu data |
| HIDDEN | 🔒 | Saldo Tersembunyi | •••• |

Semua fallback state sekarang clickable untuk membuka Dashboard.

## Technical Notes

### Widget Data Storage
Widget menggunakan SharedPreferences dengan nama `casha_widget_prefs`:
- `isLoggedIn`: Boolean - login status
- `isPremium`: Boolean - premium subscription status
- `widgetSummary`: JSON string - serialized WidgetSummary object
- `lastUpdatedAt`: Long - epoch millis timestamp

### Data Flow
```
LoginViewModel
  ├─> WidgetPreferences.setLoggedIn(true)
  ├─> WidgetPreferences.setPremium(isPremium)
  └─> WidgetUpdateCoordinator.emit(LoginStateChanged)
       └─> WidgetRefreshWorker.refreshNow()
            └─> Widget re-renders with new state

DashboardViewModel
  ├─> Load data from API/DB
  ├─> updateWidgetData()
       ├─> WidgetUpdater.setAuthState()
       └─> WidgetUpdater.updateSummary()
            ├─> WidgetPreferences.saveSummary()
            └─> WidgetRefreshWorker.refreshNow()
                 └─> Widget re-renders with data
```

## Testing Checklist

- [x] Build berhasil tanpa error
- [x] LoginViewModel inject SubscriptionManager
- [x] Premium status dicek saat login
- [x] Fallback view clickable
- [x] Hint "Tap untuk buka app" ditampilkan
- [ ] Test widget setelah login (requires device/emulator)
- [ ] Test widget premium vs non-premium
- [ ] Test widget update saat Dashboard dibuka
- [ ] Test widget tap deep link ke Dashboard

## Files Changed

1. **app/src/main/java/com/casha/app/ui/feature/auth/LoginViewModel.kt**
   - Added SubscriptionManager dependency
   - Check premium status after login
   - Save premium status to widget preferences

2. **app/src/main/java/com/casha/app/widget/ui/HomeSmallWidget.kt**
   - Made WidgetFallbackView clickable
   - Added "Tap untuk buka app" hint

## Next Steps

1. Install app di device/emulator
2. Test flow: Login → Open Dashboard → Check widget
3. Verify widget menampilkan data atau fallback state yang jelas
4. Test tap widget untuk membuka Dashboard
5. Verify widget update real-time saat transaksi dibuat/diubah
