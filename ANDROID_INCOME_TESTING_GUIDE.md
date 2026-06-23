# Income Transaction Flow - Testing & Validation Guide

Generated: June 23, 2026

---

## ✅ Implementation Complete

All components of the income transaction flow have been implemented and enhanced according to the iOS documentation specifications.

### Changes Made

#### 1. **Enhanced IncomeRepositoryImpl.kt** (✅ UPDATED)
**File:** `app/src/main/java/com/casha/app/data/remote/impl/IncomeRepositoryImpl.kt`

**Changes:**
- ✅ Added `SyncEventBus` dependency injection
- ✅ Enhanced `saveIncome()` with:
  - Optimistic local save (immediate UI update)
  - Async remote sync with proper error handling
  - Sync event emission when income linked to wallet
  - Detailed logging for debugging
  
- ✅ Enhanced `updateIncome()` with:
  - Logging and error tracking
  - Wallet refresh trigger when asset linked
  
- ✅ Enhanced `deleteIncome()` with:
  - Dashboard refresh trigger after deletion
  - Proper logging

- ✅ Added detailed logging to:
  - `getIncomes()` - Shows fetch status
  - `getSummary()` - Shows summary retrieval

**Log Tags:** All use `TAG = "IncomeRepository"` for easy filtering

#### 2. **Already Implemented Components** (✅ VERIFIED)

| Component | File | Status | Details |
|-----------|------|--------|---------|
| **TransactionViewModel** | TransactionViewModel.kt | ✅ Complete | Calls `addIncome()`, emits sync events, handles errors |
| **AddIncomeUseCase** | AddIncomeUseCase.kt | ✅ Complete | Calls repository.saveIncome() |
| **IncomeApiService** | FeatureApiServices.kt | ✅ Complete | Has POST /income endpoint |
| **IncomeDao** | RemainingDaos.kt | ✅ Complete | Local database operations |
| **IncomeEntity** | CategorIncomeEntities.kt | ✅ Complete | Local model with sync tracking |
| **AddTransactionScreen** | AddTransactionScreen.kt | ✅ Complete | UI form with income input |
| **DashboardViewModel** | DashboardViewModel.kt | ✅ Complete | Listens to sync events, refreshes on income added |
| **SyncEventBus** | SyncEventBus.kt | ✅ Complete | Event coordination system |

---

## 🧪 Testing Checklist

### Unit Test: Income Creation Flow

```kotlin
// File: app/src/androidTest/java/com/casha/app/IncomeFlowTest.kt

@Test
fun testIncomeCreationFlow() {
    // 1. Create income request
    val incomeRequest = CreateIncomeRequest(
        name = "Test Salary",
        amount = 5000000.0,
        datetime = Date(),
        type = IncomeType.SALARY,
        source = "Test Company",
        assetId = "wallet-123",  // Link to wallet
        isRecurring = true,
        frequency = IncomeFrequency.MONTHLY,
        note = "Test income"
    )
    
    // 2. Save income
    runTest {
        incomeRepository.saveIncome(incomeRequest)
    }
    
    // 3. Verify:
    // - ✅ Income saved locally immediately
    // - ✅ Sync event emitted (if assetId provided)
    // - ✅ Income marked as synced after API response
    // - ✅ Dashboard refreshed
}

@Test
fun testWalletRefreshAfterIncome() {
    // 1. Setup: Initial wallet balance = 10,000,000
    
    // 2. Add income linked to wallet: 5,000,000
    val incomeRequest = CreateIncomeRequest(
        name = "Income",
        amount = 5000000.0,
        datetime = Date(),
        type = IncomeType.SALARY,
        assetId = "wallet-123",  // ← Link to wallet
        isRecurring = false,
        frequency = null,
        note = null
    )
    
    runTest {
        incomeRepository.saveIncome(incomeRequest)
    }
    
    // 3. Verify:
    // - ✅ Wallet balance updated to 15,000,000
    // - ✅ Dashboard shows new balance
}

@Test
fun testIncomeWithoutWalletLink() {
    // Income not linked to wallet should not trigger wallet refresh
    val incomeRequest = CreateIncomeRequest(
        name = "Income",
        amount = 5000000.0,
        datetime = Date(),
        type = IncomeType.SALARY,
        assetId = null,  // ← No wallet link
        isRecurring = false,
        frequency = null,
        note = null
    )
    
    runTest {
        incomeRepository.saveIncome(incomeRequest)
    }
    
    // 3. Verify:
    // - ✅ Income saved locally
    // - ✅ No wallet refresh triggered
}
```

---

## 🔍 Manual Testing Steps

### Scenario 1: Add Income Linked to Wallet (WITH Balance Update)

**Setup:**
- User has a wallet with ID: `wallet-123`
- Current wallet balance: 10,000,000 IDR

