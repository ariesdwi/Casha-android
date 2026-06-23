# Technical Design: Optimized Sync Fetch All

**Created**: 2026-06-09  
**Status**: Draft

---

## 🏗️ Architecture Overview

### Components to Modify

```
┌─────────────────────────────────────────────────────────────┐
│                     DashboardViewModel                       │
│  - Replace syncAndFetch() → syncAndFetchAll()               │
│  - Pass month/year parameters                                │
│  - Handle loading state                                      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   CashflowSyncUseCase                        │
│  + syncAndFetchAll(month, year): List<CashflowEntry>        │
│  - Loop through all pages                                    │
│  - Merge data to local repos                                 │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  CashflowRepository (Remote)                 │
│  + getHistoryAllPages(month, year, pageSize): List          │
│  - Handle pagination loop                                    │
│  - Call API multiple times                                   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    CashflowApiService                        │
│  getHistory(month, year, page, pageSize): Response          │
│  - Existing API, no changes                                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Data Models

### API Response Model (Existing)

```kotlin
data class CashflowHistoryResponse(
    val entries: List<CashflowEntry>,
    val pagination: PaginationInfo?
)

data class PaginationInfo(
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalItems: Int
)
```

### Sync Progress State (New)

```kotlin
data class SyncProgress(
    val isLoading: Boolean = false,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val itemsFetched: Int = 0,
    val totalItems: Int = 0,
    val error: String? = null
) {
    val progress: Float
        get() = if (totalPages > 0) currentPage.toFloat() / totalPages else 0f
}
```

---

## 🔌 API Layer

### File: `CashflowRepositoryImpl.kt`

#### New Method: `getHistoryAllPages()`

```kotlin
override suspend fun getHistoryAllPages(
    month: String?,
    year: String?,
    pageSize: Int
): List<CashflowEntry> {
    val allEntries = mutableListOf<CashflowEntry>()
    var currentPage = 1
    var totalPages = 1
    
    Log.d("CashflowSync", "🔄 Starting full sync - month: ${month ?: "all"}, year: ${year ?: "all"}")
    
    while (currentPage <= totalPages) {
        Log.d("CashflowSync", "🔄 Fetching page $currentPage of $totalPages...")
        
        val response = getHistory(
            month = month,
            year = year,
            page = currentPage,
            pageSize = pageSize
        )
        
        allEntries.addAll(response.entries)
        
        // Update total pages from first response
        if (currentPage == 1) {
            totalPages = response.pagination?.totalPages ?: 1
            Log.d("CashflowSync", "📊 Total pages to fetch: $totalPages")
        }
        
        Log.d("CashflowSync", "✅ Fetched ${response.entries.size} items (total so far: ${allEntries.size})")
        
        currentPage++
        
        // Optional: Add small delay to avoid rate limiting
        if (currentPage <= totalPages) {
            kotlinx.coroutines.delay(100) // 100ms delay between requests
        }
    }
    
    Log.d("CashflowSync", "✅ Full sync complete: ${allEntries.size} total items")
    return allEntries
}
```

### Interface Addition

```kotlin
// File: CashflowRepository.kt
interface CashflowRepository {
    // Existing
    suspend fun getHistory(
        month: String?,
        year: String?,
        page: Int,
        pageSize: Int
    ): CashflowHistoryResponse
    
