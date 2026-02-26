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
                CashflowHistoryResponse(entries)
            },
            onFailure = { CashflowHistoryResponse(emptyList()) }
        )
    }

    override suspend fun getSummary(month: String?, year: String?): CashflowSummary {
        val result = safeApiCall { apiService.getSummary(month, year) }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() ?: CashflowSummary(0.0, 0.0, 0.0, "Current") },
            onFailure = { CashflowSummary(0.0, 0.0, 0.0, "Current") }
        )
    }

    private fun CashflowDto.toDomain() = CashflowEntry(
        id = id,
        title = name ?: source ?: "Untitled",
        amount = amount,
        category = category.ifEmpty { if (type.lowercase() == "income") incomeType ?: "Income" else "" },
        type = if (type.lowercase() == "income") CashflowType.INCOME else CashflowType.EXPENSE,
        date = try { dateFormat.parse(datetime) ?: Date() } catch (e: Exception) { Date() },
        icon = null // Icons could be derived from category later
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
}
