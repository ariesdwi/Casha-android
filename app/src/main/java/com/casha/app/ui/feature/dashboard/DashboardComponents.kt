package com.casha.app.ui.feature.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.*
import com.casha.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max

@Composable
fun CardBalanceSection(
    summary: CashflowSummary?,
    selectedPeriod: SpendingPeriod,
    onPeriodChange: (SpendingPeriod) -> Unit
) {
    var isBalanceVisible by remember { mutableStateOf(true) }
    var showPeriodPicker by remember { mutableStateOf(false) }

    val netBalanceValue = summary?.netBalance ?: 0.0
    val amountColor = when {
        netBalanceValue > 0 -> CashaSuccess
        netBalanceValue < 1 -> CashaDanger
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Label
                Text(
                    text = "Net Cashflow", // TODO: Localize "dashboard.balance.net_cashflow"
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                // Amount Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isBalanceVisible) CurrencyFormatter.format(summary?.netBalance ?: 0.0) else "••••••••",
                        style = MaterialTheme.typography.displaySmall,
                        color = amountColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    IconButton(
                        onClick = { isBalanceVisible = !isBalanceVisible },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Balance",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Period Selector Button
                TextButton(
                    onClick = { showPeriodPicker = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = getPeriodTitle(selectedPeriod),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mini Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniStatItem(
                        title = "In", // TODO: Localize "dashboard.balance.in"
                        amount = summary?.totalIncome ?: 0.0,
                        icon = Icons.Default.NorthEast,
                        color = CashaSuccess,
                        isVisible = isBalanceVisible,
                        modifier = Modifier.weight(1f)
                    )

                    VerticalDivider(
                        modifier = Modifier.height(30.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    MiniStatItem(
                        title = "Out", // TODO: Localize "dashboard.balance.out"
                        amount = summary?.totalExpense ?: 0.0,
                        icon = Icons.Default.SouthEast,
                        color = CashaDanger,
                        isVisible = isBalanceVisible,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showPeriodPicker) {
        PeriodPickerBottomSheet(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = {
                onPeriodChange(it)
                showPeriodPicker = false
            },
            onDismiss = { showPeriodPicker = false }
        )
    }
}

@Composable
fun MiniStatItem(
    title: String,
    amount: Double,
    icon: ImageVector,
    color: Color,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = if (isVisible) CurrencyFormatter.format(amount) else "••••",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodPickerBottomSheet(
    selectedPeriod: SpendingPeriod,
    onPeriodSelected: (SpendingPeriod) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Select Period",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            val periods = listOf(
                SpendingPeriod.THIS_WEEK,
                SpendingPeriod.THIS_MONTH,
                SpendingPeriod.LAST_MONTH,
                SpendingPeriod.LAST_THREE_MONTHS,
                SpendingPeriod.THIS_YEAR,
                SpendingPeriod.ALL_TIME
            )

            periods.forEach { period ->
                val title = getPeriodTitle(period)
                ListItem(
                    headlineContent = { Text(title) },
                    trailingContent = {
                        if (selectedPeriod == period) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.clickable {
                        onPeriodSelected(period)
                    }
                )
            }
        }
    }
}

fun getPeriodTitle(period: SpendingPeriod): String {
    return when (period) {
        SpendingPeriod.THIS_WEEK -> "This Week"
        SpendingPeriod.THIS_MONTH -> "This Month"
        SpendingPeriod.LAST_MONTH -> "Last Month"
        SpendingPeriod.LAST_THREE_MONTHS -> "Last 3 Months"
        SpendingPeriod.THIS_YEAR -> "This Year"
        SpendingPeriod.ALL_TIME -> "All Time"
        SpendingPeriod.FUTURE -> "Future"
        is SpendingPeriod.CUSTOM -> {
            val formatter = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.US)
            formatter.format(period.start)
        }
    }
}

@Composable
fun ReportSection(
    report: SpendingReport,
    selectedTab: ChartTab,
    onTabChange: (ChartTab) -> Unit,
    isSyncing: Boolean
) {
    val transition = rememberInfiniteTransition(label = "syncRotation")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Spending This Month", // TODO: Localize "dashboard.spending.title"
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (isSyncing) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).rotate(rotation),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        ReportChartView(
            report = report,
            selectedTab = selectedTab,
            onTabChange = onTabChange
        )
    }
}

@Composable
fun ReportChartView(
    report: SpendingReport,
    selectedTab: ChartTab,
    onTabChange: (ChartTab) -> Unit
) {
    val weekTotal = CurrencyFormatter.format(report.thisWeekTotal)
    val monthTotal = CurrencyFormatter.format(report.thisMonthTotal)
    
    val chartData = if (selectedTab == ChartTab.WEEK) report.dailyBars else report.weeklyBars
    val maxAmount = chartData.map { it.value }.maxOrNull() ?: 1.0
    val isEmpty = chartData.all { it.value == 0.0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Toggle Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChartToggleButton(
                    title = "Week", // TODO: Localize "report.filter.week"
                    isSelected = selectedTab == ChartTab.WEEK,
                    onClick = { onTabChange(ChartTab.WEEK) },
                    modifier = Modifier.weight(1f)
                )
                ChartToggleButton(
                    title = "Month", // TODO: Localize "report.filter.month"
                    isSelected = selectedTab == ChartTab.MONTH,
                    onClick = { onTabChange(ChartTab.MONTH) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Total Amount
            Text(
                text = if (selectedTab == ChartTab.WEEK) weekTotal else monthTotal,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary, 
                modifier = Modifier.fillMaxWidth()
            )

            // Bar Chart
            val animatedHeight by animateDpAsState(
                targetValue = if (isEmpty) 40.dp else 160.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "chartHeight"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(animatedHeight)
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                chartData.forEachIndexed { index, bar ->
                    val barHeightFactor = (bar.value / max(maxAmount, 0.01)).toFloat()
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (!isEmpty) {
                            // Bar area takes all remaining space
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .width(24.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                // Animated Bar
                                var animatedHeightFactor by remember { mutableStateOf(0f) }
                                LaunchedEffect(barHeightFactor) {
                                    animatedHeightFactor = barHeightFactor
                                }
                                
                                val heightState by animateFloatAsState(
                                    targetValue = animatedHeightFactor,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                                    label = "barHeight"
                                )

                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .fillMaxHeight(heightState.coerceAtLeast(0.02f))
                                        .background(
                                            color = if (index % 2 == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                        )
                                )
                            }
    
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Label always visible below the bar
                        Text(
                            text = bar.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isEmpty) 0.5f else 1f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChartToggleButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    val borderColor = MaterialTheme.colorScheme.primary

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50.dp), // Pill shape matching iOS
        color = backgroundColor,
        contentColor = contentColor,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, borderColor) else null
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GoalSection(
    goals: List<Goal>,
    onSeeAllClick: () -> Unit,
    onGoalClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saving Goals", // TODO: Localize "dashboard.goals.title"
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onSeeAllClick) {
                Text(
                    text = "See All", // TODO: Localize "dashboard.goals.see_all"
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        if (goals.isEmpty()) {
            GoalEmptyState()
        } else {
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(goals.take(3).size) { index ->
                    val goal = goals[index]
                    GoalCard(
                        goal = goal,
                        onClick = { onGoalClick(goal.id) },
                        modifier = Modifier.width(280.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GoalEmptyState() {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎯",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.clip(CircleShape)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No goals yet", // TODO: Localize "goal.empty.title"
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Start saving for your future", // TODO: Localize "goal.empty.subtitle"
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        val targetColor = remember(goal.color) {
            val defaultColor = CashaAccentLight // Default Casha Green
            try {
                goal.color?.let { Color(android.graphics.Color.parseColor(it)) } ?: defaultColor
            } catch (e: Exception) {
                defaultColor
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = goal.icon?.ifEmpty { "🎯" } ?: "🎯",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Text(
                    text = "${goal.progress.percentage.toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { (goal.progress.percentage / 100.0).coerceIn(0.0, 1.0).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = targetColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
                
                 Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = CurrencyFormatter.format(goal.currentAmount.toDouble(), goal.currency),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = CurrencyFormatter.format(goal.targetAmount.toDouble(), goal.currency),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RecentTransactionsSection(
    transactions: List<CashflowEntry>,
    onTransactionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions", // TODO: Localize "dashboard.transactions.recent"
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Transaction List Card
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent transactions", // TODO: Localize "dashboard.empty_state.no_transactions"
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    transactions.forEachIndexed { index, entry ->
                        CashflowRow(
                            entry = entry,
                            index = index,
                            onClick = { onTransactionClick(entry.id) }
                        )
                        if (index < transactions.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 64.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CashflowRow(
    entry: CashflowEntry,
    index: Int,
    onClick: () -> Unit
) {
    // Animation logic
    var isVisible by remember { mutableStateOf(false) }
    val animatedOpacity by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "opacity"
    )
    val animatedOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 20.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "offset"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 50L) // Stagger effect
        isVisible = true
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animatedOpacity
                translationY = animatedOffset.toPx()
            }
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon with Gradient Background
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .background(gradientForType(entry.type), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconForEntry(entry),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                // Type Badge
                Box(
                    modifier = Modifier
                        .background(colorForType(entry.type), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (entry.type == CashflowType.INCOME) "INCOME" else "EXPENSE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = entry.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Amount & Time
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val amountColor = if (entry.type == CashflowType.INCOME) CashaSuccess else CashaDanger
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (entry.type == CashflowType.INCOME) "+" else "-",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = CurrencyFormatter.format(entry.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor
                )
            }
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(entry.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

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
    
    return when (entry.category.lowercase()) {
        "food", "food & dining" -> Icons.Default.Restaurant
        "shopping" -> Icons.Default.ShoppingBag
        "transport" -> Icons.Default.DirectionsCar
        "entertainment" -> Icons.Default.Movie
        "utilities" -> Icons.Default.Bolt
        "salary" -> Icons.Default.Payments
        "bonus" -> Icons.Default.AutoAwesome
        "investment" -> Icons.Default.TrendingUp
        else -> Icons.Default.CreditCard
    }
}