    // New
    suspend fun getHistoryAllPages(
        month: String?,
        year: String?,
        pageSize: Int = 100
    ): List<CashflowEntry>
}
```

---

## 💼 UseCase Layer

### File: `CashflowSyncUseCase.kt`

#### New Method: `syncAndFetchAll()`

```kotlin
suspend fun syncAndFetchAll(
    month: String? = null,
    year: String? = null
): List<CashflowEntry> {
    Log.d("CashflowSync", "🔄 Starting syncAndFetchAll - month: $month, year: $year")
    
    try {
        // Fetch all pages from remote
        val allEntries = cashflowRepository.getHistoryAllPages(
            month = month,
            year = year,
            pageSize = 100
        )
        
        if (allEntries.isEmpty()) {
            Log.d("CashflowSync", "ℹ️ No entries returned from API")
            return emptyList()
        }
        
        // Split by type
        val expenses = allEntries.filter { it.type == CashflowType.EXPENSE }
        val incomes = allEntries.filter { it.type == CashflowType.INCOME }
        
        Log.d("CashflowSync", "💾 Merging to local DB: ${expenses.size} expenses, ${incomes.size} incomes")
        
        // Merge to local repositories
        withContext(Dispatchers.IO) {
            launch {
                localTransactionRepo.mergeTransactions(
                    expenses.map { it.toTransactionEntity() }
                )
            }
            launch {
                localIncomeRepo.mergeIncomes(
                    incomes.map { it.toIncomeEntity() }
                )
            }
        }
        
        Log.d("CashflowSync", "✅ syncAndFetchAll complete: ${allEntries.size} items")
        return allEntries
        
    } catch (e: Exception) {
        Log.e("CashflowSync", "❌ syncAndFetchAll failed", e)
        throw e
    }
}
```

#### Extension Functions

```kotlin
private fun CashflowEntry.toTransactionEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        category = category,
        description = description,
        date = date,
        icon = icon,
        accountId = accountId,
        groupId = groupId,
        synced = true
    )
}

private fun CashflowEntry.toIncomeEntity(): IncomeEntity {
    return IncomeEntity(
        id = id,
        amount = amount,
        category = category,
        description = description,
        date = date,
        icon = icon,
        accountId = accountId,
        groupId = groupId,
        synced = true
    )
}
```

---

## 🎨 ViewModel Layer

### File: `DashboardViewModel.kt`

#### Update `loadData()` Method

```kotlin
private suspend fun loadData() {
    withContext(Dispatchers.IO) {
        _uiState.update { it.copy(isSyncing = true) }
        
        try {
            val period = _uiState.value.selectedPeriod
            val (startDate, endDate) = period.dateRange()
            
            // Calculate month/year parameters
            val calendar = Calendar.getInstance().apply { time = startDate }
            val monthStr = when (period) {
                SpendingPeriod.THIS_MONTH,
                SpendingPeriod.LAST_MONTH,
                is SpendingPeriod.CUSTOM -> {
                    SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)
                }
                else -> null
            }
            val yearStr = when (period) {
                SpendingPeriod.THIS_YEAR -> calendar.get(Calendar.YEAR).toString()
                else -> null
            }
            
            coroutineScope {
                // === NEW: Sync ALL pages ===
                if (_uiState.value.isOnline) {
                    try {
                        Log.d("Dashboard", "🔄 Starting full sync for month: $monthStr, year: $yearStr")
                        cashflowSyncUseCase.syncAndFetchAll(
                            month = monthStr,
                            year = yearStr
                        )
                        Log.d("Dashboard", "✅ Full sync complete")
                    } catch (e: Exception) {
                        Log.e("Dashboard", "❌ Sync failed", e)
                        // Continue with local data
                    }
                }
                
                // Load from local DB (now complete!)
                val spendingTask = async { getTotalSpendingUseCase.execute(period) }
                val reportsTask = async { getSpendingReportUseCase.execute() }
                val historyTask = async {
                    cashflowSyncUseCase.loadFromLocal(startDate, endDate ?: Date()).take(5)
                }
                
                // ... rest of existing code
            }
        } finally {
            _uiState.update { it.copy(isSyncing = false) }
        }
    }
}
```

#### Add Sync Progress State (Optional)

```kotlin
data class DashboardState(
    // Existing fields...
    val syncProgress: SyncProgress = SyncProgress()
)

// Expose sync progress for UI
fun observeSyncProgress(): StateFlow<SyncProgress> = 
    cashflowSyncUseCase.syncProgressFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SyncProgress()
    )
