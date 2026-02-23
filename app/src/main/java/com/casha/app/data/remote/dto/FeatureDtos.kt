package com.casha.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CashflowDto(
    val id: String = "",
    val source: String? = null,
    val name: String? = null,
    val type: String = "", // "income" or "transaction"
    val amount: Double = 0.0,
    val currency: String? = null,
    val datetime: String = "",
    val category: String = "",
    val incomeType: String? = null,
    val direction: String? = null,
    val note: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CashflowPaginationDto(
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalItems: Int = 0,
    val totalPages: Int = 0
)

@Serializable
data class CashflowHistoryDataDto(
    val items: List<CashflowDto> = emptyList(),
    val pagination: CashflowPaginationDto? = null
)

@Serializable
data class CashflowHistoryResponseDto(
    val items: List<CashflowDto> = emptyList(),
    val pagination: CashflowPaginationDto? = null
)

@Serializable
data class CashflowTotalDto(
    val total: Double = 0.0,
    val count: Int = 0,
    val breakdown: Map<String, Double> = emptyMap()
)

@Serializable
data class CashflowSummaryDto(
    val period: String = "",
    val currency: String = "IDR",
    val income: CashflowTotalDto? = null,
    val expense: CashflowTotalDto? = null,
    @SerialName("liabilityPayment")
    val liabilityPayment: CashflowTotalDto? = null,
    val netCashflow: Double = 0.0,
    val savingsRate: Double = 0.0,
    val debtServiceRatio: Double = 0.0
)

@Serializable
data class CategoryDto(
    val id: String = "",
    val name: String = "",
    val isActive: Boolean = true,
    val userId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val isActive: Boolean = true
)

@Serializable
data class UpdateCategoryRequest(
    val name: String? = null,
    val isActive: Boolean? = null
)

@Serializable
data class IncomeDto(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val currency: String = "IDR",
    val datetime: String = "",
    val type: String = "",
    val source: String? = null,
    val frequency: String? = null,
    val isRecurring: Boolean = false,
    val note: String? = null,
    val assetId: String? = null,
    val assetName: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreateIncomeRequestDto(
    val id: String? = null,
    val name: String,
    val amount: Double,
    val type: String,
    val datetime: String,
    val source: String? = null,
    val isRecurring: Boolean = false,
    val frequency: String? = null,
    val note: String? = null,
    val assetId: String? = null
)

@Serializable
data class IncomeSummaryDto(
    val totalIncome: Double = 0.0,
    val period: String = "",
    val count: Int = 0
)

@Serializable
data class GoalCategoryDto(
    val id: String = "",
    val name: String = "",
    val icon: String = "🎯",
    val color: String = "#4CAF50",
    val isActive: Boolean = true,
    val userId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class GoalProgressDto(
    val percentage: Double = 0.0,
    val daysRemaining: Int? = null,
    val monthlySavingsNeeded: Double? = null
)

@Serializable
data class GoalContributionDto(
    val id: String = "",
    val goalId: String = "",
    val amount: Double = 0.0,
    val note: String? = null,
    val datetime: String = ""
)

@Serializable
data class GoalDto(
    val id: String = "",
    val name: String = "",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val currency: String = "IDR",
    val category: String? = null,
    val categoryId: String? = null,
    val categoryDetail: GoalCategoryDto? = null,
    val icon: String? = null,
    val color: String? = null,
    val deadline: String? = null,
    val status: String = "ACTIVE",
    val assetId: String? = null,
    val assetName: String? = null,
    val note: String? = null,
    val progress: GoalProgressDto? = null,
    val recentContributions: List<GoalContributionDto> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class NearestDeadlineDto(
    val name: String = "",
    val deadline: String = ""
)

@Serializable
data class GoalSummaryDto(
    val totalGoals: Int = 0,
    val activeGoals: Int = 0,
    val completedGoals: Int = 0,
    val totalTarget: Double = 0.0,
    val totalCurrent: Double = 0.0,
    val overallProgress: Double = 0.0,
    val nearestDeadline: NearestDeadlineDto? = null
)

// ── Budget DTOs ──

@Serializable
data class BudgetCategoryDto(
    val id: String = "",
    val name: String = ""
)

@Serializable
data class BudgetDto(
    val id: String = "",
    val amount: Double = 0.0,
    val spent: Double = 0.0,
    val remaining: Double = 0.0,
    val period: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val category: BudgetCategoryDto? = null,
    val currency: String? = null,
    val isSynced: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class BudgetSummaryDto(
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val currency: String = "IDR"
)

@Serializable
data class BudgetAIRecommendationDto(
    val id: String = "",
    val category: String = "",
    val suggestedAmount: Double = 0.0,
    val reasoning: String = ""
)

@Serializable
data class RecommendationSummaryDto(
    val strategy: String = "",
    val needsPercentage: Int = 0,
    val wantsPercentage: Int = 0,
    val savingsPercentage: Int = 0
)

@Serializable
data class FinancialSummaryDto(
    val debts: Double = 0.0,
    val assets: Double = 0.0
)

@Serializable
data class BudgetMilestoneDto(
    val title: String = "",
    val target: String = "",
    val action: String = ""
)

@Serializable
data class FinancialRecommendationResponseDto(
    val summary: RecommendationSummaryDto,
    val financialSummary: FinancialSummaryDto,
    val insights: List<String> = emptyList(),
    val budgetMilestones: List<BudgetMilestoneDto> = emptyList(),
    val recommendations: List<BudgetAIRecommendationDto> = emptyList()
)

@Serializable
data class NewBudgetRequestDto(
    val id: String? = null,
    val amount: Double,
    val month: String,
    val category: String
)

@Serializable
data class UpdateBudgetRequestDto(
    val amount: Double
)

@Serializable
data class RecommendedBudgetPayload(
    val amount: Double,
    val category: String
)

@Serializable
data class ApplyRecommendationsRequest(
    val month: String,
    val budgets: List<RecommendedBudgetPayload>
)

@Serializable
data class ApplyRecommendationsResponseDto(
    val status: String,
    val budgets: List<BudgetDto>
)
