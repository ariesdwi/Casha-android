package com.casha.app.domain.model

import java.util.Date

/**
 * Represents a single cashflow entry (either Income or Transaction).
 */
data class CashflowEntry(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val type: CashflowType,
    val date: Date,
    val icon: String? = null
)

enum class CashflowType {
    INCOME, EXPENSE
}

data class CashflowDateSection(
    val day: String,
    val date: String,
    val items: List<CashflowEntry>
)

/**
 * Summary of cashflow for a specific period.
 */
data class CashflowSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val periodLabel: String,
    val currency: String = "IDR",
    val incomeBreakdown: Map<String, Double> = emptyMap(),
    val expenseBreakdown: Map<String, Double> = emptyMap(),
    val liabilityBreakdown: Map<String, Double> = emptyMap()
)

/**
 * Data for spending reports/charts.
 */
data class SpendingReport(
    val thisWeekTotal: Double,
    val thisMonthTotal: Double,
    val dailyBars: List<ChartBar>,
    val weeklyBars: List<ChartBar>
)

data class ChartBar(
    val label: String,
    val value: Double
)

/**
 * Periods for filtering dashboard data.
 */
sealed class SpendingPeriod {
    object THIS_WEEK : SpendingPeriod()
    object THIS_MONTH : SpendingPeriod()
    object LAST_MONTH : SpendingPeriod()
    object LAST_THREE_MONTHS : SpendingPeriod()
    object THIS_YEAR : SpendingPeriod()
    object ALL_TIME : SpendingPeriod()
    object FUTURE : SpendingPeriod()
    data class CUSTOM(val start: java.util.Date, val end: java.util.Date) : SpendingPeriod()
}
