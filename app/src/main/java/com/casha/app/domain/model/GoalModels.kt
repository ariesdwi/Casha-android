package com.casha.app.domain.model

import java.util.Date

data class GoalCategory(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val isActive: Boolean = true,
    val userId: String? = null
)

enum class GoalStatus {
    ACTIVE, COMPLETED, ARCHIVED, ON_HOLD
}

data class GoalProgress(
    val percentage: Double,
    val daysRemaining: Int? = null,
    val monthlySavingsNeeded: Double? = null
)

data class GoalContribution(
    val id: String,
    val goalId: String,
    val amount: Double,
    val note: String? = null,
    val datetime: Date
)

data class Goal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val currency: String = com.casha.app.core.util.CurrencyFormatter.defaultCurrency,
    val category: GoalCategory,
    val icon: String? = null,
    val color: String? = null,
    val deadline: Date? = null,
    val status: GoalStatus = GoalStatus.ACTIVE,
    val assetId: String? = null,
    val assetName: String? = null,
    val note: String? = null,
    val progress: GoalProgress,
    val recentContributions: List<GoalContribution> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

data class NearestDeadline(
    val name: String,
    val deadline: Date
)

data class GoalSummary(
    val totalGoals: Int,
    val activeGoals: Int,
    val completedGoals: Int,
    val totalTarget: Double,
    val totalCurrent: Double,
    val overallProgress: Double,
    val nearestDeadline: NearestDeadline? = null
)

data class CreateGoalRequest(
    val name: String,
    val targetAmount: Double,
    val category: GoalCategory,
    val deadline: Date?,
    val assetId: String?,
    val icon: String?,
    val color: String?,
    val note: String?
)
