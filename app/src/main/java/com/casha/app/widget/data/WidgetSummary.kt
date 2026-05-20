package com.casha.app.widget.data

import kotlinx.serialization.Serializable

@Serializable
data class WidgetSummary(
    val safeSpendToday: Double = 0.0,
    val currency: String = "IDR",
    val daysRemaining: Int = 0,
    val monthlyIncome: Double = 0.0,
    val spentSoFar: Double = 0.0,
    val pendingObligations: Double = 0.0,
    val freeRemaining: Double = 0.0,
    val status: String = "unknown",
    val statusLabel: String = "-",
    val budgetPctUsed: Int = 0,
    val hideBalance: Boolean = false,
    val lastFetchedAt: String? = null,
    val lastUpdatedAt: String? = null,
    val spentToday: Double = 0.0,
    val insightText: String? = null,
    val insightGeneratedAt: String? = null,
    val nextBillName: String? = null,
    val nextBillAmount: Double? = null,
    val nextBillDueInDays: Int? = null
) {
    val spendStatus: SpendStatus get() = SpendStatus.fromString(status)
}
