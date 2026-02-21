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
    val source: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val datetime: String = "",
    val note: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
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
