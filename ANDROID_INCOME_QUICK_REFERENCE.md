# Income Flow - Quick Reference Guide

**Quick lookup guide for the manual income transaction flow**

---

## 🚀 Quick Start: How Income Gets Created

### Step-by-Step Code Flow

```
1. USER INPUT (AddTransactionScreen.kt)
   └─ User taps "Save"
   
2. VIEWMODEL (TransactionViewModel.kt)
   └─ addIncome(request: CreateIncomeRequest)
   
3. USE CASE (AddIncomeUseCase.kt)
   └─ invoke(request) → repository.saveIncome(request)
   
4. REPOSITORY (IncomeRepositoryImpl.kt) ⭐ ENHANCED
   ├─ PART A: Optimistic Local Save
   │  └─ incomeDao.insertIncome(entity with isSynced=false)
   │  └─ Return immediately → UI shows income
   │
   └─ PART B: Async Backend Sync
      ├─ apiService.createIncome(dto)
      ├─ Mark isSynced=true
      ├─ [IF assetId] syncEventBus.emitSyncCompleted()
      └─ Dashboard refreshes wallet
```

---

## 📍 Key Files & Their Roles

| File | Role | Key Method |
|------|------|-----------|
| **AddTransactionScreen.kt** | UI Form | Shows income input form |
| **TransactionViewModel.kt** | State | `addIncome()` - calls use case, emits sync events |
| **AddIncomeUseCase.kt** | Business Logic | `invoke()` - validates and calls repository |
| **IncomeRepositoryImpl.kt** ⭐ | Data Access | `saveIncome()` - optimistic + async sync |
| **IncomeApiService.kt** | API | `POST /income` endpoint |
| **IncomeDao.kt** | Local DB | `insertIncome()` - saves to SQLite |
| **SyncEventBus.kt** | Events | `emitSyncCompleted()` - triggers dashboard refresh |
| **DashboardViewModel.kt** | Dashboard | Listens to sync events, refreshes wallet |

---

## 💻 Code Snippets - How To

### How to Add Income (From UI)

```kotlin
// In AddTransactionScreen.kt
fun onSaveIncome() {
    val incomeRequest = CreateIncomeRequest(
        name = "Salary",
        amount = 5000000.0,
        datetime = Date(),
        type = IncomeType.SALARY,
        source = "Company",
        assetId = "wallet-123",  // ← Link to wallet
        isRecurring = true,
        frequency = IncomeFrequency.MONTHLY,
        note = "Monthly salary"
    )
    
    viewModel.addIncome(incomeRequest)  // ← Triggers flow
}
```

### How Repository Saves Income

```kotlin
// In IncomeRepositoryImpl.kt - saveIncome()

// 1️⃣ STEP 1: OPTIMISTIC LOCAL SAVE
val localEntity = IncomeEntity(
    id = UUID.randomUUID().toString(),
    name = request.name,
    amount = request.amount,
    // ... other fields ...
    isSynced = false,  // Mark as pending sync
)
incomeDao.insertIncome(localEntity)  // Saved immediately!

// 2️⃣ STEP 2: ASYNC BACKEND SYNC
val result = safeApiCall { apiService.createIncome(dto) }
result.onSuccess { response ->
    // Update with server data
    val syncedEntity = entity.copy(
        id = response.data?.id,
        isSynced = true,  // Mark as synced
        remoteId = response.data?.id
    )
    incomeDao.insertIncome(syncedEntity)
    
    // 3️⃣ STEP 3: TRIGGER WALLET REFRESH
    if (request.assetId != null) {
        syncEventBus.emitSyncCompleted()  // ← Magic happens here!
    }
}
```

### How Dashboard Gets Refreshed

```kotlin
// In DashboardViewModel.kt - setupSyncEventListener()
viewModelScope.launch {
    syncEventBus.syncCompletedEvent.collect { event ->
        // When income with assetId is created:
        refreshDashboard(force = true)  // ← Refresh wallet
    }
}

// This triggers:
// 1. Fetch updated wallet data via GetWalletsUseCase
// 2. Fetch updated cashflow summary
// 3. Update UI with new balance
```

---

## 🔍 Where to Look for What

### "I need to understand the flow"
→ Read: **ANDROID_INCOME_FLOW_IMPLEMENTATION.md**

