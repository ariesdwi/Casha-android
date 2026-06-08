package com.casha.app.ui.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.casha.app.R
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.BudgetCasha
import com.casha.app.ui.theme.*

/**
 * Conditional budget alert banner that displays when budget categories exceed smart threshold.
 *
 * Features:
 * - Only visible when alerts exist (height = 0dp when empty)
 * - Displays max 2 alerts sorted by severity
 * - Shows category name and remaining amount/percentage
 * - Color-coded by severity (Warning 70-90%, Danger ≥90%)
 * - Tappable to navigate to budget detail
 *
 * @param alerts List of budget alerts (already filtered and sorted)
 * @param onAlertClick Callback when an alert is tapped, receives the BudgetCasha
 */
@Composable
fun BudgetAlertBanner(
    alerts: List<BudgetCasha>,
    onAlertClick: (BudgetCasha) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only render if alerts exist
    if (alerts.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Show max 2 alerts
        alerts.take(2).forEach { budget ->
            BudgetAlertCard(
                budget = budget,
                onClick = { onAlertClick(budget) }
            )
        }
    }
}

/**
 * Individual budget alert card
 */
@Composable
private fun BudgetAlertCard(
    budget: BudgetCasha,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentUsed = if (budget.amount > 0) {
        (budget.spent / budget.amount * 100).toInt()
    } else {
        0
    }
    
    val remaining = budget.remaining
    
    // Determine severity color
    val (alertColor, alertColorBg) = when {
        percentUsed >= 90 -> CashaDanger to CashaDanger.copy(alpha = 0.1f)
        percentUsed >= 70 -> CashaWarning to CashaWarning.copy(alpha = 0.1f)
        else -> CashaWarning to CashaWarning.copy(alpha = 0.1f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
            .clickable(
                onClickLabel = "View ${budget.category} budget details"
            ) { onClick() }
            .semantics {
                contentDescription = "Budget alert: ${budget.category} category " +
                    "$percentUsed percent used, ${CurrencyFormatter.format(remaining)} remaining"
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = alertColorBg
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .heightIn(min = 48.dp), // Minimum touch target
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Warning icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(alertColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = alertColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = budget.category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = alertColor
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$percentUsed% used",
                            style = MaterialTheme.typography.bodySmall,
                            color = alertColor.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = alertColor.copy(alpha = 0.5f)
                        )
                        
                        Text(
                            text = stringResource(
                                R.string.dashboard_budget_alert_remaining,
                                CurrencyFormatter.format(remaining.coerceAtLeast(0.0))
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = alertColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Chevron icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = alertColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
