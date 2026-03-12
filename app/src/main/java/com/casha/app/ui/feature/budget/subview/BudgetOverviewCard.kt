package com.casha.app.ui.feature.budget.subview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.BudgetSummary
import com.casha.app.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.casha.app.R

@Composable
private fun getProgressColor(spentPercentage: Float): Color {
    val isDark = isSystemInDarkTheme()
    return when {
        spentPercentage < 0.7f -> if (isDark) Color(0xFF81C784) else CashaSuccess
        spentPercentage < 0.9f -> if (isDark) Color(0xFFFFB74D) else CashaWarning
        else -> if (isDark) Color(0xFFE57373) else CashaDanger
    }
}

/**
 * Premium budget overview card with a custom donut chart and detailed stats.
 */
@Composable
fun BudgetOverviewCard(
    summary: BudgetSummary?,
    modifier: Modifier = Modifier
) {
    val totalBudget = summary?.totalBudget ?: 0.0
    val totalSpent = summary?.totalSpent ?: 0.0
    val totalRemaining = summary?.totalRemaining ?: 0.0
    val spentPercentage = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    val displayPercentage = (spentPercentage * 100).toInt()
    
    val progressColor = getProgressColor(spentPercentage)
    
    val isDark = isSystemInDarkTheme()
    val successColor = if (isDark) Color(0xFF81C784) else CashaSuccess
    val dangerColor = if (isDark) Color(0xFFE57373) else CashaDanger

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.budget_summary_overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Donut Chart
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    // Background Circle
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Progress Circle
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = 360f * spentPercentage,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$displayPercentage%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                    Text(
                        text = stringResource(R.string.budget_label_spent),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    icon = Icons.Default.KeyboardArrowDown,
                    iconColor = CashaBlue,
                    amount = totalBudget,
                    label = stringResource(R.string.budget_label_budget).uppercase(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.KeyboardArrowUp,
                    iconColor = progressColor,
                    amount = totalSpent,
                    label = stringResource(R.string.budget_label_spent).uppercase(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.CheckCircle,
                    iconColor = if (totalRemaining >= 0) successColor else dangerColor,
                    amount = totalRemaining,
                    label = stringResource(R.string.budget_label_remaining).uppercase(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Linear Progress
            LinearProgressIndicator(
                progress = { spentPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                color = progressColor,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.budget_label_spent)}: ${CurrencyFormatter.format(totalSpent)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${stringResource(R.string.budget_label_remaining)}: ${CurrencyFormatter.format(totalRemaining)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (totalRemaining >= 0) successColor else dangerColor
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    iconColor: Color,
    amount: Double,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
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
            Text(
                text = CurrencyFormatter.format(amount),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
