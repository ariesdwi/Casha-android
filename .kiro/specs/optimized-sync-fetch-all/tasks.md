# Implementation Tasks: Optimized Sync Fetch All

**Created**: 2026-06-09  
**Status**: Ready for Implementation

---

## 📋 Task Breakdown

### Phase 1: Repository Layer (Data Fetching)

#### Task 1.1: Update `CashflowRepository` Interface
**File**: `app/src/main/java/com/casha/app/domain/repository/CashflowRepository.kt`

**Changes**:
```kotlin
interface CashflowRepository {
    // Existing methods...
    
    /**
     * Fetch ALL pages of cashflow history
     * @param month Optional month filter (format: "2026-06")
     * @param year Optional year filter (format: "2026")
     * @param pageSize Number of items per page (default: 100)
     * @return Complete list of all entries across all pages
     */
    suspend fun getHistoryAllPages(
        month: String? = null,
        year: String? = null,
        pageSize: Int = 100
    ): List<CashflowEntry>
}
```

**Acceptance**:
- [x] Interface method added
- [x] KDoc comments added
- [x] Default parameters set
- [x] Build passes

---

#### Task 1.2: Implement `getHistoryAllPages()` in Repository
**File**: `app/src/main/java/com/casha/app/data/remote/impl/CashflowRepositoryImpl.kt`

**Implementation**:
```kotlin
override suspend fun getHistoryAllPages(
    month: String?,
    year: String?,
    pageSize: Int
): List<CashflowEntry> {
    val allEntries = mutableListOf<CashflowEntry>()
    var currentPage = 1
    var totalPages = 1
    
    Log.d(TAG, "🔄 Starting full sync - month: ${month ?: "all"}, year: ${year ?: "all"}")
    
    try {
        while (currentPage <= totalPages) {
            Log.d(TAG, "🔄 Fetching page $currentPage of $totalPages...")
            
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
                Log.d(TAG, "📊 Total pages to fetch: $totalPages")
            }
            
            Log.d(TAG, "✅ Fetched ${response.entries.size} items (total so far: ${allEntries.size})")
            
            currentPage++
            
            // Add small delay to avoid rate limiting
            if (currentPage <= totalPages) {
                kotlinx.coroutines.delay(100) // 100ms delay
            }
        }
        
        Log.d(TAG, "✅ Full sync complete: ${allEntries.size} total items")
        return allEntries
        
    } catch (e: Exception) {
        Log.e(TAG, "❌ Full sync failed at page $currentPage", e)
        // Return whatever we fetched so far (partial sync)
        if (allEntries.isNotEmpty()) {
            Log.w(TAG, "⚠️ Partial sync: returning ${allEntries.size} items from $currentPage pages")
            return allEntries
        }
        throw e
    }
}

companion object {
    private const val TAG = "CashflowRepository"
}
```

**Acceptance**:
- [x] Method implemented
- [ ] Pagination loop works correctly
- [x] Logging added for debugging
- [x] Delay between requests (100ms)
- [x] Partial sync handling
- [x] Error handling
- [x] Build passes

---

#### Task 1.3: Add Unit Tests for Repository
**File**: `app/src/test/java/com/casha/app/data/remote/impl/CashflowRepositoryImplTest.kt`

