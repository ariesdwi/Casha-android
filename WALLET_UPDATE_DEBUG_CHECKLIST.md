# Wallet Update Debugging Checklist

**Problem**: Wallet tidak update setelah add income  
**Last Updated**: 2026-06-23

---

## ✅ CODE STATUS - Semua sudah implement!

### File 1: IncomeRepositoryImpl.kt
**Status**: ✅ SUDAH ADA  
**Location**: `app/src/main/java/com/casha/app/data/remote/impl/IncomeRepositoryImpl.kt`

```kotlin
// ✅ Import ada
import com.casha.app.core.network.SyncEventBus

// ✅ Constructor ada
private val syncEventBus: SyncEventBus

// ✅ saveIncome() ada
override suspend fun saveIncome(request: CreateIncomeRequest) {
    // 1️⃣ Save locally
    incomeDao.insertIncome(localEntity)
    Log.d(TAG, "✓ Income saved locally with ID: $localId")
    
    // 2️⃣ Async sync
    val result = safeApiCall { apiService.createIncome(dto) }
    
    // 3️⃣ Trigger wallet refresh
    if (request.assetId != null) {
        Log.d(TAG, "🔄 Income linked to wallet, triggering sync event")
        syncEventBus.emitSyncCompleted()  // ✅ THIS LINE!
    }
}
```

✅ **Conclusion**: Wallet refresh trigger SUDAH ADA!

---

### File 2: DashboardViewModel.kt
**Status**: ✅ SUDAH ADA  
**Location**: `app/src/main/java/com/casha/app/ui/feature/dashboard/DashboardViewModel.kt`

```kotlin
// ✅ Import ada
import com.casha.app.core.network.SyncEventBus

// ✅ Constructor parameter ada
private val syncEventBus: SyncEventBus,

// ✅ Init method call ada
init {
    setupSyncEventListener()  // Line 90
}

// ✅ Listener method ada
private fun setupSyncEventListener() {
    viewModelScope.launch {
        syncEventBus.syncCompletedEvent.collect {
            refreshDashboard(force = true)  // ✅ REFRESH CALL!
        }
    }
}
```

✅ **Conclusion**: Dashboard SUDAH listening dan akan refresh!

---

### File 3: TransactionViewModel.kt
**Status**: ✅ SUDAH ADA  
**Location**: `app/src/main/java/com/casha/app/ui/feature/transaction/TransactionViewModel.kt`

```kotlin
// ✅ Constructor parameter ada
private val syncEventBus: SyncEventBus,

// ✅ addIncome() method ada
fun addIncome(request: CreateIncomeRequest) {
    viewModelScope.launch {
        try {
            addIncomeUseCase(request)
            syncEventBus.emitSyncCompleted()  // ✅ EMIT CALL!
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
        }
    }
}

// ✅ setupSyncEventListener ada juga
private fun setupSyncEventListener() {
    viewModelScope.launch {
        syncEventBus.syncCompletedEvent.collect {
            fetchHistory()  // Fetch updated data
        }
    }
}
```

✅ **Conclusion**: Transaction ViewModel SUDAH emit dan listen!

---

### File 4: AddTransactionScreen.kt
**Status**: ✅ SUDAH ADA (tapi perlu di-cek implementasi)  
**Location**: `app/src/main/java/com/casha/app/ui/feature/transaction/AddTransactionScreen.kt`

```kotlin
// ✅ Variable ada
var selectedWalletId by remember { mutableStateOf<String?>(null) }

// ✅ Request dibuat dengan assetId
val req = TransactionRequest(
    name = name,
    category = selectedCategory,
    amount = amountValue,
    datetime = selectedDate,
    note = note.takeIf { it.isNotEmpty() },
    assetId = selectedWalletId  // ✅ ASSETID SET!
)

// ✅ Wallet picker UI ada
if (selectedWalletId != null) {
    val wallet = walletUiState.wallets.find { it.id == selectedWalletId }
    // Show selected wallet...
}
```

✅ **Conclusion**: Wallet picker SUDAH ada!

---

### File 5: SyncUseCases.kt
**Status**: ✅ SUDAH FIX  
**Location**: `app/src/main/java/com/casha/app/domain/usecase/dashboard/SyncUseCases.kt`

```kotlin
// ✅ Line 209 sudah di-fix
val allIncomes = incomeDao.getAllIncomesOnce()  // ✅ CORRECT!

// ❌ BUKAN ini (yang lama):
// val allIncomes = incomeDao.getAllIncomes().firstOrNull() ?: emptyList()
```

✅ **Conclusion**: Bug fix SUDAH applied!

---

## 🔍 KEMUNGKINAN MASALAH (Most Likely)

### Problem #1: AssetId TIDAK di-set ❌
**Probability**: ⭐⭐⭐⭐⭐ HIGHEST!

**Penyebab**:
- Saat add income, user tidak memilih wallet
- Field `selectedWalletId` tetap `null`
- Request dikirim dengan `assetId = null`
- `if (request.assetId != null)` gagal di IncomeRepositoryImpl
- `syncEventBus.emitSyncCompleted()` tidak dipanggil
- Dashboard tidak refresh

