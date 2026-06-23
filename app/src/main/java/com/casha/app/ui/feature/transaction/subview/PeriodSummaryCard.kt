package com.casha.app.ui.feature.transaction.subview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.ui.theme.CashaDanger
import com.casha.app.ui.theme.CashaSuccess
import com.casha.app.ui.theme.CashaTheme

/**
 * Period summary card component displaying aggregated financial data.
 * 
 * Displays total income (green), total expense (red), and net amount (contextual color)
 * in a compact horizontal layout matching existing card design.
 * 
 * Validates: Requirements 1.1, 1.3, 1.4, 1.5
 * 
 * @param totalIncome Total income for the period
 * @param totalExpense Total expense for the period
 * @param netAmount Net amount (income - expense) for the period
 * @param modifier Optional modifier for styling
 */
@Composable
fun PeriodSummaryCard(
    totalIncome: Double,
    totalExpense: Double,
    netAmount: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Income Section
            PeriodSummaryItem(
                icon = Icons.Default.ArrowDownward,
                iconColor = CashaSuccess,
                label = "Income",
                amount = totalIncome,
                amountColor = CashaSuccess,
                modifier = Modifier.weight(1f)
            )

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )

            // Expense Section
            PeriodSummaryItem(
                icon = Icons.Default.ArrowUpward,
                iconColor = CashaDanger,
                label = "Expense",
                amount = totalExpense,
                amountColor = CashaDanger,
                modifier = Modifier.weight(1f)
            )

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )

            // Net Section
            val netColor = if (netAmount >= 0) CashaSuccess else CashaDanger
            PeriodSummaryItem(
                icon = Icons.Default.AccountBalance,
                iconColor = netColor,
                label = "Net",
                amount = netAmount,
                amountColor = netColor,
                showSign = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual summary item within the period summary card.
 * 
 * @param icon Icon to display
 * @param iconColor Color of the icon
 * @param label Label text (e.g., "Income", "Expense", "Net")
 * @param amount Amount value to display
 * @param amountColor Color of the amount text
 * @param showSign Whether to show +/- sign for the amount
 * @param modifier Optional modifier for styling
 */
@Composable
private fun PeriodSummaryItem(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    amount: Double,
    amountColor: Color,
    showSign: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Icon with background
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Amount
        val prefix = if (showSign) {
            if (amount >= 0) "+" else ""
        } else {
            ""
        }
        
        Text(
            text = "$prefix${CurrencyFormatter.format(amount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}


// ========== Preview Composables ==========

/**
 * Preview for PeriodSummaryCard with positive net amount
 */
@Preview(name = "Positive Net Amount", showBackground = true)
@Composable
private fun PeriodSummaryCardPreview_Positive() {
    CashaTheme {
        PeriodSummaryCard(
            totalIncome = 5000.0,
            totalExpense = 3000.0,
            netAmount = 2000.0
        )
    }
}

/**
 * Preview for PeriodSummaryCard with negative net amount
 */
@Preview(name = "Negative Net Amount", showBackground = true)
@Composable
private fun PeriodSummaryCardPreview_Negative() {
    CashaTheme {
        PeriodSummaryCard(
            totalIncome = 2000.0,
            totalExpense = 5000.0,
            netAmount = -3000.0
        )
    }
}

/**
 * Preview for PeriodSummaryCard with zero values
 */
@Preview(name = "Zero Values", showBackground = true)
@Composable
private fun PeriodSummaryCardPreview_Zero() {
    CashaTheme {
        PeriodSummaryCard(
            totalIncome = 0.0,
            totalExpense = 0.0,
            netAmount = 0.0
        )
    }
}

/**
 * Preview for PeriodSummaryCard with large amounts
 */
@Preview(name = "Large Amounts", showBackground = true)
@Composable
private fun PeriodSummaryCardPreview_Large() {
    CashaTheme {
        PeriodSummaryCard(
            totalIncome = 1000000.0,
            totalExpense = 750000.0,
            netAmount = 250000.0
        )
    }
}

/**
 * Preview for PeriodSummaryCard with only income
 */
@Preview(name = "Only Income", showBackground = true)
@Composable
private fun PeriodSummaryCardPreview_OnlyIncome() {
    CashaTheme {
        PeriodSummaryCard(
            totalIncome = 8000.0,
            totalExpense = 0.0,
            netAmount = 8000.0
        )
    }
}

/**
 * Preview for PeriodSummaryCard with only expenses
 */
@Preview(name = "Only Expenses", showBackground = true)
@Composable
private fun PeriodSummaryCardPreview_OnlyExpenses() {
    CashaTheme {
        PeriodSummaryCard(
            totalIncome = 0.0,
            totalExpense = 4500.0,
            netAmount = -4500.0
        )
    }
}