**Tests**:
```kotlin
@Test
fun `getHistoryAllPages fetches single page when totalPages is 1`() = runTest {
    // Given: API returns 1 page with 50 items
    val mockResponse = CashflowHistoryResponse(
        entries = List(50) { mockEntry(it) },
        pagination = PaginationInfo(1, 100, 1, 50)
    )
    coEvery { apiService.getHistory(any(), any(), 1, 100) } returns mockApiResponse(mockResponse)
    
    // When
    val result = repository.getHistoryAllPages(null, null, 100)
    
    // Then
    assertEquals(50, result.size)
    coVerify(exactly = 1) { apiService.getHistory(any(), any(), 1, 100) }
}

@Test
fun `getHistoryAllPages fetches all 3 pages`() = runTest {
    // Given: API returns 3 pages
    coEvery { apiService.getHistory(any(), any(), 1, 100) } returns mockPage(1, 100, 3, 250)
    coEvery { apiService.getHistory(any(), any(), 2, 100) } returns mockPage(2, 100, 3, 250)
    coEvery { apiService.getHistory(any(), any(), 3, 100) } returns mockPage(3, 50, 3, 250)
    
    // When
    val result = repository.getHistoryAllPages(null, null, 100)
    
    // Then
    assertEquals(250, result.size)
    coVerify(exactly = 1) { apiService.getHistory(any(), any(), 1, 100) }
    coVerify(exactly = 1) { apiService.getHistory(any(), any(), 2, 100) }
    coVerify(exactly = 1) { apiService.getHistory(any(), any(), 3, 100) }
}

@Test
fun `getHistoryAllPages with month filter passes parameter`() = runTest {
    // Given
    coEvery { apiService.getHistory("2026-06", null, 1, 100) } returns mockPage(1, 50, 1, 50)
    
    // When
    val result = repository.getHistoryAllPages(month = "2026-06", year = null, pageSize = 100)
    
    // Then
    coVerify { apiService.getHistory("2026-06", null, 1, 100) }
}

@Test
fun `getHistoryAllPages returns partial data on error`() = runTest {
    // Given: Page 1 succeeds, page 2 fails
    coEvery { apiService.getHistory(any(), any(), 1, 100) } returns mockPage(1, 100, 3, 250)
    coEvery { apiService.getHistory(any(), any(), 2, 100) } throws IOException("Network error")
    
    // When
    val result = repository.getHistoryAllPages(null, null, 100)
    
    // Then: Should return page 1 data
    assertEquals(100, result.size)
}
```

**Acceptance**:
- [~] Test for single page
- [~] Test for multiple pages
- [~] Test with month filter
- [~] Test with year filter
- [~] Test network error handling
- [~] Test partial sync
- [~] All tests pass

---

### Phase 2: UseCase Layer (Business Logic)

#### Task 2.1: Add `syncAndFetchAll()` to UseCase
**File**: `app/src/main/java/com/casha/app/domain/usecase/dashboard/CashflowSyncUseCase.kt`

**Implementation**:
```kotlin
/**
 * Sync and fetch ALL pages of cashflow data
 * @param month Optional month filter (format: "2026-06")
 * @param year Optional year filter (format: "2026")
 * @return Complete list of all entries
 */
suspend fun syncAndFetchAll(
    month: String? = null,
    year: String? = null
): List<CashflowEntry> {
    Log.d(TAG, "🔄 Starting syncAndFetchAll - month: ${month ?: "all"}, year: ${year ?: "all"}")
    
    try {
        // Fetch all pages from remote
        val allEntries = cashflowRepository.getHistoryAllPages(
            month = month,
            year = year,
            pageSize = 100
        )
        
        if (allEntries.isEmpty()) {
            Log.d(TAG, "ℹ️ No entries returned from API")
            return emptyList()
        }
        
        // Split by type
        val expenses = allEntries.filter { it.type == CashflowType.EXPENSE }
        val incomes = allEntries.filter { it.type == CashflowType.INCOME }
        
        Log.d(TAG, "💾 Merging to local DB: ${expenses.size} expenses, ${incomes.size} incomes")
        
        // Merge to local repositories in parallel
        withContext(Dispatchers.IO) {
            launch {
                if (expenses.isNotEmpty()) {
                    localTransactionRepo.mergeTransactions(
                        expenses.map { it.toTransactionEntity() }
                    )
                }
            }
            launch {
                if (incomes.isNotEmpty()) {
                    localIncomeRepo.mergeIncomes(
                        incomes.map { it.toIncomeEntity() }
                    )
                }
            }
        }
        
        Log.d(TAG, "✅ syncAndFetchAll complete: ${allEntries.size} items")
        return allEntries
        
    } catch (e: Exception) {
        Log.e(TAG, "❌ syncAndFetchAll failed", e)
        throw e
    }
}

// Extension functions for mapping
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

companion object {
    private const val TAG = "CashflowSyncUseCase"
}
```

