package com.casha.app.domain.model

import java.time.LocalDate
import java.time.YearMonth
import java.util.Date

/**
 * Filter periods for the Report screen.
 */
enum class ReportFilterPeriod(val displayName: String) {
    WEEK("This Week"),
    MONTH("This Month"),
    YEAR("This Year"),
    CUSTOM("Custom Range")
}

/**
 * Represents aggregated spending data for a category in the report.
 */
data class ChartCategorySpending(
    val id: String,
    val category: String,
    val total: Double,
    val percentage: Double
)

/**
 * Daily spending amount for a single calendar day.
 */
data class DailySpending(
    val date: LocalDate,
    val amount: Double
)

/**
 * Monthly spending total for year-view calendar tiles.
 */
data class MonthlySpending(
    val month: YearMonth,
    val amount: Double
)