### "I need to test it"
→ Read: **ANDROID_INCOME_TESTING_GUIDE.md**

### "I need to debug it"
→ Search logs for: `IncomeRepository|DashboardViewModel|SyncEventBus`

### "I need to see where income is created"
→ Go to: **IncomeRepositoryImpl.kt** - `saveIncome()` method

### "I need to know if wallet will update"
→ Check: Is `assetId` provided in CreateIncomeRequest?
→ If YES → Wallet updates
→ If NO → No wallet update

### "I need to see where wallet updates"
→ Go to: **DashboardViewModel.kt** - `setupSyncEventListener()`

---

## ✅ Quick Checklist: Is It Working?

After adding income linked to wallet, verify:

- [ ] Income appears in transaction list immediately
- [ ] Logs show: `✓ Income saved locally with ID`
- [ ] After 0.5-1s: `✅ Income created on backend`
- [ ] Logs show: `🔄 Income linked to wallet, triggering sync`
- [ ] Logs show: `🔄 Instantly force UI state refresh`
- [ ] Wallet balance updated (old + income amount)

If any step missing → Check logs for errors

---

## 📊 Local vs Remote Data

```
LOCAL (SQLite - IncomeEntity)
├─ Saved immediately (isSynced = false)
├─ Used for offline support
└─ Updated after API response (isSynced = true)

REMOTE (Backend API)
├─ Receives POST /income request
├─ Updates wallet balance (if assetId provided)
└─ Returns response with remote ID
```

---

## 🔄 Sync Status Tracking

```
isSynced = false  →  Pending sync, will retry
isSynced = true   →  Synced with backend
remoteId = null   →  Not yet synced
remoteId = "abc"  →  Remote ID from backend
```

---

## 🚨 Common Mistakes

### ❌ WRONG: Expecting immediate wallet update
```kotlin
// WRONG - balance won't update immediately
viewModel.addIncome(request)  // Returns immediately
println(walletBalance)        // Still old value!
```

### ✅ CORRECT: Wait for sync event
```kotlin
// CORRECT - balance updates after sync
viewModel.addIncome(request)  // Returns immediately
// Dashboard refreshes automatically via SyncEventBus
// Balance updates within 1-2 seconds
```

### ❌ WRONG: Forgetting assetId
```kotlin
// WRONG - wallet won't refresh
val request = CreateIncomeRequest(
    // ... fields ...
    assetId = null  // ← No wallet update!
)
```

### ✅ CORRECT: Including assetId
```kotlin
// CORRECT - wallet will refresh
val request = CreateIncomeRequest(
    // ... fields ...
    assetId = "wallet-123"  // ← Wallet refreshes!
)
```

---

## 🔧 Debugging Steps

### Step 1: Check Local Save
```bash
adb logcat | grep "✓ Income saved locally"
```
If you see this: ✅ Local save works

### Step 2: Check API Sync
```bash
adb logcat | grep "✅ Income created on backend"
```
If you see this: ✅ Backend sync works

### Step 3: Check Wallet Link
```bash
adb logcat | grep "🔄 Income linked to wallet"
```
If you see this: ✅ Wallet will refresh
If you DON'T see this: ❌ assetId is NULL

### Step 4: Check Dashboard Refresh
```bash
adb logcat | grep "DashboardViewModel" | grep "refresh"
```
If you see this: ✅ Dashboard is refreshing

### Step 5: Verify Wallet Updated
Look at UI: Is balance updated? ✅ Yes → All working!

---

## 📋 Data Models

### CreateIncomeRequest (Input)
```kotlin
data class CreateIncomeRequest(
    val name: String,                    // "Salary"
    val amount: Double,                  // 5000000.0
    val datetime: Date,                  // Date()
    val type: IncomeType,                // SALARY, FREELANCE, etc
    val source: String?,                 // "Company"
    val assetId: String?,                // "wallet-123" ← IMPORTANT!
    val isRecurring: Boolean,            // true
    val frequency: IncomeFrequency?,     // MONTHLY
    val note: String?                    // "Monthly"
)
```

### IncomeEntity (Local DB)
```kotlin
@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey val id: String,
    val isSynced: Boolean,               // false = pending, true = synced
    val remoteId: String?,               // Server ID after sync
    // ... other fields ...
)
```

