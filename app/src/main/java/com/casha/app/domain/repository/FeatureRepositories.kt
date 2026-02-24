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
    suspend fun getTransactionsByCategory(category: String, startDate: java.util.Date, endDate: java.util.Date): List<TransactionCasha>
    suspend fun getCategorySpendings(startDate: java.util.Date, endDate: java.util.Date): List<com.casha.app.domain.model.ChartCategorySpending>
    
    // Command
    suspend fun saveTransaction(transaction: TransactionCasha)
    suspend fun updateTransaction(id: String, request: com.casha.app.data.remote.dto.UpdateTransactionDto)
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
    fun getIncomesFlow(): Flow<List<com.casha.app.domain.model.IncomeCasha>>
    suspend fun getIncomes(): List<com.casha.app.domain.model.IncomeCasha>
    suspend fun getSummary(period: String?): com.casha.app.domain.model.IncomeSummary
    suspend fun saveIncome(request: com.casha.app.domain.model.CreateIncomeRequest)
    suspend fun updateIncome(id: String, request: com.casha.app.domain.model.CreateIncomeRequest)
    suspend fun deleteIncome(id: String)
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
    suspend fun getAllCategories(): List<com.casha.app.domain.model.CategoryCasha>
    suspend fun createCategory(name: String, isActive: Boolean): com.casha.app.domain.model.CategoryCasha
    suspend fun updateCategory(id: String, name: String, isActive: Boolean): com.casha.app.domain.model.CategoryCasha
    suspend fun deleteCategory(id: String)
}

/**
 * Repository interface for Budget operations.
 * Separates remote and local operations to support offline-first architecture.
 */
interface BudgetRepository {
    // Remote
    suspend fun fetchRemoteBudgets(month: String? = null): List<com.casha.app.domain.model.BudgetCasha>
    suspend fun fetchRemoteSummary(month: String? = null): com.casha.app.domain.model.BudgetSummary
    suspend fun createRemoteBudget(request: com.casha.app.domain.model.NewBudgetRequest): com.casha.app.domain.model.BudgetCasha
    suspend fun updateRemoteBudget(id: String, request: com.casha.app.domain.model.NewBudgetRequest): com.casha.app.domain.model.BudgetCasha
    suspend fun deleteRemoteBudget(id: String)
    suspend fun fetchAIRecommendations(monthlyIncome: Double? = null): com.casha.app.domain.model.FinancialRecommendationResponse
    suspend fun applyRemoteRecommendations(request: com.casha.app.data.remote.dto.ApplyRecommendationsRequest): List<com.casha.app.domain.model.BudgetCasha>

    // Local
    suspend fun getLocalBudgets(month: String? = null): List<com.casha.app.domain.model.BudgetCasha>
    suspend fun saveLocalBudget(budget: com.casha.app.domain.model.BudgetCasha)
    suspend fun saveLocalBudgets(budgets: List<com.casha.app.domain.model.BudgetCasha>)
    suspend fun deleteLocalBudget(id: String)
    suspend fun getUnsyncedBudgets(): List<com.casha.app.domain.model.BudgetCasha>
    suspend fun markAsSynced(localId: String, remoteId: String)
    suspend fun clearLocalBudgets()
    fun calculateLocalSummary(budgets: List<com.casha.app.domain.model.BudgetCasha>): com.casha.app.domain.model.BudgetSummary
}
