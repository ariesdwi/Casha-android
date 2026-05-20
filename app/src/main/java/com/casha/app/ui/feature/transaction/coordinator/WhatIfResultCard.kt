package com.casha.app.ui.feature.transaction.coordinator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.domain.model.*
import java.text.NumberFormat
import java.util.Locale

// ─── Severity helpers ────────────────────────────────────────────────────────

private fun WhatIfSeverity.toColor(): Color = when (this) {
    WhatIfSeverity.SAFE    -> Color(0xFF2E7D32)
    WhatIfSeverity.WARNING -> Color(0xFFFF9800)
    WhatIfSeverity.DANGER  -> Color(0xFFF44336)
}

private fun WhatIfSeverity.toIcon(): ImageVector = when (this) {
    WhatIfSeverity.SAFE    -> Icons.Filled.CheckCircle
    WhatIfSeverity.WARNING -> Icons.Filled.Warning
    WhatIfSeverity.DANGER  -> Icons.Filled.Cancel
}

private fun healthScoreColor(score: Int): Color = when {
    score >= 75 -> Color(0xFF2E7D32)
    score >= 50 -> Color(0xFFFF9800)
    else        -> Color(0xFFF44336)
}

private fun formatCurrency(amount: Double): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    fmt.maximumFractionDigits = 0
    return fmt.format(amount)
}

// ─── Main card ───────────────────────────────────────────────────────────────

@Composable
fun WhatIfResultCard(
    simulation: WhatIfSimulation,
    message: String,
    onResetTapped: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.92f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(animationSpec = spring())
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // ── Header ──────────────────────────────────────────────────
                WhatIfHeader(simulation.title, simulation.subtitle)

                WhatIfDivider()

                // ── Cashflow ─────────────────────────────────────────────────
                WhatIfCashflowSection(simulation.cashflow)

                // ── Financial Health (optional) ──────────────────────────────
                simulation.financialHealth?.let {
                    WhatIfDivider()
                    WhatIfFinancialHealthSection(it)
                }

                // ── Impacted Budgets (optional) ──────────────────────────────
                if (simulation.impactedBudgets.isNotEmpty()) {
                    WhatIfDivider()
                    WhatIfBudgetSection(simulation.impactedBudgets)
                }

                // ── Goal Impacts (optional) ──────────────────────────────────
                if (simulation.goalImpacts.isNotEmpty()) {
                    WhatIfDivider()
                    WhatIfGoalSection(simulation.goalImpacts)
                }

                // ── Verdict ──────────────────────────────────────────────────
                WhatIfDivider()
                WhatIfVerdictSection(simulation.verdict)

                // ── Actions ──────────────────────────────────────────────────
                WhatIfDivider()
                WhatIfActions(onResetTapped = onResetTapped)
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun WhatIfHeader(title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF6C63FF).copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.AutoFixHigh,
                contentDescription = null,
                tint = Color(0xFF6C63FF),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            color = Color(0xFF6C63FF).copy(alpha = 0.14f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = "WHAT IF",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6C63FF),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    if (subtitle.isNotBlank()) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Cashflow ─────────────────────────────────────────────────────────────────

@Composable
private fun WhatIfCashflowSection(cashflow: WhatIfCashflowImpact) {
    val sevColor = cashflow.severity.toColor()
    val maxVal = maxOf(cashflow.before, cashflow.after, 1.0)

    Text(
        text = "Cashflow Bulanan",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(10.dp))

    // Sebelum row
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Sebelum",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(58.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { (cashflow.before / maxVal).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF2E7D32),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatCurrency(cashflow.before),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2E7D32),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 90.dp)
        )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Sesudah row
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Sesudah",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(58.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { (cashflow.after / maxVal).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = sevColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatCurrency(cashflow.after),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = sevColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 90.dp)
        )
    }
}

// ─── Financial Health ─────────────────────────────────────────────────────────

@Composable
private fun WhatIfFinancialHealthSection(health: WhatIfFinancialHealth) {
    val ringColor = healthScoreColor(health.score)

    Text(
        text = "Financial Health",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(10.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Score ring
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(64.dp)) {
                val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                val inset = 4.dp.toPx()
                val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                val topLeft = Offset(inset, inset)
                // Track
                drawArc(
                    color = ringColor.copy(alpha = 0.15f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
                // Fill
                drawArc(
                    color = ringColor,
                    startAngle = 135f,
                    sweepAngle = 270f * (health.score / 100f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
            }
            Text(
                text = "${health.score}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ringColor
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = health.label.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = ringColor
            )
            health.breakdown?.let { b ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SubScoreChip("DTI", b.dtiScore)
                    SubScoreChip("CF", b.cashflowScore)
                    SubScoreChip("EF", b.emergencyFundScore)
                    SubScoreChip("Goal", b.goalProgressScore)
                }
            }
        }
    }
}

@Composable
private fun SubScoreChip(label: String, score: Int) {
    val color = healthScoreColor(score)
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = "$label $score",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

// ─── Budget Impacts ───────────────────────────────────────────────────────────

@Composable
private fun WhatIfBudgetSection(budgets: List<WhatIfBudgetImpact>) {
    Text(
        text = "Budget Terdampak",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    budgets.forEach { budget ->
        val color = budget.severity.toColor()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                budget.severity.toIcon(),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = budget.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${formatCurrency(budget.before)} → ${formatCurrency(budget.after)}",
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─── Goal Impacts ─────────────────────────────────────────────────────────────

@Composable
private fun WhatIfGoalSection(goals: List<WhatIfGoalImpact>) {
    Text(
        text = "Dampak ke Goal",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    goals.forEach { goal ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(16.dp).padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "${goal.name} — Mundur ${goal.delayMonths} bulan",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (goal.originalDate != null || goal.newDate != null) {
                    Text(
                        text = "${goal.originalDate ?: "-"} → ${goal.newDate ?: "-"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── Verdict ─────────────────────────────────────────────────────────────────

@Composable
private fun WhatIfVerdictSection(verdict: WhatIfVerdict) {
    val sevColor = verdict.severity.toColor()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            verdict.severity.toIcon(),
            contentDescription = null,
            tint = sevColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = verdict.headline,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = sevColor
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = verdict.message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 20.sp
    )

    verdict.coachingNote?.let { note ->
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = Color(0xFF42A5F5).copy(alpha = 0.10f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("🎓", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF42A5F5),
                    lineHeight = 18.sp
                )
            }
        }
    }

    verdict.urgency?.let { urgency ->
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            color = Color(0xFFFF9800).copy(alpha = 0.10f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("❗", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = urgency,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF9800),
                    lineHeight = 18.sp
                )
            }
        }
    }

    if (verdict.alternatives.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        verdict.alternatives
            .sortedBy { it.priority }
            .forEach { alt ->
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("💡", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = alt.label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = alt.impact,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
    }
}

// ─── Actions ─────────────────────────────────────────────────────────────────

@Composable
private fun WhatIfActions(onResetTapped: () -> Unit) {
    // "Simulasi Baru" — main CTA
    Button(
        onClick = onResetTapped,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6C63FF),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Simulasi Baru", fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(8.dp))

    // "Terapkan" — Phase 2, permanently disabled
    OutlinedButton(
        onClick = {},
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Terapkan (Coming Soon)")
    }
}

// ─── Divider helper ───────────────────────────────────────────────────────────

@Composable
private fun WhatIfDivider() {
    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(12.dp))
}