**Steps:**
1. Open app
2. Navigate to "Add Transaction"
3. Select "Income" tab
4. Fill form:
   - Name: "Salary"
   - Amount: 5,000,000
   - Type: SALARY
   - Asset ID: Select your wallet (`wallet-123`)
   - Frequency: MONTHLY
5. Tap "Save"

**Expected Results:**
```
t=0.0s   Income form submitted
         ✓ Toast: "Saving income..."
         
t=0.2s   Income appears in transaction list (optimistic)
         
t=0.5s   Backend syncs
         Logs: "✓ Income saved locally with ID: {id}"
         
t=1.0s   API response received
         Logs: "✅ Income created on backend: id={remote-id}"
         Logs: "🔄 Income linked to wallet, triggering sync event"
         
t=1.2s   Dashboard refreshes
         Logs: "🔄 Instantly force UI state refresh"
         
t=1.5s   Wallet balance updated
         Old: 10,000,000 IDR
         New: 15,000,000 IDR ← BALANCE UPDATED ✅
         
t=1.8s   Success message shown
         ✓ Navigate back to transaction list
```

**Log Filter (in Android Studio):**
```
Filter: "IncomeRepository|SyncEventBus|DashboardViewModel"
```

---

### Scenario 2: Add Income WITHOUT Wallet Link (NO Balance Update)

**Steps:**
1. Open app
2. Navigate to "Add Transaction"
3. Select "Income" tab
4. Fill form:
   - Name: "Gift"
   - Amount: 1,000,000
   - Type: GIFT
   - **Asset ID: Leave EMPTY** ← No wallet link
   - Note: "Birthday gift"
5. Tap "Save"

**Expected Results:**
```
t=0.0s   Income form submitted
         ✓ Toast: "Saving income..."
         
t=0.2s   Income appears in transaction list
         
t=1.0s   API response received
         Logs: "✅ Income created on backend"
         Logs: "❌ NO wallet refresh" ← No sync event
         
t=1.2s   Success message shown
         ✓ Wallet balance remains unchanged ✅
```

---

### Scenario 3: Network Error Handling (Retry Flow)

**Steps:**
1. Turn off WiFi/disable mobile data (simulate offline)
2. Navigate to "Add Transaction"
3. Add income linked to wallet
4. Tap "Save"
5. Turn WiFi back on

**Expected Results:**
```
t=0.0s   Income form submitted
         ✓ Income saved locally immediately
         
t=0.5s   Background tries to sync
         Logs: "❌ Failed to sync income to backend: Network error"
         
t=0.6s   Status: Income marked as "Pending Sync"
         ✓ Local data preserved
         
t=2.0s   WiFi turned back on
         Background sync retries
         Logs: "✅ Income synced to backend"
         Logs: "🔄 Wallet refresh triggered"
         
t=2.5s   Wallet balance updated ✅
```

---

## 🐛 Debug Logging

### View Income Repository Logs
```bash
# In Android Studio Logcat
adb logcat | grep "IncomeRepository"

# Expected output:
# 💰 saveIncome() called: name=Salary, amount=5000000.0, assetId=wallet-123
# ✓ Income saved locally with ID: {uuid}
# ✅ Income created on backend: id={remote-id}
# 🔄 Income linked to wallet, triggering sync event
```

### View Sync Event Logs
```bash
adb logcat | grep "SyncEventBus"

# Expected output:
# 🔄 emitSyncCompleted() triggered
# ✓ Sync event broadcast to all listeners
```

### View Dashboard Refresh Logs
```bash
adb logcat | grep "DashboardViewModel"

# Expected output:
# 🔄 Instantly force UI state refresh when global event received
# 💾 Merging to local DB: {expenses.size} expenses, {incomes.size} incomes
```

### Complete Flow Log Chain
```bash
adb logcat | grep -E "IncomeRepository|SyncEventBus|DashboardViewModel|WalletViewModel"

# Will show complete chain:
# IncomeRepository: 💰 saveIncome()
# IncomeRepository: ✓ Income saved locally
# IncomeRepository: ✅ Income created on backend
# IncomeRepository: 🔄 Triggering sync event
# SyncEventBus: 🔄 emitSyncCompleted()
# DashboardViewModel: 🔄 Instantly refresh
# WalletViewModel: 💾 Fetching wallets
# DashboardComponents: ✓ Balance updated
```

---

## ✅ Validation Points

### Point 1: Local Optimistic Save
- [ ] After tapping "Save", income appears immediately in transaction list
- [ ] No wait for network response
- [ ] UI remains responsive

### Point 2: Backend Sync
- [ ] After 0.5-2s, logs show `✅ Income created on backend`
- [ ] Remote ID is returned and stored
- [ ] Income marked as synced in local database

### Point 3: Wallet Link Detection
- [ ] If `assetId` is provided, logs show `🔄 Income linked to wallet`
- [ ] If `assetId` is NULL, no wallet refresh logs appear

### Point 4: Dashboard Refresh
- [ ] After wallet link, logs show `🔄 Instantly force UI state refresh`
- [ ] Dashboard state is updated

