package com.casha.app.data.remote.impl

import com.casha.app.data.remote.api.CashflowApiService
import com.casha.app.data.remote.dto.*
import com.casha.app.domain.model.CashflowEntry
import com.casha.app.domain.model.CashflowType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.Date

/**
 * Unit tests for CashflowRepositoryImpl.getHistoryAllPages()
 *
 * Tests verify:
 * 1. Single page pagination (totalPages = 1)
 * 2. Multiple page pagination (3 pages)
 * 3. Month filter parameter passing
 * 4. Year filter parameter passing
 * 5. Network error handling
 * 6. Partial sync (some pages succeed, others fail)
 */
class CashflowRepositoryImplTest {

    private lateinit var repository: CashflowRepositoryImpl
    private lateinit var fakeApiService: FakeCashflowApiService

    @Before
    fun setup() {
        fakeApiService = FakeCashflowApiService()
        repository = CashflowRepositoryImpl(fakeApiService)
    }

    /**
     * Test 1: Single page (totalPages = 1)
     * Verify that when API returns only 1 page, repository makes exactly 1 API call
     */
    @Test
    fun `getHistoryAllPages fetches single page when totalPages is 1`() = runTest {
        // Given: API returns 1 page with 50 items
        val items = createMockCashflowDtos(50)
        val pagination = CashflowPaginationDto(
            page = 1,
            pageSize = 100,
            totalItems = 50,
            totalPages = 1
        )
        fakeApiService.configurePage(1, items, pagination)

        // When
        val result = repository.getHistoryAllPages(null, null, 100)

        // Then
        assertEquals(50, result.size)
        assertEquals(1, fakeApiService.callCount)
        assertEquals(listOf(1), fakeApiService.pagesRequested)
    }

    /**
     * Test 2: Multiple pages (3 pages)
     * Verify that repository fetches all 3 pages and aggregates results
     */
    @Test
    fun `getHistoryAllPages fetches all 3 pages`() = runTest {
        // Given: API returns 3 pages
        fakeApiService.configurePage(
            page = 1,
            items = createMockCashflowDtos(100, startId = 1),
            pagination = CashflowPaginationDto(page = 1, pageSize = 100, totalItems = 250, totalPages = 3)
        )
        fakeApiService.configurePage(
            page = 2,
            items = createMockCashflowDtos(100, startId = 101),
            pagination = CashflowPaginationDto(page = 2, pageSize = 100, totalItems = 250, totalPages = 3)
        )
        fakeApiService.configurePage(
            page = 3,
            items = createMockCashflowDtos(50, startId = 201),
            pagination = CashflowPaginationDto(page = 3, pageSize = 100, totalItems = 250, totalPages = 3)
        )

        // When
        val result = repository.getHistoryAllPages(null, null, 100)

        // Then
        // Main assertion: total items should be 250
        assertEquals("Should fetch all 250 items from 3 pages", 250, result.size)
        
        // Verify only 3 API calls were made
        assertEquals("Should make exactly 3 API calls", 3, fakeApiService.callCount)
        
        // Verify pages 1, 2, 3 were requested
        assertEquals("Should request pages 1, 2, 3", listOf(1, 2, 3), fakeApiService.pagesRequested)
        
        // Verify IDs are in sequence (verifying all pages aggregated correctly)
        assertEquals("First item should be ID 1", "1", result[0].id)
        assertEquals("Item at index 100 should be ID 101", "101", result[100].id)
        assertEquals("Item at index 200 should be ID 201", "201", result[200].id)
    }

    /**
     * Test 3: Month filter parameter
     * Verify that month parameter is passed correctly to API
     */
    @Test
    fun `getHistoryAllPages with month filter passes parameter`() = runTest {
        // Given
        val items = createMockCashflowDtos(50)
        val pagination = CashflowPaginationDto(page = 1, pageSize = 100, totalItems = 50, totalPages = 1)
        fakeApiService.configurePage(1, items, pagination)

        // When
        val result = repository.getHistoryAllPages(month = "2026-06", year = null, pageSize = 100)

        // Then
        assertTrue(result.isNotEmpty())
        assertEquals("2026-06", fakeApiService.lastMonthParam)
        assertNull(fakeApiService.lastYearParam)
    }

    /**
     * Test 4: Year filter parameter
     * Verify that year parameter is passed correctly to API
     */
    @Test
    fun `getHistoryAllPages with year filter passes parameter`() = runTest {
        // Given
        val items = createMockCashflowDtos(50)
        val pagination = CashflowPaginationDto(page = 1, pageSize = 100, totalItems = 50, totalPages = 1)
        fakeApiService.configurePage(1, items, pagination)

        // When
        val result = repository.getHistoryAllPages(month = null, year = "2026", pageSize = 100)

        // Then
        assertTrue(result.isNotEmpty())
        assertNull(fakeApiService.lastMonthParam)
        assertEquals("2026", fakeApiService.lastYearParam)
    }

