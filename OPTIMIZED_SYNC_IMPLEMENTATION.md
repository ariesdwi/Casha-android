# 🚀 Optimized Sync Implementation: Fetch ALL Data

**Date**: June 1, 2026  
**Status**: Implemented & Ready to Test

---

## 🎯 Problem

**Before**: `syncAndFetch()` hanya fetch **1 page** (50 items)
- Jika user punya 200 transaksi, hanya 50 yang di-sync
- Data tidak lengkap di CoreData
- Filter dan summary tidak akurat

**After**: `syncAndFetchAll()` fetch **SEMUA pages** sampai habis
- Loop fetch page 1, 2, 3, ... sampai selesai
- Semua data di-sync ke CoreData
- Data lengkap dan akurat

---

## 📊 New Implementation

### 1. **New Method: `syncAndFetchAll()`**

```swift
public func syncAndFetchAll(
    month: String? = nil,
    year: String? = nil,
    type: CashflowType? = nil
) async throws -> [CashflowEntry]
```

**Features**:
- ✅ Fetch **ALL pages** automatically
- ✅ Use `pageSize = 100` for efficiency
- ✅ Merge all data to CoreData
- ✅ Return complete array of entries
- ✅ Detailed logging for debugging

**Flow**:
```
1. Start with page 1
2. Fetch page 1 → Get totalPages (e.g., 5)
3. Loop:
   - Fetch page 2
   - Fetch page 3
   - Fetch page 4
   - Fetch page 5
4. Merge ALL entries to CoreData
5. Return complete data
```

---

## 🔄 Sync Flow

### **Full Sync (All Data)**

```
┌─────────────────────────────────────────────────────────────┐
│ USER: Pull to refresh / Tap sync button                     │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ DashboardState.syncData()                                   │
│ Print: "🔄 Starting full data sync..."                     │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ CashflowSyncUseCase.syncAndFetchAll()                       │
│ - No month/year filter (fetch ALL)                          │
│ - pageSize = 100                                            │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ API CALL: Page 1                                            │
│ GET /cashflow/history?page=1&pageSize=100                   │
│                                                              │
│ Response:                                                    │
│ {                                                            │
│   "items": [100 items],                                     │
│   "pagination": {                                            │
│     "page": 1,                                              │
│     "totalPages": 3,                                        │
│     "totalItems": 250                                       │
│   }                                                          │
│ }                                                            │
│                                                              │
│ Print: "🔄 Fetching page 1 of 3..."                        │
│ Print: "✅ Fetched 100 items (total so far: 100)"          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ API CALL: Page 2                                            │
│ GET /cashflow/history?page=2&pageSize=100                   │
│                                                              │
│ Response: 100 items                                         │
│ Print: "🔄 Fetching page 2 of 3..."                        │
│ Print: "✅ Fetched 100 items (total so far: 200)"          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ API CALL: Page 3                                            │
│ GET /cashflow/history?page=3&pageSize=100                   │
│                                                              │
│ Response: 50 items (last page)                              │
│ Print: "🔄 Fetching page 3 of 3..."                        │
│ Print: "✅ Fetched 50 items (total so far: 250)"           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Print: "✅ Full sync complete: 250 total items"            │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ SPLIT BY TYPE                                               │
│ - Expenses: 180 items                                       │
│ - Incomes: 70 items                                         │
│                                                              │
│ Print: "💾 Merging to CoreData: 180 expenses, 70 incomes" │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ MERGE TO COREDATA                                           │
│ - localTransactionRepo.mergeTransactions(180 items)         │
│ - localIncomeRepo.mergeIncomes(70 items)                    │
│                                                              │
│ Print: "✅ CoreData merge complete"                         │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ REFRESH DASHBOARD                                           │
│ - Load from CoreData (now complete!)                        │
│ - Display all 250 transactions                              │
│ - ✅ Data up-to-date!                                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 📱 Usage Examples

### Example 1: Full Sync (All Data)

```swift
// Sync ALL transactions (no filter)
let allEntries = try await cashflowSyncUseCase.syncAndFetchAll()
print("Synced \(allEntries.count) total transactions")
```

**API Calls**:
```
GET /cashflow/history?page=1&pageSize=100
GET /cashflow/history?page=2&pageSize=100
GET /cashflow/history?page=3&pageSize=100
...
```

**Result**: ALL transactions synced to CoreData

---

### Example 2: Sync Specific Month

```swift
// Sync only June 2026
let juneEntries = try await cashflowSyncUseCase.syncAndFetchAll(
    month: "2026-06"
)
print("Synced \(juneEntries.count) June transactions")
```

**API Calls**:
```
GET /cashflow/history?month=2026-06&page=1&pageSize=100
GET /cashflow/history?month=2026-06&page=2&pageSize=100
...
```

**Result**: All June transactions synced

---

### Example 3: Sync Specific Year

```swift
// Sync all 2026 transactions
let year2026 = try await cashflowSyncUseCase.syncAndFetchAll(
    year: "2026"
)
print("Synced \(year2026.count) transactions for 2026")
```

---

### Example 4: Sync by Type

```swift
// Sync only expenses
let expenses = try await cashflowSyncUseCase.syncAndFetchAll(
    type: .expense
)
print("Synced \(expenses.count) expenses")
```

---

## 🎯 When to Use Each Method

### Use `syncAndFetchAll()` for:
- ✅ **Initial sync** after login
- ✅ **Full refresh** (pull to refresh)
- ✅ **Background sync** (app foreground)
- ✅ **Complete data** needed

### Use `syncAndFetch()` (single page) for:
- ✅ **Pagination** in UI (load more)
- ✅ **Quick preview** (first 50 items)
- ✅ **Incremental loading**

---

## 📊 Performance Comparison

### Before (Single Page)
```
API Calls: 1
Items Fetched: 50
Time: ~500ms
Data Complete: ❌ No (only 50 of 250)
```

### After (All Pages)
```
API Calls: 3 (for 250 items)
Items Fetched: 250
Time: ~1.5s (3 × 500ms)
Data Complete: ✅ Yes (all 250)
```

**Trade-off**: Slightly slower, but **data is complete**

---

## 🔍 Debug Logs

When sync runs, you'll see:

```
🔄 Starting full data sync...
🔄 Starting full sync for month: all, year: all
🔄 Fetching page 1 of 3...
✅ Fetched 100 items (total so far: 100)
🔄 Fetching page 2 of 3...
✅ Fetched 100 items (total so far: 200)
🔄 Fetching page 3 of 3...
✅ Fetched 50 items (total so far: 250)
✅ Full sync complete: 250 total items
💾 Merging to CoreData: 180 expenses, 70 incomes
✅ CoreData merge complete
✅ Full sync complete, refreshing dashboard...
```

---

## ⚡ Optimization Tips

### 1. **Use Larger Page Size**
```swift
let pageSize = 100 // Instead of 20 or 50
```
**Benefit**: Fewer API calls, faster sync

### 2. **Sync in Background**
```swift
Task.detached(priority: .background) {
    try await cashflowSyncUseCase.syncAndFetchAll()
}
```
**Benefit**: Don't block UI

### 3. **Sync Only Recent Data**
```swift
// Sync only last 3 months
let threeMonthsAgo = Calendar.current.date(byAdding: .month, value: -3, to: Date())
let entries = try await cashflowSyncUseCase.syncAndFetchAll(
    month: DateHelper.format(threeMonthsAgo, style: .monthYearAPI)
)
```
**Benefit**: Faster sync for recent data

### 4. **Cache Last Sync Time**
```swift
let lastSync = UserDefaults.standard.object(forKey: "lastSyncTime") as? Date
let now = Date()

