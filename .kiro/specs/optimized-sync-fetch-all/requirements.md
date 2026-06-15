# Optimized Sync Implementation: Fetch ALL Data

**Created**: 2026-06-09  
**Status**: Draft  
**Priority**: High  
**Type**: Feature Enhancement

---

## 📋 Problem Statement

### Current Issues

1. **Incomplete Data Sync**
   - `syncAndFetch()` only fetches **1 page** (default 50 items)
   - If user has 200 transactions, only 50 are synced to local database
   - Dashboard shows incomplete data

2. **Inaccurate Month Filtering** ⚠️ **CRITICAL**
   - When viewing **June 2026** data, transactions from **May 2026** appear in the results
   - This happens because:
     - Backend returns paginated data
     - Only first page is fetched
     - Month/year filter may not work correctly on backend
     - Local filtering relies on incomplete data
   - **Result**: Income and expense totals are WRONG

3. **Summary Calculations Wrong**
   - Filtered views (This Month, Last Month, etc.) show incorrect totals
   - Budget alerts calculated on incomplete data
   - Reports and charts inaccurate

### Example Scenario

```
User has 250 transactions in June 2026:
├── Current behavior:
│   ├── syncAndFetch() called → Fetches page 1 (50 items)
│   ├── Local DB has only 50 transactions
│   ├── Some May transactions appear in June filter
│   └── Dashboard shows: Income Rp 5,000,000 (WRONG! Should be Rp 12,000,000)
│
└── Expected behavior:
    ├── syncAndFetchAll() called → Fetches ALL pages (1, 2, 3, 4, 5)
    ├── Local DB has all 250 transactions
    ├── Proper date filtering applied
    └── Dashboard shows: Income Rp 12,000,000 (CORRECT!)
```

---

## 🎯 Goals

### Primary Goals

1. **Fetch ALL Transaction Pages**
   - Loop through all pagination pages automatically
   - Use larger page size (100 items) for efficiency
   - Sync complete dataset to local database

2. **Fix Date/Month Filtering**
   - Ensure transactions only appear in correct month
   - Accurate filtering for periods: This Month, Last Month, This Year
   - Fix timezone-related date issues

3. **Accurate Calculations**
   - Correct income/expense totals
   - Accurate budget alerts
   - Reliable dashboard summaries

### Secondary Goals

1. **Performance Optimization**
   - Minimize API calls with larger page size
   - Background sync to avoid blocking UI
   - Cache sync results

2. **User Experience**
   - Show sync progress indicator
   - Handle network errors gracefully
   - Offline-first with complete local data

---

## 👥 User Stories

### US-1: Complete Data Sync
**As a** user  
**I want** all my transactions to be synced automatically  
**So that** I see accurate financial summaries

**Acceptance Criteria**:
- [ ] All pages of transactions are fetched during sync
- [ ] Local database contains complete transaction history
- [ ] Dashboard shows all transactions in selected period

### US-2: Accurate Month Filtering
**As a** user  
**I want** to see only June transactions when I filter by June  
**So that** my monthly income/expense reports are correct

**Acceptance Criteria**:
- [ ] Transactions appear only in their actual month
- [ ] May transactions don't appear in June filter
- [ ] Date filtering respects timezone correctly

### US-3: Correct Income Totals
**As a** user  
**I want** accurate income and expense totals  
**So that** I can trust the financial data

**Acceptance Criteria**:
- [ ] Income total matches all income transactions in period
- [ ] Expense total matches all expense transactions in period
- [ ] Budget calculations based on complete data

### US-4: Sync Progress Feedback
**As a** user  
**I want** to see sync progress when fetching large datasets  
**So that** I know the app is working

**Acceptance Criteria**:
- [ ] Sync progress indicator shown during multi-page fetch
- [ ] Clear error messages if sync fails
- [ ] Ability to retry sync if it fails

---

## 🔧 Technical Requirements

### API Requirements

1. **Pagination Support**
   - GET `/cashflow/history?page={page}&pageSize={size}&month={month}&year={year}`
   - Response includes:
     ```json
     {
       "data": {
         "items": [...],
         "pagination": {
           "page": 1,
           "pageSize": 100,
           "totalPages": 3,
           "totalItems": 250
         }
       }
     }
     ```

2. **Date Filtering**
   - `month` parameter: "2026-06" format
   - `year` parameter: "2026" format
   - Backend must filter by transaction date (not created_at)

### Data Layer Requirements

