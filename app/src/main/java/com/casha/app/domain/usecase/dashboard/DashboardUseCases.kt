package com.casha.app.domain.usecase.dashboard

import com.casha.app.domain.model.*
import com.casha.app.domain.repository.TransactionRepository
import com.casha.app.domain.repository.CashflowRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class GetTotalSpendingUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend fun execute(period: SpendingPeriod): Double {
        return repository.getTotalSpending(period)
    }
}

class GetSpendingReportUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend fun execute(): List<SpendingReport> {
        return listOf(repository.fetchSpendingReport())
    }
}

class GetUnsyncTransactionCountUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend fun execute(): Int {
        return repository.getUnsyncCount()
    }
}

class GetCashflowHistoryUseCase @Inject constructor(
    private val repository: CashflowRepository
) {
    suspend fun execute(
        month: String?,
        year: String?,
        page: Int,
        pageSize: Int
    ): CashflowHistoryResponse {
        return repository.getHistory(month, year, page, pageSize)
    }
}

data class CashflowHistoryResponse(
    val entries: List<CashflowEntry>
)

class GetCashflowSummaryUseCase @Inject constructor(
    private val repository: CashflowRepository
) {
    suspend fun execute(month: String?, year: String?): CashflowSummary {
        return repository.getSummary(month, year)
    }
}

class GetRecentTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend fun execute(limit: Int = 5): List<CashflowEntry> {
        val transactionsFlow = repository.getTransactions()
        val transactionsList = transactionsFlow.firstOrNull() ?: emptyList()
        
        return transactionsList.take(limit).map {
            CashflowEntry(
                id = it.id,
                title = it.name,
                amount = it.amount,
                category = it.category,
                type = CashflowType.EXPENSE,
                date = it.datetime
            )
        }
    }
}