**Acceptance**:
- [ ] Method implemented
- [~] Split entries by type
- [~] Parallel merge to local repos
- [~] Extension functions added
- [~] Logging added
- [ ] Error handling
- [ ] Build passes

---

#### Task 2.2: Add Unit Tests for UseCase
**File**: `app/src/test/java/com/casha/app/domain/usecase/dashboard/CashflowSyncUseCaseTest.kt`

**Tests**:
```kotlin
@Test
fun `syncAndFetchAll merges expenses and incomes separately`() = runTest {
    // Given
    val entries = listOf(
        mockCashflowEntry(id = "1", type = CashflowType.EXPENSE, amount = 100.0),
        mockCashflowEntry(id = "2", type = CashflowType.EXPENSE, amount = 200.0),
        mockCashflowEntry(id = "3", type = CashflowType.INCOME, amount = 500.0),
        mockCashflowEntry(id = "4", type = CashflowType.INCOME, amount = 300.0)
    )
    
    coEvery { cashflowRepository.getHistoryAllPages(any(), any(), any()) } returns entries
    coEvery { localTransactionRepo.mergeTransactions(any()) } just Runs
    coEvery { localIncomeRepo.mergeIncomes(any()) } just Runs
    
    // When
    val result = useCase.syncAndFetchAll()
    
    // Then
    assertEquals(4, result.size)
    coVerify { localTransactionRepo.mergeTransactions(match { it.size == 2 }) }
    coVerify { localIncomeRepo.mergeIncomes(match { it.size == 2 }) }
}

@Test
fun `syncAndFetchAll with month filter passes parameter`() = runTest {
    // Given
    coEvery { cashflowRepository.getHistoryAllPages("2026-06", null, 100) } returns emptyList()
    coEvery { localTransactionRepo.mergeTransactions(any()) } just Runs
    coEvery { localIncomeRepo.mergeIncomes(any()) } just Runs
    
    // When
    useCase.syncAndFetchAll(month = "2026-06")
    
    // Then
    coVerify { cashflowRepository.getHistoryAllPages("2026-06", null, 100) }
}

@Test
fun `syncAndFetchAll returns empty list when no entries`() = runTest {
    // Given
    coEvery { cashflowRepository.getHistoryAllPages(any(), any(), any()) } returns emptyList()
    
    // When
    val result = useCase.syncAndFetchAll()
    
    // Then
    assertTrue(result.isEmpty())
    coVerify(exactly = 0) { localTransactionRepo.mergeTransactions(any()) }
    coVerify(exactly = 0) { localIncomeRepo.mergeIncomes(any()) }
}
```

**Acceptance**:
- [~] Test merge logic
- [ ] Test with month filter
- [~] Test empty response
- [~] Test error handling
- [ ] All tests pass

---

### Phase 3: ViewModel Integration

#### Task 3.1: Update `DashboardViewModel.loadData()`
**File**: `app/src/main/java/com/casha/app/ui/feature/dashboard/DashboardViewModel.kt`

