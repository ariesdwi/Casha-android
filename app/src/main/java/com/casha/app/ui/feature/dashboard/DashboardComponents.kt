package com.casha.app.ui.feature.dashboard
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.res.stringResource
import com.casha.app.R
import com.casha.app.domain.model.*
import com.casha.app.ui.theme.*
import com.casha.app.ui.feature.transaction.CashflowUiUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardBalanceSection(
    summary: CashflowSummary?,
    selectedPeriod: SpendingPeriod,
    onPeriodChange: (SpendingPeriod) -> Unit
) {
    var isBalanceVisible by remember { mutableStateOf(true) }
    var showPeriodPicker by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    val netBalanceValue = summary?.netBalance ?: 0.0
    val amountColor = when {
        netBalanceValue > 0 -> CashaSuccess
        netBalanceValue < 1 -> CashaDanger
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Label
                Text(
                    text = stringResource(R.string.dashboard_balance_net_cashflow),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )

                // Amount Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = if (isBalanceVisible) CurrencyFormatter.format(summary?.netBalance ?: 0.0) else "••••••••",
                        style = MaterialTheme.typography.displayMedium,
                        color = amountColor,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = { isBalanceVisible = !isBalanceVisible },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Balance",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Period Selector Button
                Surface(
                    onClick = { showPeriodPicker = true },
                    shape = RoundedCornerShape(50.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = getPeriodTitle(selectedPeriod),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mini Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MiniStatItem(
                        title = stringResource(R.string.dashboard_balance_in),
                        amount = summary?.totalIncome ?: 0.0,
                        icon = Icons.Default.NorthEast,
                        color = CashaSuccess,
                        isVisible = isBalanceVisible,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    )

                    MiniStatItem(
                        title = stringResource(R.string.dashboard_balance_out),
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
            onDismiss = { showPeriodPicker = false },
            onCustomClick = {
                showPeriodPicker = false
                showDateRangePicker = true
            }
        )
    }

    if (showDateRangePicker) {
        val initialStart = if (selectedPeriod is SpendingPeriod.CUSTOM) selectedPeriod.start.time else null
        val initialEnd = if (selectedPeriod is SpendingPeriod.CUSTOM) selectedPeriod.end.time else null
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = initialStart,
            initialSelectedEndDateMillis = initialEnd
        )
        ModalBottomSheet(
modifier = Modifier.fillMaxSize(),
onDismissRequest = { showDateRangePicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showDateRangePicker = false }) {
                        Text(stringResource(R.string.add_transaction_cancel))
                    }
                    Text(
                        text = stringResource(R.string.transactions_filter_select_date_range),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(
                        onClick = {
                            val startMillis = dateRangePickerState.selectedStartDateMillis
                            val endMillis = dateRangePickerState.selectedEndDateMillis
                            if (startMillis != null && endMillis != null) {
                                // Material3 DateRangePicker returns UTC midnight millis.
                                // Extract year/month/day in UTC, then build local midnight dates.
                                val utcStart = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = startMillis }
                                val utcEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = endMillis }
                                val startDate = Calendar.getInstance().apply {
                                    set(utcStart.get(Calendar.YEAR), utcStart.get(Calendar.MONTH), utcStart.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.time
                                val endDate = Calendar.getInstance().apply {
                                    set(utcEnd.get(Calendar.YEAR), utcEnd.get(Calendar.MONTH), utcEnd.get(Calendar.DAY_OF_MONTH), 23, 59, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }.time
                                onPeriodChange(SpendingPeriod.CUSTOM(startDate, endDate))
                            }
                            showDateRangePicker = false
                        },
                        enabled = dateRangePickerState.selectedStartDateMillis != null &&
                                  dateRangePickerState.selectedEndDateMillis != null
                    ) {
                        Text(stringResource(R.string.profile_action_done))
                    }
                }
                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier.fillMaxWidth().height(500.dp),
                    title = null,
                    headline = null,
                    showModeToggle = false
                )
            }
        }
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = if (isVisible) CurrencyFormatter.format(amount) else "••••",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodPickerBottomSheet(
    selectedPeriod: SpendingPeriod,
    onPeriodSelected: (SpendingPeriod) -> Unit,
    onDismiss: () -> Unit,
    onCustomClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
modifier = Modifier.fillMaxSize(),
onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboard_period_select),
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
 // Custom Range option
            ListItem(
                headlineContent = { Text(stringResource(R.string.transactions_filter_custom_range)) },
                trailingContent = {
                    if (selectedPeriod is SpendingPeriod.CUSTOM) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                modifier = Modifier.clickable { onCustomClick() }
            )
            periods.forEach { period ->
                val title = getPeriodTitle(period)
                ListItem(
                    headlineContent = { Text(title) },
                    trailingContent = {
                        if (selectedPeriod == period) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.clickable { onPeriodSelected(period) }
                )
            }

           
        }
    }
}

@Composable
fun getPeriodTitle(period: SpendingPeriod): String {
    return when (period) {
        SpendingPeriod.THIS_WEEK -> "This Week"
        SpendingPeriod.THIS_MONTH -> stringResource(R.string.dashboard_period_this_month)
        SpendingPeriod.LAST_MONTH -> stringResource(R.string.dashboard_period_last_month)
        SpendingPeriod.LAST_THREE_MONTHS -> "Last 3 Months"
        SpendingPeriod.THIS_YEAR -> stringResource(R.string.dashboard_period_this_year)
        SpendingPeriod.ALL_TIME -> "All Time"
        SpendingPeriod.FUTURE -> "Future"
        is SpendingPeriod.CUSTOM -> {
            val formatter = SimpleDateFormat("d MMM", Locale.getDefault())
            "${formatter.format(period.start)} – ${formatter.format(period.end)}"
        }
    }
}

@Composable
fun ReportSection(
    report: SpendingReport,
    selectedTab: ChartTab,
    onTabChange: (ChartTab) -> Unit,
    isSyncing: Boolean,
    onSeeAllClick: () -> Unit = {}
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
                text = stringResource(R.string.dashboard_spending_title),
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
            } else {
                TextButton(onClick = onSeeAllClick) {
                    Text(
                        text = stringResource(R.string.dashboard_goals_see_all),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
                    title = stringResource(R.string.report_filter_week),
                    isSelected = selectedTab == ChartTab.WEEK,
                    onClick = { onTabChange(ChartTab.WEEK) },
                    modifier = Modifier.weight(1f)
                )
                ChartToggleButton(
                    title = stringResource(R.string.report_filter_month),
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
                                            color = if (index % 2 == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
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
                text = stringResource(R.string.dashboard_goals_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onSeeAllClick) {
                Text(
                    text = stringResource(R.string.dashboard_goals_see_all),
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
                    text = stringResource(R.string.dashboard_empty_state_no_goals),
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
    onTransactionClick: (String, String) -> Unit
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
                text = stringResource(R.string.dashboard_transactions_recent),
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
                            text = stringResource(R.string.dashboard_empty_state_no_transactions),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    transactions.forEachIndexed { index, entry ->
                        CashflowRow(
                            entry = entry,
                            index = index,
                            onClick = { onTransactionClick(entry.id, entry.type.name) }
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
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .background(CashflowUiUtils.gradientForType(entry.type), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = CashflowUiUtils.iconForEntry(entry),
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
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // Type Badge
                Box(
                    modifier = Modifier
                        .background(CashflowUiUtils.colorForType(entry.type), RoundedCornerShape(4.dp))
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
            val amountColor = CashflowUiUtils.colorForType(entry.type)
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