1. **New Repository Method**
   - `suspend fun getHistoryAllPages(month: String?, year: String?, pageSize: Int = 100): List<CashflowEntry>`
   - Loops through all pages automatically
   - Returns complete list of entries

2. **UseCase Enhancement**
   - New method: `suspend fun syncAndFetchAll(month: String? = null, year: String? = null): List<CashflowEntry>`
   - Replaces calls to `syncAndFetch()` in ViewModel
   - Logs sync progress for debugging

### ViewModel Requirements

1. **Update Dashboard Sync**
   - Replace `cashflowSyncUseCase.syncAndFetch()` with `syncAndFetchAll()`
   - Pass month/year parameters when filtering by period
   - Handle loading state during multi-page fetch

2. **Loading States**
   ```kotlin
   data class SyncState(
       val isLoading: Boolean = false,
       val currentPage: Int = 0,
       val totalPages: Int = 0,
       val itemsFetched: Int = 0,
       val error: String? = null
   )
   ```

---

## 📊 Data Flow

### Current Flow (Broken)
```
User opens dashboard
  └→ DashboardViewModel.loadData()
       └→ cashflowSyncUseCase.syncAndFetch()
            └→ API: GET /cashflow/history?page=1&pageSize=50
                 └→ Returns 50 items (incomplete)
                      └→ Save to local DB
                           └→ Dashboard shows 50 items
                                └→ ❌ WRONG TOTALS
```

### New Flow (Fixed)
```
User opens dashboard
  └→ DashboardViewModel.loadData()
       └→ cashflowSyncUseCase.syncAndFetchAll(month="2026-06")
            └→ API: GET /cashflow/history?month=2026-06&page=1&pageSize=100
                 └→ Returns 100 items, totalPages=3
                      └→ API: GET /cashflow/history?month=2026-06&page=2&pageSize=100
                           └→ Returns 100 items
                                └→ API: GET /cashflow/history?month=2026-06&page=3&pageSize=100
                                     └→ Returns 50 items
                                          └→ Merge all 250 items to local DB
                                               └→ Dashboard shows ALL items
                                                    └→ ✅ CORRECT TOTALS
```

---

## 🧪 Test Scenarios

### Test 1: Small Dataset (< 100 items)
- **Given**: User has 50 transactions in June
- **When**: Sync is triggered
- **Then**: 
  - [ ] Only 1 API call made
  - [ ] All 50 transactions saved to DB
  - [ ] Dashboard shows correct total

### Test 2: Large Dataset (> 100 items)
- **Given**: User has 250 transactions in June
- **When**: Sync is triggered
- **Then**:
  - [ ] 3 API calls made (pages 1, 2, 3)
  - [ ] All 250 transactions saved to DB
  - [ ] Dashboard shows correct total

### Test 3: Month Filtering
- **Given**: User has transactions in May and June
- **When**: Filter set to June 2026
- **Then**:
  - [ ] Only June transactions fetched
  - [ ] May transactions not in results
  - [ ] Income/expense totals accurate for June only

### Test 4: Network Error
- **Given**: Network connection lost during page 2 fetch
- **When**: Sync is in progress
- **Then**:
  - [ ] Error shown to user
  - [ ] Page 1 data still saved to DB
  - [ ] Retry option available

### Test 5: Empty State
- **Given**: User has no transactions
- **When**: Sync is triggered
- **Then**:
  - [ ] 1 API call made
  - [ ] No crash or error
  - [ ] Empty state shown in dashboard

---

## 📏 Success Metrics

1. **Data Completeness**: 100% of transactions synced
2. **Accuracy**: Income/expense totals match server data within 0.01%
3. **Performance**: Full sync completes in < 3 seconds for 500 items
4. **Reliability**: Sync success rate > 99%

---

## 🚧 Out of Scope

- Real-time sync (websockets)
- Differential sync (only fetch changed items)
- Background sync scheduling
- Conflict resolution for offline edits

---

## 🔗 Related Issues

- [ ] Backend: Fix timezone handling for date filtering
- [ ] Backend: Ensure consistent date format in API responses
- [ ] iOS: Already implemented (reference: OPTIMIZED_SYNC_IMPLEMENTATION.md)

---

## 📝 Notes

- **Reference iOS implementation** for API patterns and flow
- **Maintain backward compatibility** with existing `syncAndFetch()` method
- **Log extensively** during development for debugging pagination issues
- **Consider rate limiting** - add delays between page fetches if needed
