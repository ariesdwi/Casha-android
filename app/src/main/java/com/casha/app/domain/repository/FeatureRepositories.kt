package com.casha.app.domain.repository

import com.casha.app.domain.model.CashflowEntry
import com.casha.app.domain.model.CashflowSummary
import com.casha.app.domain.model.SpendingPeriod
import com.casha.app.domain.model.SpendingReport
import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.usecase.dashboard.CashflowHistoryResponse

/**
 * Repository interface for transaction-related data operations.
 */
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    // Queries
    fun getTransactions(): Flow<List<TransactionCasha>>
    suspend fun fetchTransactions()
    suspend fun getTransaction(id: String): TransactionCasha?
    suspend fun getTotalSpending(period: SpendingPeriod): Double
    suspend fun getUnsyncCount(): Int
    suspend fun fetchSpendingReport(): SpendingReport
    
    // Command
    suspend fun saveTransaction(transaction: TransactionCasha)
    suspend fun deleteTransaction(id: String)
    suspend fun syncTransactions()
}

/**
 * Repository interface for consolidated cashflow data (Incomes + Transactions).
 */
interface CashflowRepository {
    suspend fun getHistory(month: String?, year: String?, page: Int, pageSize: Int): CashflowHistoryResponse
    suspend fun getSummary(month: String?, year: String?): CashflowSummary
}

/**
 * Repository interface for Income operations.
 */
interface IncomeRepository {
    suspend fun getIncomes(): List<com.casha.app.domain.model.IncomeCasha>
    suspend fun getSummary(period: String?): com.casha.app.domain.model.IncomeSummary
}

/**
 * Repository interface for Goals operations.
 */
interface GoalRepository {
    suspend fun getGoals(): List<com.casha.app.domain.model.Goal>
    suspend fun getSummary(): com.casha.app.domain.model.GoalSummary
}

/**
 * Repository interface for expense category operations.
 * CRUD strategy: Create/Update/Delete = remote-first, Read = local-first.
 */
interface CategoryRepository {
    suspend fun getAllCategories(): List<com.casha.app.data.local.entity.CategoryEntity>
    suspend fun createCategory(name: String, isActive: Boolean): com.casha.app.data.local.entity.CategoryEntity
    suspend fun updateCategory(id: String, name: String, isActive: Boolean): com.casha.app.data.local.entity.CategoryEntity
    suspend fun deleteCategory(id: String)
}
