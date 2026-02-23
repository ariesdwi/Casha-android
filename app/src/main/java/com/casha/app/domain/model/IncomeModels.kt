package com.casha.app.domain.model

import java.util.Date

data class IncomeCasha(
    val id: String,
    val name: String,
    val amount: Double,
    val datetime: Date,
    val type: IncomeType,
    val source: String?,
    val assetId: String?,
    val isRecurring: Boolean,
    val frequency: IncomeFrequency?,
    val note: String?,
    val createdAt: Date,
    val updatedAt: Date
)

enum class IncomeType {
    SALARY, FREELANCE, BUSINESS, INVESTMENT, GIFT, REFUND, OTHER
}

enum class IncomeFrequency {
    DAILY, WEEKLY, BIWEEKLY, MONTHLY, YEARLY
}

data class CreateIncomeRequest(
    val name: String,
    val amount: Double,
    val datetime: Date,
    val type: IncomeType,
    val source: String? = null,
    val assetId: String? = null,
    val isRecurring: Boolean = false,
    val frequency: IncomeFrequency? = null,
    val note: String? = null
)

data class IncomeSummary(
    val totalIncome: Double,
    val count: Int,
    val byType: List<IncomeTypeBreakdown>
)

data class IncomeTypeBreakdown(
    val type: IncomeType,
    val total: Double,
    val count: Int
)
