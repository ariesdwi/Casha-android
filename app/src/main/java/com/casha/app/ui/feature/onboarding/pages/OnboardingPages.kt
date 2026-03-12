package com.casha.app.ui.feature.onboarding.pages

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.casha.app.R
import com.casha.app.ui.feature.onboarding.components.BulletPoint
import com.casha.app.ui.feature.onboarding.components.CoachmarkContainer
import com.casha.app.ui.feature.onboarding.components.OnboardingPremiumFeature
import com.casha.app.ui.feature.onboarding.model.OnboardingMockData
import com.casha.app.domain.model.SpendingPeriod
import com.casha.app.ui.feature.dashboard.CardBalanceSection
import com.casha.app.ui.feature.transaction.subview.TransactionListItem
import com.casha.app.ui.feature.budget.subview.BudgetOverviewCard
import com.casha.app.ui.feature.budget.subview.BudgetCardItem
import com.casha.app.ui.feature.portfolio.subviews.PortfolioSummaryCard
import com.casha.app.ui.feature.portfolio.subviews.AssetRow
import com.casha.app.ui.feature.liability.LiabilitySummaryCard
import com.casha.app.ui.feature.liability.LiabilityRow
import com.casha.app.ui.feature.goaltracker.GoalSummaryGrid
import com.casha.app.ui.feature.goaltracker.GoalPreviewCard
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════
// PAGE 1: Hook
// ═══════════════════════════════════════════
@Composable
fun HookPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Illustration
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        CoachmarkContainer(
            titleRes = R.string.onboarding_hook_title,
            subtitleRes = R.string.onboarding_hook_subtitle
        ) {
            BulletPoint(R.string.onboarding_hook_bullet1)
            BulletPoint(R.string.onboarding_hook_bullet2)
            BulletPoint(R.string.onboarding_hook_bullet3)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ═══════════════════════════════════════════
// PAGE 2: Chat Input
// ═══════════════════════════════════════════
@Composable
fun ChatInputPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Mock Chat UI Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Chat header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.onboarding_chat_mock_ai_name),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                        )
                        Text(
                            text = stringResource(R.string.onboarding_chat_mock_online),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // Chat bubbles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Timestamp
                Text(
                    text = stringResource(R.string.onboarding_chat_mock_timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // User bubble
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = stringResource(R.string.onboarding_chat_mock_user_msg),
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }

                // AI bubble
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_chat_mock_ai_confirm),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Coffee,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.onboarding_chat_mock_ai_result),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Read receipt
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Text(
                            text = stringResource(R.string.onboarding_chat_mock_recorded),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // Fake input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text(
                        text = stringResource(R.string.onboarding_chat_mock_input_placeholder),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        CoachmarkContainer(
            titleRes = R.string.onboarding_chat_title,
            subtitleRes = R.string.onboarding_chat_subtitle
        ) {
            BulletPoint(R.string.onboarding_chat_bullet1)
            BulletPoint(R.string.onboarding_chat_bullet2)
            BulletPoint(R.string.onboarding_chat_bullet3)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ═══════════════════════════════════════════
// PAGE 3: Revelation (Dashboard)
// ═══════════════════════════════════════════
@Composable
fun RevelationPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Mock Dashboard Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp)
                .scale(0.9f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val context = LocalContext.current
            // Real CardBalanceSection
            CardBalanceSection(
                summary = OnboardingMockData.mockDashboardSummary(context),
                selectedPeriod = SpendingPeriod.THIS_MONTH,
                onPeriodChange = {}
            )

            // Mock chart bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val dayRes = listOf(
                    R.string.day_mon, R.string.day_tue, R.string.day_wed,
                    R.string.day_thu, R.string.day_fri, R.string.day_sat, R.string.day_sun
                )
                val heights = listOf(30, 40, 65, 45, 80, 100, 70)
                dayRes.forEachIndexed { idx, resId ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(heights[idx].dp)
                                .background(
                                    color = if (idx == 5) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        Text(stringResource(resId), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        CoachmarkContainer(titleRes = R.string.onboarding_revelation_title) {
            BulletPoint(R.string.onboarding_revelation_bullet1)
            BulletPoint(R.string.onboarding_revelation_bullet2)
            BulletPoint(R.string.onboarding_revelation_bullet3)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ═══════════════════════════════════════════
// PAGE 4: Transaction History
// ═══════════════════════════════════════════
@Composable
fun TransactionHistoryPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Mock transaction list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_history_recent_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 8.dp)
            ) {
                val context = LocalContext.current
                val transactions = OnboardingMockData.mockTransactions(context)
                transactions.forEachIndexed { idx, transaction ->
                    TransactionListItem(
                        entry = transaction,
                        onClick = {}
                    )
                    if (idx < transactions.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                        )
                    }
                }
            }
        }

        CoachmarkContainer(
            titleRes = R.string.onboarding_history_title,
            subtitleRes = R.string.onboarding_history_subtitle
        ) {
            BulletPoint(R.string.onboarding_history_bullet1)
            BulletPoint(R.string.onboarding_history_bullet2)
            BulletPoint(R.string.onboarding_history_bullet3)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ═══════════════════════════════════════════
// PAGE 5: Budget Tracker
// ═══════════════════════════════════════════
@Composable
fun BudgetTrackerPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Real Budget Overview
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp)
                .scale(0.9f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val context = LocalContext.current
            BudgetOverviewCard(
                summary = OnboardingMockData.mockBudgetSummary(context)
            )

            OnboardingMockData.mockBudgets(context).forEach { budget ->
                BudgetCardItem(
                    budget = budget,
                    onDelete = {},
                    onEdit = {}
                )
            }
        }

        CoachmarkContainer(
            titleRes = R.string.onboarding_budget_title,
            subtitleRes = R.string.onboarding_budget_subtitle
        ) {
            BulletPoint(R.string.onboarding_budget_bullet1)
            BulletPoint(R.string.onboarding_budget_bullet2)
            BulletPoint(R.string.onboarding_budget_bullet3)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ═══════════════════════════════════════════
// PAGE 6: Portfolio
// ═══════════════════════════════════════════
@Composable
fun PortfolioPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp)
                .scale(0.9f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val context = LocalContext.current
            PortfolioSummaryCard(
                summary = OnboardingMockData.mockPortfolioSummary(context),
                isLoading = false,
                onTap = {}
            )

            // Asset rows
            OnboardingMockData.mockAssetSummaryList(context).forEach { asset ->
                AssetRow(
                    asset = asset,
                    onClick = {}
                )
            }
        }

        CoachmarkContainer(
            titleRes = R.string.onboarding_portfolio_title,
            subtitleRes = R.string.onboarding_portfolio_subtitle
        ) {
            BulletPoint(R.string.onboarding_portfolio_bullet1)
            BulletPoint(R.string.onboarding_portfolio_bullet2)
            BulletPoint(R.string.onboarding_portfolio_bullet3)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ═══════════════════════════════════════════
// PAGE 7: Liabilities
// ═══════════════════════════════════════════
@Composable
fun LiabilitiesPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp)
                .scale(0.9f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val context = LocalContext.current
            val summary = OnboardingMockData.mockLiabilitySummary(context)
            LiabilitySummaryCard(
                totalBalance = summary.totalDebt,
                totalMonthlyInstallment = summary.totalMonthlyPayment,
                activeCount = summary.activeLoansCount,
                paidOffCount = 0,
                isLoading = false,
                onTap = {}
            )

            summary.liabilities.forEach { liability ->
                LiabilityRow(
                    liability = liability,
                    onClick = {}
                )
            }
        }

        CoachmarkContainer(
            titleRes = R.string.onboarding_liabilities_title,
            subtitleRes = R.string.onboarding_liabilities_subtitle
        ) {
            BulletPoint(R.string.onboarding_liabilities_bullet1)
            BulletPoint(R.string.onboarding_liabilities_bullet2)
            BulletPoint(R.string.onboarding_liabilities_bullet3)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ═══════════════════════════════════════════
// PAGE 8: Goal Tracker
// ═══════════════════════════════════════════
@Composable
fun GoalTrackerPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp)
                .scale(0.9f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val context = LocalContext.current
            val goal = OnboardingMockData.mockGoal(context)
            GoalSummaryGrid(
                summary = OnboardingMockData.mockGoalSummary(context),
                currency = stringResource(R.string.onboarding_mock_currency)
            )

            GoalPreviewCard(
                name = goal.name,
                targetAmount = goal.targetAmount.toString(),
                icon = goal.category.icon ?: "🏠",
                color = Color(0xFF2196F3),
                userCurrency = stringResource(R.string.onboarding_mock_currency),
                categoryName = goal.category.name
            )
        }

        CoachmarkContainer(titleRes = R.string.onboarding_goal_title) {
            BulletPoint(R.string.onboarding_goal_bullet1)
            BulletPoint(R.string.onboarding_goal_bullet2)
            BulletPoint(R.string.onboarding_goal_bullet3)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ═══════════════════════════════════════════
// PAGE 9: Global Power
// ═══════════════════════════════════════════
@Composable
fun GlobalPowerPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Globe orbit animation
        GlobeOrbitView(
            modifier = Modifier
                .size(260.dp)
                .fillMaxWidth()
        )

        // Currency marquee
        CurrencyMarqueeStrip()

        CoachmarkContainer(titleRes = R.string.onboarding_global_title) {
            BulletPoint(R.string.onboarding_global_bullet1)
            BulletPoint(R.string.onboarding_global_bullet2)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun GlobeOrbitView(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "globe")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "globeRotation"
    )

    var activeIndex by remember { mutableIntStateOf(0) }
    val flags = listOf(
        "🇮🇩" to "IDR", "🇺🇸" to "USD", "🇯🇵" to "JPY",
        "🇩🇪" to "EUR", "🇧🇷" to "BRL", "🇮🇳" to "INR",
        "🇰🇷" to "KRW", "🇸🇦" to "SAR", "🇨🇳" to "CNY"
    )

    val orbitRadius = 90f

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1500)
            activeIndex = (activeIndex + 1) % flags.size
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Orbit ring
        Box(
            modifier = Modifier
                .size((orbitRadius * 2 + 40).dp)
                .background(Color.Transparent, CircleShape)
        )

        // Orbiting flags
        flags.forEachIndexed { idx, (flag, code) ->
            val angle = Math.toRadians(
                (idx.toDouble() * (360.0 / flags.size) + rotation).toDouble()
            )
            val isActive = activeIndex == idx
            val flagScale by animateFloatAsState(
                targetValue = if (isActive) 1.2f else 0.9f,
                animationSpec = tween(300),
                label = "flagScale$idx"
            )
            val flagAlpha by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.4f,
                animationSpec = tween(300),
                label = "flagAlpha$idx"
            )

            Column(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = (orbitRadius * cos(angle)).toFloat() * density
                        translationY = (orbitRadius * sin(angle)).toFloat() * density
                        scaleX = flagScale
                        scaleY = flagScale
                        alpha = flagAlpha
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(flag, fontSize = if (isActive) 20.sp else 14.sp)
                Text(
                    code,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Central globe
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = null,
                modifier = Modifier
                    .size(55.dp)
                    .rotate(rotation * 0.5f),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CurrencyMarqueeStrip() {
    val codes = listOf("IDR", "USD", "JPY", "EUR", "GBP", "BRL", "INR", "KRW", "SAR", "CAD", "CNY", "AUD", "SGD", "HKD")
    val infiniteTransition = rememberInfiniteTransition(label = "marquee")
    
    // We use a larger list to ensure no gaps and smooth looping
    val displayCodes = codes + codes + codes
    
    var contentWidth by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    // Animate from 0 to -contentWidth/3 (since we have 3 copies)
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "xOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    contentWidth = coordinates.size.width.toFloat()
                }
                .graphicsLayer {
                    translationX = xOffset * (contentWidth / 3f)
                },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            displayCodes.forEach { code ->
                CurrencyBadge(code)
            }
        }
    }
}

