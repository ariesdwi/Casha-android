package com.casha.app.domain.model

import java.util.Date

/**
 * Filter periods for the Report screen.
 */
enum class ReportFilterPeriod(val displayName: String) {
    WEEK("This Week"),
    MONTH("This Month"),
    YEAR("This Year")
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
