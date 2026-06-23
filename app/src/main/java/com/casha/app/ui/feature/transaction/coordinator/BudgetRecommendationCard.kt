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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.domain.model.BudgetRecommendationData
import com.casha.app.domain.model.DebtPayoffPlan
import com.casha.app.domain.model.LoanPayoff
import com.casha.app.domain.model.RecommendedBudget
import java.text.NumberFormat
import java.util.Locale

// ─── Colors ──────────────────────────────────────────────────────────────────

private val Indigo = Color(0xFF5856D6)
private val GreenStat = Color(0xFF34C759)
private val RedStat = Color(0xFFFF3B30)
private val BlueStat = Color(0xFF007AFF)
private val OrangePriority = Color(0xFFFF9500)

private fun priorityColor(priority: String): Color = when (priority) {
    "essential" -> BlueStat
    "debt" -> RedStat
    "saving" -> GreenStat
    "lifestyle" -> OrangePriority
    else -> Color(0xFF8E8E93)
}

private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
    maximumFractionDigits = 0
}

private fun formatAmount(amount: Double): String = currencyFormat.format(amount)

// ─── Main Card ───────────────────────────────────────────────────────────────

@Composable
fun BudgetRecommendationCard(
    data: BudgetRecommendationData
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                scaleIn(initialScale = 0.95f, animationSpec = spring(stiffness = Spring.StiffnessLow))
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Header
                HeaderSection(title = data.title, summary = data.summary)

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Income Stats
                IncomeStatsSection(
                    monthlyIncome = data.monthlyIncome,
                    totalDebt = data.totalDebtObligation,
                    freeCashflow = data.freeCashflow
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Budget Allocations
                BudgetAllocationsSection(budgets = data.recommendedBudgets)

                // Debt Payoff Plan (optional)
                if (data.debtPayoffPlan != null && data.debtPayoffPlan.loans.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    DebtPayoffSection(plan = data.debtPayoffPlan)
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Coaching Note
                CoachingNoteSection(note = data.coachingNote)
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun HeaderSection(title: String, summary: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Indigo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Indigo
            ) {
                Text(
                    text = "BUDGET RECOMMENDATION",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = summary,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Income Stats ────────────────────────────────────────────────────────────

@Composable
private fun IncomeStatsSection(monthlyIncome: Double, totalDebt: Double, freeCashflow: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatColumn(label = "Pemasukan", value = formatAmount(monthlyIncome), color = GreenStat)
        StatColumn(label = "Cicilan", value = formatAmount(totalDebt), color = RedStat)
        StatColumn(label = "Free Cashflow", value = formatAmount(freeCashflow), color = BlueStat)
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─── Budget Allocations ──────────────────────────────────────────────────────

@Composable
private fun BudgetAllocationsSection(budgets: List<RecommendedBudget>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "ALOKASI BUDGET",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        budgets.forEach { budget ->
            BudgetAllocationRow(budget = budget)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun BudgetAllocationRow(budget: RecommendedBudget) {
    val color = priorityColor(budget.priority)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = budget.category,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = formatAmount(budget.amount),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (budget.percentage.toFloat() / 100f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${budget.percentage.toInt()}%",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (budget.note.isNotEmpty()) {
            Text(
                text = budget.note,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Debt Payoff Plan ────────────────────────────────────────────────────────

@Composable
private fun DebtPayoffSection(plan: DebtPayoffPlan) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DEBT PAYOFF PLAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = RedStat
            ) {
                Text(
                    text = plan.strategy.uppercase(),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        plan.loans.forEach { loan ->
            LoanPayoffRow(loan = loan)
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Footer
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Estimasi bebas hutang: ",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${plan.estimatedMonthsToDebtFree} bulan",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LoanPayoffRow(loan: LoanPayoff) {
    Column {
        Text(
            text = loan.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Sisa: ${formatAmount(loan.balance)}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${formatAmount(loan.monthlyPayment)}/bln",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedStat
                )
                Text(
                    text = "${loan.monthsToPayoff} bln lagi",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Coaching Note ───────────────────────────────────────────────────────────

@Composable
private fun CoachingNoteSection(note: String) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Indigo),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = note,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}