@Composable
private fun CurrencyBadge(code: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = code,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

// ═══════════════════════════════════════════
// PAGE 10: Offline Sync
// ═══════════════════════════════════════════
@Composable
fun OfflineSyncPage(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Cloud sync animation
        CloudSyncView(modifier = Modifier.height(100.dp))

        CoachmarkContainer(titleRes = R.string.onboarding_sync_title) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.onboarding_sync_premium_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OnboardingPremiumFeature(R.string.onboarding_sync_feature1)
                OnboardingPremiumFeature(R.string.onboarding_sync_feature2)
                OnboardingPremiumFeature(R.string.onboarding_sync_feature3)
                OnboardingPremiumFeature(R.string.onboarding_sync_feature4)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Start button
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(R.string.onboarding_sync_start_button),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CloudSyncView(modifier: Modifier = Modifier) {
    var phase by remember { mutableIntStateOf(0) } // 0=offline, 1=syncing, 2=done

    LaunchedEffect(Unit) {
        while (true) {
            phase = 0
            kotlinx.coroutines.delay(1500)
            phase = 1
            kotlinx.coroutines.delay(2000)
            phase = 2
            kotlinx.coroutines.delay(1500)
        }
    }

    val statusColor = when (phase) {
        0 -> Color(0xFFFF9800)
        1 -> MaterialTheme.colorScheme.primary
        else -> Color(0xFF4CAF50)
    }
    val statusIcon = when (phase) {
        0 -> Icons.Default.CloudOff
        1 -> Icons.Default.CloudSync
        else -> Icons.Default.CloudDone
    }
    val statusLabelRes = when (phase) {
        0 -> R.string.onboarding_sync_status_offline
        1 -> R.string.onboarding_sync_status_syncing
        else -> R.string.onboarding_sync_status_done
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (phase == 1) 1.1f else 1.0f,
        animationSpec = tween(500),
        label = "pulse"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulseScale)
                    .background(statusColor.copy(alpha = 0.1f), CircleShape)
            )
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = statusColor
            )
        }

        // Status pill
        Row(
            modifier = Modifier
                .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(statusColor, CircleShape)
            )
            Text(
                text = stringResource(statusLabelRes),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}
