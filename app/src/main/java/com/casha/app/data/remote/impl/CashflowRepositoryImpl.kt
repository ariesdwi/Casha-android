package com.casha.app.data.remote.impl

import com.casha.app.data.remote.api.CashflowApiService
import com.casha.app.data.remote.dto.*
import com.casha.app.domain.model.*
import com.casha.app.domain.repository.CashflowRepository
import com.casha.app.domain.usecase.dashboard.CashflowHistoryResponse
import java.text.SimpleDateFormat
import java.util.*
import com.casha.app.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CashflowRepositoryImpl @Inject constructor(
    private val apiService: CashflowApiService
) : CashflowRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override suspend fun getHistory(month: String?, year: String?, page: Int, pageSize: Int): CashflowHistoryResponse {
        val result = safeApiCall { apiService.getHistory(month, year, page, pageSize) }
        return result.fold(
            onSuccess = { response -> 
                val entries = response.data?.items?.map { it.toDomain() } ?: emptyList()
                val pagination = response.data?.pagination
                android.util.Log.d(TAG, "getHistory page=$page: entries=${entries.size}, totalPages=${pagination?.totalPages}, totalItems=${pagination?.totalItems}")
                CashflowHistoryResponse(entries, pagination)
            },
            onFailure = { CashflowHistoryResponse(emptyList(), null) }
        )
    }

    override suspend fun getHistoryAllPages(
        month: String?,
        year: String?,
        pageSize: Int
    ): List<CashflowEntry> {
        val allEntries = mutableListOf<CashflowEntry>()
        var currentPage = 1
        var totalPages = 1
        
        android.util.Log.d(TAG, "🔄 Starting full sync - month: ${month ?: "all"}, year: ${year ?: "all"}")
        
        try {
            while (currentPage <= totalPages) {
                android.util.Log.d(TAG, "🔄 Fetching page $currentPage of $totalPages...")
                
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
                    android.util.Log.d(TAG, "📊 Total pages to fetch: $totalPages (${response.pagination?.totalItems ?: 0} items)")
                }
                
                android.util.Log.d(TAG, "✅ Fetched ${response.entries.size} items (total so far: ${allEntries.size})")
                
                currentPage++
                
                // Add small delay to avoid rate limiting
                if (currentPage <= totalPages) {
                    kotlinx.coroutines.delay(100) // 100ms delay
                }
            }
            
            android.util.Log.d(TAG, "✅ Full sync complete: ${allEntries.size} total items")
            return allEntries
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Full sync failed at page $currentPage", e)
            // Return whatever we fetched so far (partial sync)
            if (allEntries.isNotEmpty()) {
                android.util.Log.w(TAG, "⚠️ Partial sync: returning ${allEntries.size} items from ${currentPage - 1} pages")
                return allEntries
            }
            throw e
        }
    }

    override suspend fun getSummary(month: String?, year: String?): CashflowSummary {
        val result = safeApiCall { apiService.getSummary(month, year) }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() ?: CashflowSummary(0.0, 0.0, 0.0, "Current") },
            onFailure = { CashflowSummary(0.0, 0.0, 0.0, "Current") }
        )
    }

    override suspend fun getSafeSpendToday(): SafeSpendToday {
        val result = safeApiCall { apiService.getSafeSpendToday() }
        return result.fold(
            onSuccess = { response -> 
                response.data?.toSafeSpendDomain() ?: SafeSpendToday(
                    safeSpendToday = 0.0,
                    currency = "IDR",
                    daysRemaining = 0,
                    budgetPctUsed = 0,
                    monthlyIncome = 0.0,
                    spentSoFar = 0.0,
                    pendingObligations = 0.0,
                    freeRemaining = 0.0,
                    status = "",
                    statusLabel = ""
                )
            },
            onFailure = { 
                SafeSpendToday(
                    safeSpendToday = 0.0,
                    currency = "IDR",
                    daysRemaining = 0,
                    budgetPctUsed = 0,
                    monthlyIncome = 0.0,
                    spentSoFar = 0.0,
                    pendingObligations = 0.0,
                    freeRemaining = 0.0,
                    status = "",
                    statusLabel = ""
                )
            }
        )
    }

    private fun CashflowDto.toDomain() = CashflowEntry(
        id = id,
        title = name ?: source ?: "Untitled",
        amount = amount,
        category = category.ifEmpty { if (type.lowercase() == "income") incomeType ?: "Income" else "" },
        type = if (type.lowercase() == "income") CashflowType.INCOME else CashflowType.EXPENSE,
        date = try { dateFormat.parse(datetime) ?: Date() } catch (e: Exception) { Date() },
        icon = null,
        groupId = groupId,
        groupName = groupName
    )

    private fun CashflowSummaryDto.toDomain() = CashflowSummary(
        totalIncome = income?.total ?: 0.0,
        totalExpense = expense?.total ?: 0.0,
        netBalance = netCashflow,
        periodLabel = period,
        currency = currency,
        incomeBreakdown = income?.breakdown ?: emptyMap(),
        expenseBreakdown = expense?.breakdown ?: emptyMap(),
        liabilityBreakdown = liabilityPayment?.breakdown ?: emptyMap()
    )

    private fun SafeSpendTodayDto.toSafeSpendDomain() = SafeSpendToday(
        safeSpendToday = safeSpendToday,
        currency = currency,
        daysRemaining = daysRemaining,
        budgetPctUsed = budgetPctUsed,
        monthlyIncome = monthlyIncome,
        spentSoFar = spentSoFar,
        pendingObligations = pendingObligations,
        freeRemaining = freeRemaining,
        status = status,
        statusLabel = statusLabel
    )

    override suspend fun deleteGroup(groupId: String) {
        val result = safeApiCall { apiService.deleteExpenseGroup(groupId) }
        result.onFailure { throw it }
    }

    override suspend fun renameGroup(groupId: String, newName: String) {
        val result = safeApiCall {
            apiService.renameExpenseGroup(groupId, RenameGroupRequestDto(groupName = newName))
        }
        result.onFailure { throw it }
    }

    companion object {
        private const val TAG = "CashflowRepository"
    }
}
