package com.casha.app.ui.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
 * Color configuration for budget alert cards
 */
private data class AlertColors(
    val primary: Color,
    val background: Color,
    val iconBackground: Color
)

/**
 * Individual budget alert card with professional light/dark mode colors
 */
@Composable
private fun BudgetAlertCard(
    budget: BudgetCasha,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    
    val percentUsed = if (budget.amount > 0) {
        (budget.spent / budget.amount * 100).toInt()
    } else {
        0
    }
    
    val remaining = budget.remaining
    
    // Professional color palette for both light and dark modes
    val alertColors = when {
        // Critical: ≥90% (Danger/Red)
        percentUsed >= 90 -> {
            if (isDark) {
                AlertColors(
                    primary = Color(0xFFFF8A80),        // Soft red for dark mode
                    background = Color(0xFF4A1C1C),     // Dark red background
                    iconBackground = Color(0xFF6B2929)   // Icon background
                )
            } else {
                AlertColors(
                    primary = Color(0xFFD32F2F),        // Vibrant red for light mode
                    background = Color(0xFFFFEBEE),     // Very light red background
                    iconBackground = Color(0xFFFFCDD2)   // Light red icon background
                )
            }
        }
        // Warning: 70-89% (Orange/Amber)
        percentUsed >= 70 -> {
            if (isDark) {
                AlertColors(
                    primary = Color(0xFFFFD54F),        // Soft amber for dark mode
                    background = Color(0xFF4A3A1C),     // Dark amber background
                    iconBackground = Color(0xFF6B5329)   // Icon background
                )
            } else {
                AlertColors(
                    primary = Color(0xFFF57C00),        // Vibrant orange for light mode
                    background = Color(0xFFFFF3E0),     // Very light amber background
                    iconBackground = Color(0xFFFFE0B2)   // Light amber icon background
                )
            }
        }
        // Default: <70% (shouldn't show, but fallback to warning)
        else -> {
            if (isDark) {
                AlertColors(
                    primary = Color(0xFFFFD54F),
                    background = Color(0xFF4A3A1C),
                    iconBackground = Color(0xFF6B5329)
                )
            } else {
                AlertColors(
                    primary = Color(0xFFF57C00),
                    background = Color(0xFFFFF3E0),
                    iconBackground = Color(0xFFFFE0B2)
                )
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 0.dp else 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isDark) Color.Transparent else alertColors.primary.copy(alpha = 0.1f),
                spotColor = if (isDark) Color.Transparent else alertColors.primary.copy(alpha = 0.1f)
            )
            .clickable(
                onClickLabel = "View ${budget.category} budget details"
            ) { onClick() }
            .semantics {
                contentDescription = "Budget alert: ${budget.category} category " +
                    "$percentUsed percent used, ${CurrencyFormatter.format(remaining)} remaining"
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = alertColors.background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .heightIn(min = 56.dp), // Better touch target
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Warning icon with improved styling
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(alertColors.iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = alertColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Category name
                    Text(
                        text = budget.category,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = alertColors.primary
                    )
                    
                    // Usage info
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$percentUsed% used",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = alertColors.primary.copy(alpha = if (isDark) 0.85f else 0.75f)
                        )
                        
                        // Dot separator
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(alertColors.primary.copy(alpha = 0.4f))
                        )
                        
                        Text(
                            text = stringResource(
                                R.string.dashboard_budget_alert_remaining,
                                CurrencyFormatter.format(remaining.coerceAtLeast(0.0))
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = alertColors.primary.copy(alpha = if (isDark) 0.85f else 0.75f)
                        )
                    }
                }
            }

            // Chevron icon with improved styling
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = alertColors.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
