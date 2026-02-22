package com.casha.app.domain.model

import java.util.Date
import java.util.Calendar

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
    
    // Abstract function so subclasses can compute their specific ranges
    abstract fun dateRange(): Pair<Date, Date?>

    object THIS_WEEK : SpendingPeriod() {
        override fun dateRange(): Pair<Date, Date?> {
            val calendar = Calendar.getInstance()
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.time
            
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val end = calendar.time
            
            return Pair(start, end)
        }
    }

    object THIS_MONTH : SpendingPeriod() {
        override fun dateRange(): Pair<Date, Date?> {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.time
            
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.SECOND, -1)
            val end = calendar.time
            
            return Pair(start, end)
        }
    }

    object LAST_MONTH : SpendingPeriod() {
        override fun dateRange(): Pair<Date, Date?> {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.time
            
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.SECOND, -1)
            val end = calendar.time
            
            return Pair(start, end)
        }
    }

    object LAST_THREE_MONTHS : SpendingPeriod() {
        override fun dateRange(): Pair<Date, Date?> {
            val calendar = Calendar.getInstance()
            
            // startOfCurrentMonth
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfCurrentMonth = calendar.timeInMillis
            
            // Go back 3 months from start of current month
            calendar.add(Calendar.MONTH, -3)
            val start = calendar.time
            
            // Go to end of current month
            calendar.timeInMillis = startOfCurrentMonth
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.SECOND, -1)
            val end = calendar.time
            
            return Pair(start, end)
        }
    }

    object THIS_YEAR : SpendingPeriod() {
        override fun dateRange(): Pair<Date, Date?> {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.time
            
            calendar.add(Calendar.YEAR, 1)
            calendar.add(Calendar.SECOND, -1)
            val end = calendar.time
            
            return Pair(start, end)
        }
    }

    object ALL_TIME : SpendingPeriod() {
        override fun dateRange(): Pair<Date, Date?> {
            return Pair(Date(0), null)
        }
    }

    object FUTURE : SpendingPeriod() {
        override fun dateRange(): Pair<Date, Date?> {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.time
            return Pair(start, null)
        }
    }

    data class CUSTOM(val start: Date, val end: Date) : SpendingPeriod() {
        override fun dateRange(): Pair<Date, Date?> {
            return Pair(start, end)
        }
    }
}
