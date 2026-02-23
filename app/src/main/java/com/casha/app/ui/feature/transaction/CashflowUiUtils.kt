package com.casha.app.ui.feature.transaction

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.casha.app.domain.model.CashflowEntry
import com.casha.app.domain.model.CashflowType
import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.model.IncomeCasha
import com.casha.app.ui.theme.*

/**
 * Shared UI utilities for rendering Cashflow items (Incomes & Transactions).
 * Ported and centralized from DashboardComponents.kt logic.
 */
object CashflowUiUtils {

    fun colorForType(type: CashflowType): Color {
        return when (type) {
            CashflowType.INCOME -> CashaSuccess
            CashflowType.EXPENSE -> CashaDanger
        }
    }

    fun gradientForType(type: CashflowType): Brush {
        val colors = when (type) {
            CashflowType.INCOME -> listOf(CashaAccentLight, CashaTeal)
            CashflowType.EXPENSE -> listOf(CashaDanger, Color(0xFFFF8A80))
        }
        return Brush.linearGradient(colors)
    }

    fun iconForEntry(entry: CashflowEntry): ImageVector {
        if (entry.type == CashflowType.INCOME) return Icons.AutoMirrored.Filled.CallMade
        
        return when (entry.category.lowercase().trim()) {
            "food", "food & drink", "food & dining", "restaurant" -> Icons.Default.Restaurant
            "shopping" -> Icons.Default.ShoppingBag
            "transport", "transportation" -> Icons.Default.DirectionsCar
            "entertainment", "movie", "gaming" -> Icons.Default.Movie
            "utilities", "bill", "electricity", "water" -> Icons.Default.Bolt
            "salary" -> Icons.Default.Payments
            "bonus", "gift" -> Icons.Default.AutoAwesome
            "investment", "stock", "crypto" -> Icons.AutoMirrored.Filled.TrendingUp
            "health", "medical", "pharmacy" -> Icons.Default.LocalHospital
            "education" -> Icons.Default.School
            else -> Icons.Default.CreditCard
        }
    }

    fun groupTransactionsByDate(transactions: List<CashflowEntry>): List<com.casha.app.domain.model.CashflowDateSection> {
        val dateFormatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val displayDateFormatter = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())
        val dayFormatter = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())
        
        val now = java.util.Date()
        val todayStr = dateFormatter.format(now)
        val calendar = java.util.Calendar.getInstance().apply { time = now; add(java.util.Calendar.DATE, -1) }
        val yesterdayStr = dateFormatter.format(calendar.time)

        return transactions
            .sortedByDescending { it.date }
            .groupBy { dateFormatter.format(it.date) }
            .map { (dateKey, entries) ->
                val parsed = dateFormatter.parse(dateKey) ?: java.util.Date()
                val displayDate = displayDateFormatter.format(parsed)
                val day = when (dateKey) {
                    todayStr -> "Today"
                    yesterdayStr -> "Yesterday"
                    else -> dayFormatter.format(parsed)
                }
                com.casha.app.domain.model.CashflowDateSection(day = day, date = displayDate, items = entries)
            }
    }

    fun TransactionCasha.toCashflowEntry(): CashflowEntry {
        return CashflowEntry(
            id = this.id,
            title = this.name,
            amount = this.amount,
            category = this.category,
            type = CashflowType.EXPENSE,
            date = this.datetime,
            icon = null
        )
    }

    fun IncomeCasha.toTransaction(): TransactionCasha {
        return TransactionCasha(
            id = id,
            name = name,
            category = type.name,
            amount = amount,
            datetime = datetime,
            note = note,
            isSynced = true, // Incomes are usually fetched from remote and synced
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun IncomeCasha.toCashflowEntry(): CashflowEntry {
        return CashflowEntry(
            id = id,
            title = name,
            amount = amount,
            category = type.name,
            type = CashflowType.INCOME,
            date = datetime,
            icon = null
        )
    }
}
