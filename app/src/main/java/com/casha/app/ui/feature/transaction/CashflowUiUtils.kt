package com.casha.app.ui.feature.transaction

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.casha.app.domain.model.CashflowEntry
import com.casha.app.domain.model.CashflowType
import com.casha.app.ui.theme.*

/**
 * Shared UI utilities for rendering Cashflow items (Incomes & Transactions).
 * Ported and centralized from DashboardComponents.kt logic.
 */
object CashflowUiUtils {

    fun colorForType(type: CashflowType): Color {
        return when (type) {
            CashflowType.INCOME -> CashaSuccess
            CashflowType.EXPENSE -> CashaBlue
        }
    }

    fun gradientForType(type: CashflowType): Brush {
        val colors = when (type) {
            CashflowType.INCOME -> listOf(CashaAccentLight, CashaTeal)
            CashflowType.EXPENSE -> listOf(CashaBlue, CashaPurple)
        }
        return Brush.linearGradient(colors)
    }

    fun iconForEntry(entry: CashflowEntry): ImageVector {
        if (entry.type == CashflowType.INCOME) return Icons.Default.CallMade
        
        return when (entry.category.lowercase().trim()) {
            "food", "food & drink", "food & dining", "restaurant" -> Icons.Default.Restaurant
            "shopping" -> Icons.Default.ShoppingBag
            "transport", "transportation" -> Icons.Default.DirectionsCar
            "entertainment", "movie", "gaming" -> Icons.Default.Movie
            "utilities", "bill", "electricity", "water" -> Icons.Default.Bolt
            "salary" -> Icons.Default.Payments
            "bonus", "gift" -> Icons.Default.AutoAwesome
            "investment", "stock", "crypto" -> Icons.Default.TrendingUp
            "health", "medical", "pharmacy" -> Icons.Default.LocalHospital
            "education" -> Icons.Default.School
            else -> Icons.Default.CreditCard
        }
    }
}
