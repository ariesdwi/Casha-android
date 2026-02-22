package com.casha.app.ui.feature.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.casha.app.navigation.Screen
import com.casha.app.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val calendar = remember { Calendar.getInstance() }
    val greeting = remember {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    val nickname = remember(uiState.nickname) {
        uiState.nickname.split(" ").firstOrNull() ?: "User"
    }

    val initial = remember(uiState.nickname) {
        uiState.nickname.firstOrNull()?.uppercase() ?: "U"
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (scrollBehavior.state.overlappedFraction > 0.5f) {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    if (!uiState.isOnline) {
                        OfflineIndicator()
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (uiState.unsyncedCount > 0) {
                        SyncBadge(
                            count = uiState.unsyncedCount,
                            isSyncing = uiState.isSyncing,
                            onClick = { navController.navigate(Screen.UnsyncedInfo.route) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                },
                windowInsets = WindowInsets(0.dp),
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                WelcomeHeader(
                    greeting = greeting,
                    nickname = nickname,
                    initial = initial,
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }
            
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CardBalanceSection(
                        summary = uiState.cashflowSummary,
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodChange = { viewModel.changePeriod(it) }
                    )
                }
            }
            
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ReportSection(
                        report = uiState.report,
                        selectedTab = uiState.selectedChartTab,
                        onTabChange = { viewModel.changeChartTab(it) },
                        isSyncing = uiState.isSyncing
                    )
                }
            }
            
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    GoalSection(
                        goals = uiState.goals,
                        onSeeAllClick = { navController.navigate(Screen.GoalTracker.route) },
                        onGoalClick = { goalId -> navController.navigate(Screen.GoalDetail(goalId).route) }
                    )
                }
            }
            
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    RecentTransactionsSection(
                        transactions = uiState.recentTransactions,
                        onTransactionClick = { transactionId, cashflowType ->
                            navController.navigate(Screen.TransactionDetail(transactionId, cashflowType).route)
                        }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun WelcomeHeader(
    greeting: String,
    nickname: String,
    initial: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = nickname,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Surface(
            onClick = onProfileClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.size(44.dp),
            tonalElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SyncBadge(
    count: Int,
    isSyncing: Boolean,
    onClick: () -> Unit
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

    Surface(
        onClick = onClick,
        color = CashaBlue.copy(alpha = 0.15f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .then(if (isSyncing) Modifier.rotate(rotation) else Modifier),
                tint = CashaBlue
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = CashaBlue,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OfflineIndicator() {
    Surface(
        color = CashaWarning.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = CashaWarning
            )
            Text(
                text = "Offline",
                style = MaterialTheme.typography.labelSmall,
                color = CashaWarning,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