```

---

## 🎯 Implementation Phases

### Phase 1: Repository Layer ✅
**Files**: 
- `CashflowRepository.kt` (interface)
- `CashflowRepositoryImpl.kt` (implementation)

**Tasks**:
1. [ ] Add `getHistoryAllPages()` to interface
2. [ ] Implement pagination loop in `CashflowRepositoryImpl`
3. [ ] Add logging for debugging
4. [ ] Add delay between requests (100ms)

**Testing**:
- [ ] Unit test with mock API (1 page)
- [ ] Unit test with mock API (3 pages)
- [ ] Integration test with real API

---

### Phase 2: UseCase Layer ✅
**Files**:
- `CashflowSyncUseCase.kt`

**Tasks**:
1. [ ] Add `syncAndFetchAll()` method
2. [ ] Split entries by type (expense/income)
3. [ ] Merge to local repositories
4. [ ] Add comprehensive logging
5. [ ] Handle empty response

**Testing**:
- [ ] Unit test merge logic
- [ ] Test with 0 items
- [ ] Test with 250 items
- [ ] Test error handling

---

### Phase 3: ViewModel Integration ✅
**Files**:
- `DashboardViewModel.kt`

**Tasks**:
1. [ ] Replace `syncAndFetch()` with `syncAndFetchAll()`
2. [ ] Pass month/year parameters correctly
3. [ ] Update loading states
4. [ ] Add error handling

**Testing**:
- [ ] Test THIS_MONTH filter
- [ ] Test LAST_MONTH filter
- [ ] Test THIS_YEAR filter
- [ ] Test with network error
- [ ] Verify local data fallback

---

### Phase 4: UI Enhancement (Optional) ⏸️
**Files**:
- `DashboardScreen.kt`

**Tasks**:
1. [ ] Show sync progress indicator
2. [ ] Display "Syncing X of Y pages"
3. [ ] Add pull-to-refresh with progress
4. [ ] Error toast with retry

**Deferred**: Can be added later if needed

---

## 🔍 Testing Strategy

### Unit Tests

#### Repository Test
```kotlin
@Test
fun `getHistoryAllPages fetches all pages`() = runTest {
    // Mock API to return 3 pages
    val page1 = CashflowHistoryResponse(
        entries = List(100) { mockEntry(it) },
        pagination = PaginationInfo(1, 100, 3, 250)
    )
    val page2 = CashflowHistoryResponse(
        entries = List(100) { mockEntry(it + 100) },
        pagination = PaginationInfo(2, 100, 3, 250)
    )
    val page3 = CashflowHistoryResponse(
        entries = List(50) { mockEntry(it + 200) },
        pagination = PaginationInfo(3, 100, 3, 250)
    )
    
    coEvery { api.getHistory(any(), any(), 1, 100) } returns page1
    coEvery { api.getHistory(any(), any(), 2, 100) } returns page2
    coEvery { api.getHistory(any(), any(), 3, 100) } returns page3
    
    val result = repository.getHistoryAllPages(null, null, 100)
    
    assertEquals(250, result.size)
    coVerify(exactly = 3) { api.getHistory(any(), any(), any(), 100) }
}
```

#### UseCase Test
```kotlin
@Test
fun `syncAndFetchAll merges expenses and incomes separately`() = runTest {
    val entries = listOf(
        mockExpense(1),
        mockExpense(2),
        mockIncome(3),
        mockIncome(4)
    )
    
    coEvery { repository.getHistoryAllPages(any(), any(), any()) } returns entries
    coEvery { localTransactionRepo.mergeTransactions(any()) } just Runs
    coEvery { localIncomeRepo.mergeIncomes(any()) } just Runs
    
    val result = useCase.syncAndFetchAll()
    
    assertEquals(4, result.size)
    coVerify { localTransactionRepo.mergeTransactions(match { it.size == 2 }) }
    coVerify { localIncomeRepo.mergeIncomes(match { it.size == 2 }) }
}
```

### Integration Tests

```kotlin
@Test
fun `full sync with real API fetches all pages`() = runTest {
    // This requires test backend or staging environment
    val result = repository.getHistoryAllPages(
        month = "2026-06",
        year = null,
        pageSize = 100
    )
    
    assertTrue(result.isNotEmpty())
    // Verify no duplicates
    assertEquals(result.size, result.distinctBy { it.id }.size)
}
```

### Manual Testing Checklist

- [ ] Dashboard with 0 transactions
- [ ] Dashboard with 50 transactions (1 page)
- [ ] Dashboard with 150 transactions (2 pages)
- [ ] Dashboard with 500 transactions (5 pages)
- [ ] Filter by THIS_MONTH with 250 transactions
- [ ] Filter by LAST_MONTH with 100 transactions
- [ ] Filter by THIS_YEAR with 1000 transactions
- [ ] Network error during page 2 fetch
- [ ] Offline mode (should use local data)
- [ ] Verify no May transactions in June filter
- [ ] Verify income/expense totals match backend

---

## 📊 Performance Considerations

### API Call Optimization

```
Scenario: 500 transactions
├── Current: 1 API call, 50 items = incomplete data
└── New: 5 API calls, 100 items each = complete data