    /**
     * Test 5: Network error handling
     * Verify that when API throws IOException, repository propagates the error
     */
    @Test(expected = IOException::class)
    fun `getHistoryAllPages throws exception on network error`() = runTest {
        // Given: API throws IOException on first call
        fakeApiService.shouldThrowError = true
        fakeApiService.errorToThrow = IOException("Network error")

        // When: Should throw IOException
        repository.getHistoryAllPages(null, null, 100)
    }

    /**
     * Test 6: Partial sync (page 1 succeeds, page 2 fails)
     * Verify that repository returns page 1 data when page 2 fails
     */
    @Test
    fun `getHistoryAllPages returns partial data when second page fails`() = runTest {
        // Given: Page 1 succeeds with totalPages=3, but page 2 will fail
        fakeApiService.configurePage(
            page = 1,
            items = createMockCashflowDtos(100, startId = 1),
            pagination = CashflowPaginationDto(1, 100, 3, 250)
        )
        fakeApiService.failOnPage = 2
        fakeApiService.errorToThrow = IOException("Network timeout")

        // When
        val result = repository.getHistoryAllPages(null, null, 100)

        // Then: Should return page 1 data only
        assertEquals(100, result.size)
        assertEquals("1", result[0].id)
        assertEquals("100", result[99].id)
        
        // Verify it attempted to fetch page 2
        assertTrue(fakeApiService.pagesRequested.contains(1))
        assertTrue(fakeApiService.pagesRequested.contains(2))
        assertEquals(2, fakeApiService.callCount)
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helper Functions
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Create mock CashflowDto objects for testing
     */
    private fun createMockCashflowDtos(count: Int, startId: Int = 1): List<CashflowDto> {
        return (0 until count).map { index ->
            val id = startId + index
            CashflowDto(
                id = id.toString(),
                name = "Transaction $id",
                type = if (id % 2 == 0) "transaction" else "income",
                amount = 100.0 * id,
                currency = "IDR",
                datetime = "2026-06-${(id % 28) + 1}T10:00:00.000Z",
                category = if (id % 2 == 0) "Food" else "Salary",
                incomeType = if (id % 2 == 0) null else "Salary"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Fake API Service
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Fake implementation of CashflowApiService for testing
     * Allows configuring responses per page and simulating errors
     */
    private class FakeCashflowApiService : CashflowApiService {
        private val pageResponses = mutableMapOf<Int, Pair<List<CashflowDto>, CashflowPaginationDto>>()
        
        var callCount = 0
        val pagesRequested = mutableListOf<Int>()
        var lastMonthParam: String? = null
        var lastYearParam: String? = null
        
        var shouldThrowError = false
        var failOnPage: Int? = null
        var errorToThrow: Exception = IOException("Network error")

        fun configurePage(page: Int, items: List<CashflowDto>, pagination: CashflowPaginationDto) {
            pageResponses[page] = Pair(items, pagination)
        }

        override suspend fun getHistory(
            month: String?,
            year: String?,
            page: Int?,
            pageSize: Int?
        ): BaseResponse<CashflowHistoryResponseDto> {
            val actualPage = page ?: 1
            callCount++
            pagesRequested.add(actualPage)
            lastMonthParam = month
            lastYearParam = year

            // Simulate error on first call if configured
            if (shouldThrowError && callCount == 1) {
                throw errorToThrow
            }

            // Simulate error on specific page if configured
            if (failOnPage != null && actualPage == failOnPage) {
                throw errorToThrow
            }

            val (items, pagination) = pageResponses[actualPage]
                ?: throw IllegalStateException("No response configured for page $actualPage")

            return BaseResponse(
                code = 200,
                status = "success",
                message = "OK",
                data = CashflowHistoryResponseDto(
                    items = items,
                    pagination = pagination
                )
            )
        }

        // Implement other methods (not used in these tests)
        override suspend fun getSummary(month: String?, year: String?): BaseResponse<CashflowSummaryDto> {
            throw NotImplementedError("Not used in these tests")
        }

        override suspend fun getSafeSpendToday(): BaseResponse<SafeSpendTodayDto> {
            throw NotImplementedError("Not used in these tests")
        }

        override suspend fun updateCashflow(
            type: String,
            id: String,
            request: UpdateTransactionDto
        ): BaseResponse<CashflowDto> {
            throw NotImplementedError("Not used in these tests")
        }

        override suspend fun createTransaction(request: TransactionUploadDto): BaseResponse<CashflowDto> {
            throw NotImplementedError("Not used in these tests")
        }

        override suspend fun deleteCashflow(type: String, id: String): BaseResponse<Unit> {
            throw NotImplementedError("Not used in these tests")
        }

        override suspend fun deleteExpenseGroup(groupId: String): BaseResponse<Unit> {
            throw NotImplementedError("Not used in these tests")
        }

        override suspend fun renameExpenseGroup(
            groupId: String,
            request: RenameGroupRequestDto
        ): BaseResponse<Unit> {
            throw NotImplementedError("Not used in these tests")
        }
    }
}