**Changes**:
```kotlin
private suspend fun loadData() {
    withContext(Dispatchers.IO) {
        _uiState.update { it.copy(isSyncing = true) }
        
        try {
            val period = _uiState.value.selectedPeriod
            val (startDate, endDate) = period.dateRange()
            
            // Calculate month/year parameters for API
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
            
            val periodLabel = getPeriodRawTitle(period)

            coroutineScope {
                // === CHANGED: Use syncAndFetchAll instead of syncAndFetch ===
                if (_uiState.value.isOnline) {
                    try {
                        Log.d(TAG, "🔄 Starting full sync for month: $monthStr, year: $yearStr")
                        cashflowSyncUseCase.syncAndFetchAll(
                            month = monthStr,
                            year = yearStr
                        )
                        Log.d(TAG, "✅ Full sync complete, refreshing dashboard...")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Full sync failed, using local data", e)
                        // Continue with local data
                    }
                }

                // Load all data in parallel (now from complete local DB)
                val spendingTask = async { getTotalSpendingUseCase.execute(period) }
                val reportsTask = async { getSpendingReportUseCase.execute() }
                val unsyncedTask = async { getUnsyncTransactionCountUseCase.execute() }
                
                val historyTask = async {
                    cashflowSyncUseCase.loadFromLocal(startDate, endDate ?: Date()).take(5)
                }
                
                val summaryTask = async {
                    val canUseRemote = monthStr != null || yearStr != null
                    if (_uiState.value.isOnline && canUseRemote) {
                        try {
                            getCashflowSummaryUseCase.execute(monthStr, yearStr)
                        } catch (_: Exception) {
                            cashflowSyncUseCase.calculateSummaryFromLocal(startDate, endDate ?: Date(), periodLabel)
                        }
                    } else {
                        cashflowSyncUseCase.calculateSummaryFromLocal(startDate, endDate ?: Date(), periodLabel)
                    }
                }
                
                val goalsTask = async { getGoalsUseCase.execute() }
                val goalSummaryTask = async { getGoalSummaryUseCase.execute() }
                val walletsTask = async { try { getWalletsUseCase.execute() } catch (_: Exception) { emptyList() } }
                val walletSummaryTask = async { try { getWalletSummaryUseCase.execute() } catch (_: Exception) { null } }
                val budgetAlertsTask = async { try { getBudgetAlertsUseCase(monthStr) } catch (_: Exception) { emptyList() } }

                _uiState.update { it.copy(
                    totalSpending = spendingTask.await(),
                    spendingReports = reportsTask.await(),
                    history = historyTask.await(),
                    summary = summaryTask.await(),
                    goals = goalsTask.await(),
                    goalSummary = goalSummaryTask.await(),
                    wallets = walletsTask.await(),
                    walletSummary = walletSummaryTask.await(),
                    budgetAlerts = budgetAlertsTask.await(),
                    unsyncedCount = unsyncedTask.await(),
                    monthLabel = getPeriodTitle(period),
                    selectedPeriod = period
                ) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load dashboard data", e)
            _uiState.update { it.copy(error = e.message) }
        } finally {
            _uiState.update { it.copy(isSyncing = false) }
        }
    }
}

companion object {
    private const val TAG = "DashboardViewModel"
}
```

**Acceptance**:
- [~] Replace `syncAndFetch()` with `syncAndFetchAll()`
- [~] Pass month/year parameters
- [~] Update loading state
- [ ] Error handling
- [ ] Logging added
- [ ] Build passes

---

#### Task 3.2: Test ViewModel Integration
**Manual Testing**:

**Test Case 1: THIS_MONTH Filter**
- [~] Open app
- [~] Select "This Month" filter
- [~] Pull to refresh
- [~] Verify: All June transactions shown
- [~] Verify: No May transactions
- [~] Verify: Income/expense totals correct

**Test Case 2: LAST_MONTH Filter**
- [~] Select "Last Month" filter
- [ ] Pull to refresh
- [~] Verify: All May transactions shown
- [~] Verify: No June transactions
- [~] Verify: Totals correct

**Test Case 3: THIS_YEAR Filter**
- [~] Select "This Year" filter
- [ ] Pull to refresh
- [~] Verify: All 2026 transactions shown
- [ ] Verify: Totals correct

**Test Case 4: Large Dataset**
- [~] Create 500+ transactions
- [ ] Pull to refresh
- [~] Verify: All transactions synced
- [~] Verify: Sync completes in < 3 seconds
- [~] Check logs for pagination

