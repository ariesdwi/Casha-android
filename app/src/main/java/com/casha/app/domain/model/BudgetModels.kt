package com.casha.app.domain.model

import java.util.Date

/**
 * Main budget domain model.
 */
data class BudgetCasha(
    val id: String,
    val amount: Double,
    val spent: Double,
    val remaining: Double,
    val period: String,
    val startDate: Date,
    val endDate: Date,
    val category: String,
    val currency: String,
    val isSynced: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Aggregated budget summary.
 */
data class BudgetSummary(
    val totalBudget: Double,
    val totalSpent: Double,
    val totalRemaining: Double,
    val currency: String
)

/**
 * Request body for creating or updating a budget.
 */
@kotlinx.serialization.Serializable
data class NewBudgetRequest(
    val id: String? = null,
    val amount: Double,
    val month: String,
    val category: String
)

/**
 * AI-suggested budget recommendation.
 */
data class BudgetAIRecommendation(
    val id: String,
    val category: String,
    val suggestedAmount: Double,
    val reasoning: String
)

data class RecommendationSummary(
    val strategy: String,
    val needsPercentage: Int,
    val wantsPercentage: Int,
    val savingsPercentage: Int
)

data class FinancialSummary(
    val debts: Double,
    val assets: Double
)

data class BudgetMilestone(
    val title: String,
    val target: String,
    val action: String
)

/**
 * Response model for AI-powered financial recommendations.
 */
data class FinancialRecommendationResponse(
    val summary: RecommendationSummary,
    val financialSummary: FinancialSummary,
    val insights: List<String> = emptyList(),
    val budgetMilestones: List<BudgetMilestone> = emptyList(),
    val recommendations: List<BudgetAIRecommendation> = emptyList()
)
