package com.casha.app.domain.model

import java.util.Date

/**
 * Core domain model for a transaction in Casha.
 */
data class TransactionCasha(
    val id: String,
    val name: String,
    val category: String,
    val amount: Double,
    val datetime: Date,
    val note: String? = null,
    val isSynced: Boolean = false,
    val remoteId: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val liabilityId: String? = null,
    val groupId: String? = null,
    val groupName: String? = null,
    val assetId: String? = null
)

/**
 * Request model for creating or updating a transaction.
 */
data class TransactionRequest(
    val name: String,
    val category: String,
    val amount: Double,
    val datetime: Date,
    val note: String? = null,
    val assetId: String? = null
)

/**
 * Request model for partially updating an existing transaction.
 */
data class UpdateTransactionRequest(
    val name: String,
    val amount: Double,
    val category: String?,
    val datetime: String
)

enum class ChatParseIntent(val rawValue: String) {
    EXPENSE("EXPENSE"),
    INCOME("INCOME"),
    PAYMENT("PAYMENT"),
    MULTI_EXPENSE("MULTI_EXPENSE"),
    WHAT_IF("WHAT_IF"),
    FINANCIAL_SUMMARY("FINANCIAL_SUMMARY"),
    BUDGET_RECOMMENDATION("BUDGET_RECOMMENDATION"),
    UNKNOWN("UNKNOWN")
}

data class MultiExpenseSummary(
    val groupId: String,
    val groupName: String,
    val count: Int,
    val total: Double,
    val currency: String
)

data class ChatParseResult(
    val intent: ChatParseIntent,
    val message: String,
    val expenses: List<TransactionCasha>? = null,
    val summary: MultiExpenseSummary? = null,
    val whatIfSimulation: WhatIfSimulation? = null,
    val budgetRecommendation: BudgetRecommendationData? = null
)

// ─── Budget Recommendation Models ────────────────────────────────────────────

data class BudgetRecommendationData(
    val title: String,
    val summary: String,
    val monthlyIncome: Double,
    val totalDebtObligation: Double,
    val freeCashflow: Double,
    val recommendedBudgets: List<RecommendedBudget>,
    val debtPayoffPlan: DebtPayoffPlan?,
    val coachingNote: String
)

data class RecommendedBudget(
    val category: String,
    val amount: Double,
    val percentage: Double,
    val priority: String,
    val note: String
)

data class DebtPayoffPlan(
    val strategy: String,
    val loans: List<LoanPayoff>,
    val extraPaymentSuggestion: Double,
    val estimatedMonthsToDebtFree: Int
)

data class LoanPayoff(
    val name: String,
    val balance: Double,
    val monthlyPayment: Double,
    val monthsToPayoff: Int
)