**Test Case 5: Network Error**
- [~] Disable network
- [ ] Pull to refresh
- [~] Verify: Error message shown
- [~] Verify: Local data still displayed
- [~] Enable network
- [~] Pull to refresh again
- [~] Verify: Sync succeeds

---

### Phase 4: Verification & Testing

#### Task 4.1: Database Verification
**File**: Manual database inspection

**Steps**:
1. [ ] Sync with large dataset (250+ items)
2. [ ] Use Database Inspector in Android Studio
3. [ ] Verify: `transactions` table has all expense records
4. [ ] Verify: `incomes` table has all income records
5. [ ] Verify: No duplicate entries
6. [ ] Verify: Dates are correct (no timezone issues)

---

#### Task 4.2: Log Analysis
**Steps**:
1. [ ] Enable debug logging
2. [ ] Trigger sync
3. [ ] Check logcat for:
   - [~] "🔄 Starting full sync" message
   - [~] "🔄 Fetching page X of Y" messages
   - [~] "✅ Full sync complete: N total items"
   - [~] "💾 Merging to local DB: X expenses, Y incomes"
   - [~] No error messages

---

#### Task 4.3: Performance Testing
**Steps**:
1. [ ] Measure sync time with 100 transactions (1 page)
   - Target: < 500ms
2. [ ] Measure sync time with 500 transactions (5 pages)
   - Target: < 3 seconds
3. [ ] Measure sync time with 1000 transactions (10 pages)
   - Target: < 5 seconds
4. [ ] Monitor memory usage during sync
   - Target: < 50MB increase

---

#### Task 4.4: Accuracy Testing
**Steps**:
1. [ ] Create test transactions with known totals
   - June 2026: 10 expenses (Rp 1,000,000 each) = Rp 10,000,000
   - June 2026: 5 incomes (Rp 2,000,000 each) = Rp 10,000,000
2. [ ] Sync and verify
   - Dashboard shows: Expense Rp 10,000,000 ✅
   - Dashboard shows: Income Rp 10,000,000 ✅
3. [ ] Add May transactions
   - May 2026: 5 expenses (Rp 500,000 each) = Rp 2,500,000
4. [ ] Filter by June
   - Dashboard shows: Expense Rp 10,000,000 (not Rp 12,500,000) ✅
   - No May transactions in list ✅

---

## 📊 Progress Tracking

### Phase 1: Repository Layer
- [~] Task 1.1: Update interface
- [~] Task 1.2: Implement method
- [~] Task 1.3: Unit tests

### Phase 2: UseCase Layer
- [~] Task 2.1: Add syncAndFetchAll
- [~] Task 2.2: Unit tests

### Phase 3: ViewModel Integration
- [~] Task 3.1: Update loadData
- [~] Task 3.2: Manual testing

### Phase 4: Verification
- [~] Task 4.1: Database verification
- [~] Task 4.2: Log analysis
- [~] Task 4.3: Performance testing
- [~] Task 4.4: Accuracy testing

---

## 🎯 Definition of Done

- [~] All unit tests passing
- [~] Manual tests completed successfully
- [~] No regressions in existing features
- [~] Performance targets met
- [~] Accuracy verified with test data
- [~] Code reviewed
- [~] Documentation updated
- [~] Logs cleaned up (no excessive logging)

---

## 🚀 Deployment Checklist

- [~] Feature flag enabled (if applicable)
- [~] Staged rollout plan (10% → 50% → 100%)
- [~] Monitoring dashboard set up
- [~] Rollback plan documented
- [~] Team notified
- [~] Release notes prepared

---

## 📝 Notes

- **Start with Phase 1** - Repository layer is foundation
- **Test each phase** - Don't move forward until tests pass
- **Monitor logs closely** - Pagination can be tricky
- **Performance matters** - Don't block UI thread
- **Accuracy is critical** - Verify totals match backend
