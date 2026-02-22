package com.casha.app.data.remote.impl

import com.casha.app.data.local.dao.BudgetDao
import com.casha.app.data.local.entity.BudgetEntity
import com.casha.app.data.remote.api.BudgetApiService
import com.casha.app.data.remote.dto.*
import com.casha.app.domain.model.*
import com.casha.app.domain.repository.BudgetRepository
import com.casha.app.core.util.DateHelper
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Budget repository implementation.
 * Remote-first for CRUD, with local Room caching for offline support.
 */
@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val apiService: BudgetApiService,
    private val budgetDao: BudgetDao
) : BudgetRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // ── Remote Operations ──

    override suspend fun fetchRemoteBudgets(month: String?): List<BudgetCasha> {
        val response = apiService.getBudgets(month)
        return response.data?.map { it.toDomain(month) } ?: emptyList()
    }

    override suspend fun fetchRemoteSummary(month: String?): BudgetSummary {
        val response = apiService.getSummary(month)
        return response.data?.toDomain() ?: BudgetSummary(0.0, 0.0, 0.0, "USD")
    }

    override suspend fun createRemoteBudget(request: NewBudgetRequest): BudgetCasha {
        val response = apiService.addBudget(request.toDto())
        return response.data?.toDomain(request.month) ?: throw Exception("Failed to create budget")
    }

    override suspend fun updateRemoteBudget(id: String, request: NewBudgetRequest): BudgetCasha {
        val response = apiService.updateBudget(id, request.toUpdateDto())
        return response.data?.toDomain(request.month) ?: throw Exception("Failed to update budget")
    }

    override suspend fun deleteRemoteBudget(id: String) {
        apiService.deleteBudget(id)
    }

    override suspend fun fetchAIRecommendations(monthlyIncome: Double?): FinancialRecommendationResponse {
        val response = apiService.getAIRecommendations(monthlyIncome)
        return response.data?.toDomain() ?: FinancialRecommendationResponse(
            summary = RecommendationSummary("debt-free", 50, 30, 20),
            financialSummary = FinancialSummary(0.0, 0.0)
        )
    }

    override suspend fun applyRemoteRecommendations(request: ApplyRecommendationsRequest): List<BudgetCasha> {
        val response = apiService.applyRecommendations(request)
        return response.data?.budgets?.map { it.toDomain() } ?: emptyList()
    }

    // ── Local Operations ──

    override suspend fun getLocalBudgets(month: String?): List<BudgetCasha> {
        val entities = if (month != null) {
            budgetDao.getBudgetsByMonth(month)
        } else {
            budgetDao.getAllBudgets().first()
        }
        return entities.map { it.toDomain() }
    }

    override suspend fun saveLocalBudget(budget: BudgetCasha) {
        budgetDao.insertBudget(budget.toEntity())
    }

    override suspend fun saveLocalBudgets(budgets: List<BudgetCasha>) {
        budgetDao.insertBudgets(budgets.map { it.toEntity() })
    }

    override suspend fun deleteLocalBudget(id: String) {
        budgetDao.deleteById(id)
    }

    override suspend fun getUnsyncedBudgets(): List<BudgetCasha> {
        return budgetDao.getUnsyncedBudgets().map { it.toDomain() }
    }

    override suspend fun markAsSynced(localId: String, remoteId: String) {
        budgetDao.updateSyncStatus(localId, remoteId, true)
    }

    override suspend fun clearLocalBudgets() {
        budgetDao.clearAll()
    }

    override fun calculateLocalSummary(budgets: List<BudgetCasha>): BudgetSummary {
        val totalBudget = budgets.sumOf { it.amount }
        val totalSpent = budgets.sumOf { it.spent }
        val totalRemaining = budgets.sumOf { it.remaining }
        val currency = budgets.firstOrNull()?.currency ?: "USD"
        return BudgetSummary(totalBudget, totalSpent, totalRemaining, currency)
    }

    // ── Mappers ──

    private fun BudgetDto.toDomain(requestedMonth: String? = null) = BudgetCasha(
        id = id,
        amount = amount,
        spent = spent,
        remaining = remaining,
        period = requestedMonth ?: period,
        startDate = parseDate(startDate),
        endDate = parseDate(endDate),
        category = category?.name ?: "",
        currency = currency ?: "USD",
        isSynced = true,
        createdAt = parseDate(createdAt),
        updatedAt = parseDate(updatedAt)
    )

    private fun BudgetSummaryDto.toDomain() = BudgetSummary(
        totalBudget = totalBudget,
        totalSpent = totalSpent,
        totalRemaining = totalRemaining,
        currency = currency
    )

    private fun BudgetAIRecommendationDto.toDomain() = BudgetAIRecommendation(
        id = id,
        category = category,
        suggestedAmount = suggestedAmount,
        reasoning = reasoning
    )

    private fun RecommendationSummaryDto.toDomain() = RecommendationSummary(
        strategy = strategy,
        needsPercentage = needsPercentage,
        wantsPercentage = wantsPercentage,
        savingsPercentage = savingsPercentage
    )

    private fun FinancialSummaryDto.toDomain() = FinancialSummary(
        debts = debts,
        assets = assets
    )

    private fun BudgetMilestoneDto.toDomain() = BudgetMilestone(
        title = title,
        target = target,
        action = action
    )

    private fun FinancialRecommendationResponseDto.toDomain() = FinancialRecommendationResponse(
        summary = summary.toDomain(),
        financialSummary = financialSummary.toDomain(),
        insights = insights,
        budgetMilestones = budgetMilestones.map { it.toDomain() },
        recommendations = recommendations.map { it.toDomain() }
    )

    private fun NewBudgetRequest.toDto() = NewBudgetRequestDto(
        id = id,
        amount = amount,
        month = DateHelper.formatMonthYearDisplay(month),
        category = category
    )

    private fun NewBudgetRequest.toUpdateDto() = UpdateBudgetRequestDto(
        amount = amount
    )

    private fun BudgetCasha.toEntity() = BudgetEntity(
        id = id,
        amount = amount,
        spent = spent,
        remaining = remaining,
        period = period,
        startDate = startDate,
        endDate = endDate,
        category = category,
        currency = currency,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun BudgetEntity.toDomain() = BudgetCasha(
        id = id,
        amount = amount,
        spent = spent,
        remaining = remaining,
        period = period,
        startDate = startDate,
        endDate = endDate,
        category = category,
        currency = currency,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun parseDate(dateString: String?): Date {
        return try { dateString?.let { dateFormat.parse(it) } ?: Date() } catch (_: Exception) { Date() }
    }
}
