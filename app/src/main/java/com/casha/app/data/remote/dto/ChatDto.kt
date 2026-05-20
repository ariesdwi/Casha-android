package com.casha.app.data.remote.dto

import com.casha.app.domain.model.WhatIfAlternative
import com.casha.app.domain.model.WhatIfApplyAction
import com.casha.app.domain.model.WhatIfBudgetImpact
import com.casha.app.domain.model.WhatIfCashflowImpact
import com.casha.app.domain.model.WhatIfFinancialHealth
import com.casha.app.domain.model.WhatIfGoalImpact
import com.casha.app.domain.model.WhatIfHealthBreakdown
import com.casha.app.domain.model.WhatIfSeverity
import com.casha.app.domain.model.WhatIfSimulation
import com.casha.app.domain.model.WhatIfVerdict
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Serializable
data class ChatRequestDto(
    val input: String
)

@Serializable
data class ChatResponseDto(
    val code: Int,
    val status: String,
    val message: String,
    val data: ChatParseDataDto? = null
)

@Serializable
data class ChatParseDataDto(
    val intent: String,
    val data: JsonElement,
    val message: String? = null,
    val summary: MultiExpenseSummaryDto? = null
)

@Serializable
data class ChatTransactionDto(
    val id: String = "",
    val name: String = "",
    val category: JsonElement? = null,
    val amount: Double = 0.0,
    val currency: String? = null,
    val datetime: String = "",
    val note: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val groupId: String? = null,
    val groupName: String? = null
) {
    /** Extract category name whether the field is a plain string or {"id":…,"name":…} object. */
    val categoryName: String
        get() = when {
            category == null -> ""
            category is JsonPrimitive -> (category as JsonPrimitive).contentOrNull ?: ""
            category is JsonObject -> {
                (category as JsonObject)["name"]
                    ?.let { (it as? JsonPrimitive)?.contentOrNull }
                    ?: ""
            }
            else -> ""
        }
}

@Serializable
data class MultiExpenseSummaryDto(
    val groupId: String = "",
    val groupName: String = "",
    val count: Int = 0,
    val total: Double = 0.0,
    val currency: String = ""
)