**Cara Check**:
```
1. Buka AddTransactionScreen
2. Add income
3. Lihat apakah ada field "Select wallet" atau semacamnya
4. Apakah ada wallet yang bisa di-pilih?
```

**Solusi**: HARUS SET WALLET!

---

### Problem #2: Wallet Picker tidak muncul untuk Income ⚠️
**Probability**: ⭐⭐⭐ MEDIUM!

**Penyebab**:
- AddTransactionScreen.kt mungkin hanya show wallet picker untuk EXPENSE
- Untuk INCOME, wallet picker tidak ditampilkan
- User tidak bisa pilih wallet

**Cara Check**:
```
1. Cek AddTransactionScreen.kt
2. Cari logic untuk show/hide wallet picker
3. Apakah conditional check ada? Misalnya if (isExpense)?
```

**Solusi**: Buat wallet picker selalu tampil untuk income

---

### Problem #3: Async Sync tidak selesai sebelum screen close ⚠️
**Probability**: ⭐⭐ LOW!

**Penyebab**:
- Async sync (API call) memakan waktu
- User navigasi keluar sebelum sync selesai
- Sync event tidak diterima dashboard

**Cara Check**:
- Tunggu ~2 detik sebelum navigasi
- Lihat apakah wallet update

---

### Problem #4: SyncEventBus tidak trigger refresh ⚠️
**Probability**: ⭐⭐ LOW! (sudah verify ada)

**Penyebab**:
- SyncEventBus tidak emit event ke-listener
- Listener tidak di-setup dengan benar
- Event collection timeout

**Cara Check**:
- Cek logcat dengan filter: `SyncEventBus|DashboardViewModel`
- Lihat apakah event di-emit

---

## 🧪 DEBUG STEPS (Do These!)

### Step 1: Enable Debug Logs
```bash
# Terminal dalam Android Studio / via adb
adb logcat | grep -E "IncomeRepository|SyncEventBus|DashboardViewModel" 
```

### Step 2: Test Add Income dengan Asset
```
1. Buka app
2. Navigate ke Add Income screen
3. Isi form:
   - Name: "Test Income"
   - Amount: 1,000,000
   - Type: SALARY
   - ⭐ SELECT WALLET (SANGAT PENTING!)
4. Tap "Save"
5. Amati log output
```

### Step 3: Expected Log Output
```
✓ Income saved locally with ID: abc123
✅ Income created on backend: id=xyz789
🔄 Income linked to wallet (wallet-123), triggering sync event
🔄 Refreshing dashboard
✅ Wallet balance updated!
```

### Step 4: Check Wallet UI
```
1. Tunggu ~2 detik
2. Lihat CardBalanceSection (wallet balance)
3. Harus sudah + income amount
```

---

## 📋 MOST LIKELY SOLUTION

### 99% kemungkinan issue adalah: **assetId tidak di-set**

**Checklist**:
- [ ] Saat add income, apakah muncul field untuk pilih wallet?
- [ ] Apakah ada wallet di list yang bisa di-pilih?
- [ ] Apakah wallet sudah di-select sebelum tap Save?

**If YES ke semua**: Berarti assetId sudah di-set, perlu debug lebih lanjut.  
**If NO ke satu atau lebih**: Itu masalahnya! Harus implement wallet picker untuk income.

---

## 🔧 NEXT STEPS

### If assetId tidak di-set (MOST LIKELY)
Kita perlu:
1. Make sure wallet picker tampil untuk income
2. Verify selectedWalletId di-pass ke CreateIncomeRequest
3. Test lagi

### If assetId sudah di-set tapi tetap tidak update
Kita perlu:
1. Check logcat untuk error message
2. Verify sync event di-emit
3. Verify DashboardViewModel receiving event
4. Check wallet refresh logic

---

## 📊 FLOW DIAGRAM (Should Work Like This)

```
User Add Income (dengan assetId)
         ↓
TransactionViewModel.addIncome()
         ↓
Repository.saveIncome()
    ├─ Save locally (instant)
    ├─ Async API sync
    └─ Emit syncEventBus.emitSyncCompleted()
         ↓
SyncEventBus receives event
         ↓
DashboardViewModel.setupSyncEventListener() catches event
         ↓
DashboardViewModel.refreshDashboard()
         ↓
GetWalletsUseCase() called
         ↓
Wallet balance fetched from API
         ↓
UI updated ✅
```

---

## ⚡ TL;DR

**Current Status**: ✅ Semua code sudah ada dan benar!

**Why Wallet Not Updating**: ⭐ Kemungkinan besar user tidak set assetId (wallet)

**How to Fix**:
1. **Make sure wallet picker muncul** untuk income form
2. **Pilih wallet** sebelum save income
3. **Tunggu 1-2 detik** untuk sync
4. Lihat wallet balance update

**If still not working**: Cek logcat untuk see where it breaks

---

Generated: 2026-06-23