Time Comparison:
├── Current: ~500ms (1 call)
└── New: ~2.5s (5 calls × 500ms)

Trade-off: +2 seconds for 100% data accuracy ✅ WORTH IT
```

### Memory Management

```kotlin
// Stream processing for very large datasets (future optimization)
suspend fun getHistoryAllPagesStreaming(
    month: String?,
    year: String?,
    pageSize: Int = 100,
    onPageFetched: suspend (List<CashflowEntry>) -> Unit
) {
    var currentPage = 1
    var totalPages = 1
    
    while (currentPage <= totalPages) {
        val response = getHistory(month, year, currentPage, pageSize)
        
        // Process each page immediately
        onPageFetched(response.entries)
        
        if (currentPage == 1) {
            totalPages = response.pagination?.totalPages ?: 1
        }
        currentPage++
    }
}
```

### Caching Strategy

```kotlin
// Cache sync timestamp per month
private val lastSyncCache = mutableMapOf<String, Long>()

fun shouldSync(month: String?): Boolean {
    val key = month ?: "all"
    val lastSync = lastSyncCache[key] ?: 0L
    val now = System.currentTimeMillis()
    
    // Only sync if > 5 minutes since last sync
    return (now - lastSync) > 5 * 60 * 1000
}
```

---

## 🚨 Error Handling

### Network Errors

```kotlin
try {
    cashflowSyncUseCase.syncAndFetchAll(month, year)
} catch (e: IOException) {
    Log.e("Sync", "Network error, using local data", e)
    // UI shows: "Using offline data"
} catch (e: Exception) {
    Log.e("Sync", "Sync failed", e)
    // UI shows: "Sync failed, tap to retry"
}
```

### Partial Sync

```kotlin
// If page 3 of 5 fails, we still save pages 1-2
var allEntries = mutableListOf<CashflowEntry>()
var lastSuccessfulPage = 0

for (page in 1..totalPages) {
    try {
        val response = getHistory(month, year, page, pageSize)
        allEntries.addAll(response.entries)
        lastSuccessfulPage = page
    } catch (e: Exception) {
        Log.w("Sync", "Failed to fetch page $page, stopping", e)
        break
    }
}

// Save whatever we got
if (allEntries.isNotEmpty()) {
    mergeToLocalDB(allEntries)
    Log.i("Sync", "Partial sync: $lastSuccessfulPage of $totalPages pages")
}
```

---

## 📝 Migration Notes

### Backward Compatibility

```kotlin
// Keep existing method for pagination UI
suspend fun syncAndFetch(
    month: String? = null,
    year: String? = null,
    page: Int = 1,
    pageSize: Int = 50
): CashflowHistoryResponse {
    // Existing implementation unchanged
    return cashflowRepository.getHistory(month, year, page, pageSize)
}

// New method for full sync
suspend fun syncAndFetchAll(
    month: String? = null,
    year: String? = null
): List<CashflowEntry> {
    // New implementation
    return cashflowRepository.getHistoryAllPages(month, year, 100)
}
```

### Rollback Plan

If issues occur:
1. Revert ViewModel changes (use `syncAndFetch()` again)
2. Keep new repository method for future use
3. Investigate and fix issues
4. Re-deploy with fixes

---

## 🎯 Success Criteria

1. **Data Completeness**: All transactions synced (verify in DB)
2. **Filtering Accuracy**: June filter shows only June transactions
3. **Total Accuracy**: Income/expense match backend within 0.01%
4. **Performance**: Full sync < 3s for 500 items
5. **Reliability**: No crashes, graceful error handling
6. **Logs**: Clear debug logs for troubleshooting

---

## 📚 References

- iOS implementation: `OPTIMIZED_SYNC_IMPLEMENTATION.md`
- API docs: `/cashflow/history` endpoint
- Existing code: `DashboardViewModel.kt`, `CashflowSyncUseCase.kt`