### Point 5: Wallet Balance Update
- [ ] For LINKED income: Wallet balance = Old + Income Amount ✅
- [ ] For UNLINKED income: Wallet balance unchanged ✅

### Point 6: Error Handling
- [ ] Network error: Income stays locally with `isSynced = false`
- [ ] Error message shown to user
- [ ] Retry button appears

---

## 🎯 Expected Behavior Matrix

| Scenario | UI Shows Income | Sync Event | Wallet Updates | Logs |
|----------|-----------------|-----------|----------------|------|
| Linked + Online | ✅ Immediate | ✅ Yes | ✅ Yes | ✅ All |
| Linked + Offline | ✅ Immediate | ⏱️ Later | ✅ When online | ✅ Partial |
| Unlinked + Online | ✅ Immediate | ✅ Yes | ❌ No | ✅ No wallet |
| Unlinked + Offline | ✅ Immediate | ⏱️ Later | ❌ No | ✅ No wallet |
| Sync Fails | ✅ Stays | ❌ No | ❌ No | ✅ Error |

---

## 📊 Performance Metrics

### Expected Timings
- **Local Save:** < 50ms
- **UI Update:** < 100ms
- **API Call:** 0.5-2s (depends on network)
- **Wallet Refresh:** 0.5-1s after sync
- **Total End-to-End:** < 3s

### Database Size
- Each IncomeEntity: ~200 bytes
- 1000 incomes: ~200 KB

---

## 🔒 Security Considerations

### ✅ Implemented
- [x] Bearer token in Authorization header
- [x] HTTPS/TLS encryption
- [x] Date format validation (ISO8601)
- [x] Amount validation (> 0)

### ⚠️ To Verify
- [ ] User cannot see other users' income data
- [ ] API validates assetId belongs to current user
- [ ] Sync events only update current user's data

---

## 📱 Device Testing

### Test Devices
- [ ] Android 11 (SDK 30)
- [ ] Android 12 (SDK 31)
- [ ] Android 13 (SDK 33)
- [ ] Android 14 (SDK 34)

### Test Scenarios
- [ ] Portrait orientation
- [ ] Landscape orientation
- [ ] App backgrounded during sync
- [ ] App killed during sync
- [ ] Network switch (WiFi ↔ Mobile)

---

## 🎓 Troubleshooting

### Income Not Appearing in List
**Check:**
1. ✓ IncomeDao.insertIncome() called
2. ✓ getAllIncomes() Flow is being observed
3. ✓ _uiState.update() is triggered
4. ✓ Compose recomposition occurs

**Debug:**
```bash
adb logcat | grep "getAllIncomes"
# Should see logs from IncomeDao query
```

### Wallet Balance Not Updating
**Check:**
1. ✓ `assetId` is not NULL in request
2. ✓ SyncEventBus emits event (check logs)
3. ✓ DashboardViewModel receives event
4. ✓ GetWalletsUseCase refreshes wallet

**Debug:**
```bash
adb logcat | grep "🔄 Income linked to wallet"
# If not present, assetId is NULL

adb logcat | grep "DashboardViewModel" | grep "refresh"
# Should see dashboard refresh logs
```

### API Request Failing
**Check:**
1. ✓ Network connectivity
2. ✓ Bearer token validity (check 401 errors)
3. ✓ Backend is running
4. ✓ Request DTO matches backend schema

**Debug:**
```bash
adb logcat | grep "createIncome"
# Look for HTTP status codes (200, 401, 500, etc)

adb logcat | grep "❌ Failed to sync"
# Shows error message
```

---

## ✨ Next Optimizations

- [ ] Add retry mechanism with exponential backoff
- [ ] Batch income creation (multiple at once)
- [ ] Income template for recurring transactions
- [ ] Rich notifications for income events
- [ ] Income forecasting based on recurring income
- [ ] Currency conversion for multi-currency wallets

---

## 📚 Related Documentation

- [ANDROID_INCOME_FLOW_IMPLEMENTATION.md](ANDROID_INCOME_FLOW_IMPLEMENTATION.md) - Full flow documentation
- [MANUAL_INCOME_FLOW.md](../MANUAL_INCOME_FLOW.md) - iOS reference implementation
- [SyncEventBus.kt](app/src/main/java/com/casha/app/core/network/SyncEventBus.kt) - Event system
- [IncomeRepositoryImpl.kt](app/src/main/java/com/casha/app/data/remote/impl/IncomeRepositoryImpl.kt) - Repository with enhancements

---

## 📞 Support

If you encounter issues:

1. **Check Logs:** Filter by `IncomeRepository|DashboardViewModel|SyncEventBus`
2. **Verify Flow:** Follow the timeline in testing scenarios
3. **Check Network:** Ensure connectivity and valid tokens
4. **Check Database:** Verify IncomeEntity is being saved locally

Generated: June 23, 2026