// Only sync if > 5 minutes since last sync
if lastSync == nil || now.timeIntervalSince(lastSync!) > 300 {
    try await cashflowSyncUseCase.syncAndFetchAll()
    UserDefaults.standard.set(now, forKey: "lastSyncTime")
}
```
**Benefit**: Avoid unnecessary syncs

---

## 🧪 Testing Checklist

- [ ] **Full sync with 0 transactions** (empty state)
- [ ] **Full sync with 50 transactions** (1 page)
- [ ] **Full sync with 150 transactions** (2 pages)
- [ ] **Full sync with 500 transactions** (5 pages)
- [ ] **Sync specific month** (June 2026)
- [ ] **Sync specific year** (2026)
- [ ] **Sync by type** (expenses only)
- [ ] **Network error handling** (timeout, 401, 500)
- [ ] **Verify CoreData** (all items saved)
- [ ] **Verify Dashboard** (data displayed correctly)
- [ ] **Performance** (measure time for large datasets)

---

## 🚨 Error Handling

### Network Error
```swift
do {
    try await cashflowSyncUseCase.syncAndFetchAll()
} catch {
    print("❌ Sync failed: \(error)")
    // Fallback to local data
    let localData = await cashflowSyncUseCase.loadFromLocal()
}
```

### Partial Sync (Some Pages Fail)
```swift
// If page 3 fails, we still have page 1 & 2 data
// CoreData will have partial data (better than nothing)
```

---

## 📋 Migration Guide

### Old Code (Single Page)
```swift
// ❌ OLD: Only fetches 50 items
let result = try await cashflowSyncUseCase.syncAndFetch()
print("Fetched \(result.entries.count) items") // Max 50
```

### New Code (All Pages)
```swift
// ✅ NEW: Fetches ALL items
let allEntries = try await cashflowSyncUseCase.syncAndFetchAll()
print("Fetched \(allEntries.count) items") // All items
```

---

## 🎯 Summary

### What Changed
1. **Added `syncAndFetchAll()`** - Fetch all pages automatically
2. **Updated `syncData()`** - Use `syncAndFetchAll()` instead of `syncAndFetch()`
3. **Re-enabled sync** - Removed temporary disable flags
4. **Added logging** - Track sync progress

### Benefits
- ✅ **Complete data** - All transactions synced
- ✅ **Accurate filters** - Filter works correctly
- ✅ **Up-to-date** - Dashboard shows latest data
- ✅ **Reliable** - No missing transactions

### Trade-offs
- ⚠️ **Slightly slower** - Multiple API calls
- ⚠️ **More bandwidth** - Fetch all data
- ✅ **Worth it** - Complete data is critical

---

## 🔜 Next Steps

1. **Test on device** - Verify sync works
2. **Monitor performance** - Check sync time
3. **Optimize if needed** - Add caching, background sync
4. **Backend fix** - Still need timezone fix for accuracy

**Status**: Ready to test! 🚀
