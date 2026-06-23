package com.casha.app.domain.model

data class WhatIfSimulation(
    val title: String,
    val subtitle: String,
    val cashflow: WhatIfCashflowImpact,
    val financialHealth: WhatIfFinancialHealth?,
    val impactedBudgets: List<WhatIfBudgetImpact>,
    val goalImpacts: List<WhatIfGoalImpact>,
    val verdict: WhatIfVerdict,
    val applyActions: List<WhatIfApplyAction>
)

data class WhatIfCashflowImpact(
    val before: Double,
    val after: Double,
    val severity: WhatIfSeverity
)

data class WhatIfFinancialHealth(
    val score: Int,
    val label: String,
    val breakdown: WhatIfHealthBreakdown?
)

data class WhatIfHealthBreakdown(
    val dtiScore: Int,
    val cashflowScore: Int,
    val emergencyFundScore: Int,
    val goalProgressScore: Int
)

data class WhatIfBudgetImpact(
    val category: String,
    val before: Double,
    val after: Double,
    val severity: WhatIfSeverity
)

data class WhatIfGoalImpact(
    val name: String,
    val delayMonths: Int,
    val originalDate: String?,
    val newDate: String?
)

data class WhatIfVerdict(
    val severity: WhatIfSeverity,
    val headline: String,
    val message: String,
    val coachingNote: String?,
    val urgency: String?,
    val alternatives: List<WhatIfAlternative>
)

data class WhatIfAlternative(
    val label: String,
    val impact: String,
    val actionType: String,
    val priority: Int
)

data class WhatIfApplyAction(
    val type: String,
    val params: Map<String, String>
)

enum class WhatIfSeverity(val raw: String) {
    SAFE("safe"),
    WARNING("warning"),
    DANGER("danger");

    companion object {
        fun from(raw: String?) = values().firstOrNull { it.raw == raw } ?: WARNING
    }
}