### IncomeCasha (Domain Model)
```kotlin
data class IncomeCasha(
    val id: String,
    val name: String,
    val amount: Double,
    // ... other fields ...
)
```

---

## 🎯 Decision Tree: Will Wallet Update?

```
Is assetId provided?
├─ YES → syncEventBus.emitSyncCompleted() triggered
│   └─ DashboardViewModel refreshes
│       └─ GetWalletsUseCase called
│           └─ Wallet balance updated ✅
│
└─ NO → No sync event
    └─ Wallet not refreshed ✅ (by design)
```

---

## 📱 End-to-End Example

```kotlin
// 1. User creates income
val income = CreateIncomeRequest(
    name = "Monthly Salary",
    amount = 5000000.0,
    datetime = Date(),
    type = IncomeType.SALARY,
    source = "Company ABC",
    assetId = "wallet-123",           // ← KEY FIELD!
    isRecurring = true,
    frequency = IncomeFrequency.MONTHLY,
    note = "Monthly payment"
)

// 2. ViewModel adds income
viewModel.addIncome(income)

// 3. What happens internally:
// ┌─ IncomeRepositoryImpl.saveIncome(income)
// ├─ 💾 Save locally to incomeDao (isSynced=false)
// ├─ 📤 POST /income to backend (async)
// ├─ ✅ Backend returns response
// ├─ 🔄 syncEventBus.emitSyncCompleted() triggered
// ├─ DashboardViewModel.setupSyncEventListener() receives event
// ├─ 🔄 refreshDashboard(force=true) called
// ├─ GET /wallets called
// ├─ Wallet balance updated: 10M → 15M
// └─ UI recomposes, user sees new balance

// Result: Wallet balance updated in ~2 seconds ✅
```

---

## 🎓 Architecture Patterns Used

### 1. Optimistic Update
```
Save locally first → UI responds immediately
Sync async → Update when backend responds
```

### 2. Event-Driven Architecture
```
Income created → SyncEventBus emits event
Event listeners respond → Dashboard refreshes
→ Loose coupling, easy to extend
```

### 3. Fault Tolerance
```
Sync fails? → Keep local copy
Network restored? → Auto-retry
User offline? → Still can add income locally
```

---

## 🔗 Flow at a Glance

```
AddTransactionScreen
         │
         ├─ User enters income data
         ├─ Taps "Save"
         │
TransactionViewModel
         │
         ├─ Calls addIncome()
         ├─ Emits sync signal
         │
AddIncomeUseCase
         │
         ├─ Validates data
         ├─ Calls repository
         │
IncomeRepositoryImpl ⭐
         │
         ├─ 💾 Save locally (instant)
         ├─ 📤 Sync async (background)
         ├─ 🔄 Emit wallet refresh (if linked)
         │
SyncEventBus
         │
         ├─ Broadcasts sync event
         │
DashboardViewModel
         │
         ├─ Receives event
         ├─ Calls getWallets()
         ├─ Updates state
         │
UI
         │
         └─ Balance shows updated ✅
```

---

## 📚 Documentation Map

```
Start Here
    │
    ├─ ANDROID_INCOME_IMPLEMENTATION_SUMMARY.md (this file)
    │  └─ Overview and status
    │
    ├─ ANDROID_INCOME_FLOW_IMPLEMENTATION.md
    │  └─ Detailed flow diagrams and architecture
    │
    ├─ ANDROID_INCOME_TESTING_GUIDE.md
    │  └─ How to test and debug
    │
    └─ Source Code
       ├─ IncomeRepositoryImpl.kt (saveIncome method)
       ├─ TransactionViewModel.kt (addIncome method)
       └─ DashboardViewModel.kt (setupSyncEventListener method)
```

---

## ✨ Summary

The income transaction flow is fully implemented with:
- ✅ Optimistic local save (instant UI feedback)
- ✅ Async backend sync (non-blocking)
- ✅ Automatic wallet refresh (if linked)
- ✅ Event-driven architecture (SyncEventBus)
- ✅ Comprehensive error handling
- ✅ Extensive logging (for debugging)

**To add income:** Just call `viewModel.addIncome(request)`
Everything else happens automatically! 🚀

---

Generated: June 23, 2026