@Serializable
data class ChatIncomeDto(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val currency: String? = null,
    val datetime: String = "",
    val type: String? = null,
    val source: String? = null,
    val frequency: String? = null,
    val isRecurring: Boolean = false,
    val note: String? = null,
    val assetId: String? = null,
    val assetName: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// ─── What If DTOs ────────────────────────────────────────────────────────────

@Serializable
data class WhatIfDataDto(
    val title: String = "",
    val subtitle: String = "",
    val cashflow: WhatIfCashflowDto = WhatIfCashflowDto(),
    @SerialName("financial_health") val financialHealth: WhatIfFinancialHealthDto? = null,
    @SerialName("impacted_budgets") val impactedBudgets: List<WhatIfBudgetImpactDto> = emptyList(),
    @SerialName("goal_impacts") val goalImpacts: List<WhatIfGoalImpactDto> = emptyList(),
    val verdict: WhatIfVerdictDto = WhatIfVerdictDto(),
    @SerialName("apply_actions") val applyActions: List<WhatIfApplyActionDto> = emptyList()
)

@Serializable
data class WhatIfCashflowDto(
    val before: Double = 0.0,
    val after: Double = 0.0,
    val severity: String = "warning"
)

@Serializable
data class WhatIfFinancialHealthDto(
    val score: Int = 0,
    val label: String = "",
    val breakdown: WhatIfHealthBreakdownDto? = null
)

@Serializable
data class WhatIfHealthBreakdownDto(
    val dtiScore: Int = 0,
    val cashflowScore: Int = 0,
    val emergencyFundScore: Int = 0,
    val goalProgressScore: Int = 0
)

@Serializable
data class WhatIfBudgetImpactDto(
    val category: String = "",
    val before: Double = 0.0,
    val after: Double = 0.0,
    val severity: String = "warning"
)

@Serializable
data class WhatIfGoalImpactDto(
    val name: String = "",
    @SerialName("delay_months") val delayMonths: Int = 0,
    @SerialName("original_date") val originalDate: String? = null,
    @SerialName("new_date") val newDate: String? = null
)

@Serializable
data class WhatIfVerdictDto(
    val severity: String = "warning",
    val headline: String = "",
    val message: String = "",
    @SerialName("coaching_note") val coachingNote: String? = null,
    val urgency: String? = null,
    val alternatives: List<WhatIfAlternativeDto> = emptyList()
)

@Serializable
data class WhatIfAlternativeDto(
    val label: String = "",
    val impact: String = "",
    @SerialName("action_type") val actionType: String = "",
    val priority: Int = 0
)

@Serializable
data class WhatIfApplyActionDto(
    val type: String = "",
    val params: JsonObject = JsonObject(emptyMap())
)

fun WhatIfDataDto.toDomain(): WhatIfSimulation = WhatIfSimulation(
    title = title,
    subtitle = subtitle,
    cashflow = WhatIfCashflowImpact(
        before = cashflow.before,
        after = cashflow.after,
        severity = WhatIfSeverity.from(cashflow.severity)
    ),
    financialHealth = financialHealth?.let {
        WhatIfFinancialHealth(
            score = it.score,
            label = it.label,
            breakdown = it.breakdown?.let { b ->
                WhatIfHealthBreakdown(
                    dtiScore = b.dtiScore,
                    cashflowScore = b.cashflowScore,
                    emergencyFundScore = b.emergencyFundScore,
                    goalProgressScore = b.goalProgressScore
                )
            }
        )
    },
    impactedBudgets = impactedBudgets.map { b ->
        WhatIfBudgetImpact(
            category = b.category,
            before = b.before,
            after = b.after,
            severity = WhatIfSeverity.from(b.severity)
        )
    },
    goalImpacts = goalImpacts.map { g ->
        WhatIfGoalImpact(
            name = g.name,
            delayMonths = g.delayMonths,
            originalDate = g.originalDate,
            newDate = g.newDate
        )
    },
    verdict = WhatIfVerdict(
        severity = WhatIfSeverity.from(verdict.severity),
        headline = verdict.headline,
        message = verdict.message,
        coachingNote = verdict.coachingNote,
        urgency = verdict.urgency,
        alternatives = verdict.alternatives.map { a ->
            WhatIfAlternative(
                label = a.label,
                impact = a.impact,
                actionType = a.actionType,
                priority = a.priority
            )
        }
    ),
    applyActions = applyActions.map { a ->
        WhatIfApplyAction(
            type = a.type,
            params = a.params.entries.associate { (k, v) ->
                k to ((v as? JsonPrimitive)?.contentOrNull ?: v.toString())
            }
        )
    }
)

// ─── Budget Recommendation DTOs ──────────────────────────────────────────────

@Serializable
data class BudgetRecommendationDataDto(
    val title: String = "",
    val summary: String = "",
    @SerialName("monthly_income") val monthlyIncome: Double = 0.0,
    @SerialName("total_debt_obligation") val totalDebtObligation: Double = 0.0,
    @SerialName("free_cashflow") val freeCashflow: Double = 0.0,
    @SerialName("recommended_budgets") val recommendedBudgets: List<RecommendedBudgetDto> = emptyList(),
    @SerialName("debt_payoff_plan") val debtPayoffPlan: DebtPayoffPlanDto? = null,
    @SerialName("coaching_note") val coachingNote: String = ""
)

@Serializable
data class RecommendedBudgetDto(
    val category: String = "",
    val amount: Double = 0.0,
    val percentage: Double = 0.0,
    val priority: String = "",
    val note: String = ""
)

@Serializable
data class DebtPayoffPlanDto(
    val strategy: String = "",
    val loans: List<LoanPayoffDto> = emptyList(),
    @SerialName("extra_payment_suggestion") val extraPaymentSuggestion: Double = 0.0,
    @SerialName("estimated_months_to_debt_free") val estimatedMonthsToDebtFree: Int = 0
)

@Serializable
data class LoanPayoffDto(
    val name: String = "",
    val balance: Double = 0.0,
    @SerialName("monthly_payment") val monthlyPayment: Double = 0.0,
    @SerialName("months_to_payoff") val monthsToPayoff: Int = 0
)

fun BudgetRecommendationDataDto.toDomain(): com.casha.app.domain.model.BudgetRecommendationData =
    com.casha.app.domain.model.BudgetRecommendationData(
        title = title,
        summary = summary,
        monthlyIncome = monthlyIncome,
        totalDebtObligation = totalDebtObligation,
        freeCashflow = freeCashflow,
        recommendedBudgets = recommendedBudgets.map { it.toDomain() },
        debtPayoffPlan = debtPayoffPlan?.toDomain(),
        coachingNote = coachingNote
    )

fun RecommendedBudgetDto.toDomain(): com.casha.app.domain.model.RecommendedBudget =
    com.casha.app.domain.model.RecommendedBudget(
        category = category,
        amount = amount,
        percentage = percentage,
        priority = priority,
        note = note
    )

fun DebtPayoffPlanDto.toDomain(): com.casha.app.domain.model.DebtPayoffPlan =
    com.casha.app.domain.model.DebtPayoffPlan(
        strategy = strategy,
        loans = loans.map { it.toDomain() },
        extraPaymentSuggestion = extraPaymentSuggestion,
        estimatedMonthsToDebtFree = estimatedMonthsToDebtFree
    )

fun LoanPayoffDto.toDomain(): com.casha.app.domain.model.LoanPayoff =
    com.casha.app.domain.model.LoanPayoff(
        name = name,
        balance = balance,
        monthlyPayment = monthlyPayment,
        monthsToPayoff = monthsToPayoff
    )
