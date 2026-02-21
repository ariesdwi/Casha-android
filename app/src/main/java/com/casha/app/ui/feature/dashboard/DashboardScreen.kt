package com.casha.app.ui.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit,
    onNavigateToUnsyncedInfo: () -> Unit,
    onNavigateToGoalTracker: () -> Unit,
    onNavigateToGoalDetail: (String) -> Unit,
    onNavigateToTransactionDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
            LargeTopAppBar(
                title = {
                    Text(
                        text = "$greeting $nickname",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (uiState.unsyncedCount > 0) {
                        Box(modifier = Modifier.padding(start = 12.dp)) {
                            SyncBadge(
                                count = uiState.unsyncedCount,
                                isSyncing = uiState.isSyncing,
                                onClick = onNavigateToUnsyncedInfo
                            )
                        }
                    }
                },
                actions = {
                    if (!uiState.isOnline) {
                        OfflineIndicator()
                    } else {
                        Surface(
                            onClick = onNavigateToProfile,
                            shape = CircleShape,
                            color = Color.Transparent,
                            modifier = Modifier.padding(end = 12.dp).size(36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF2E7D32),
                                                Color(0xFF66BB6A)
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                CardBalanceSection(
                    summary = uiState.cashflowSummary,
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodChange = { viewModel.changePeriod(it) }
                )
            }
            item {
                ReportSection(
                    report = uiState.report,
                    selectedTab = uiState.selectedChartTab,
                    onTabChange = { viewModel.changeChartTab(it) },
                    isSyncing = uiState.isSyncing
                )
            }
            item {
                GoalSection(
                    goals = uiState.goals,
                    onSeeAllClick = onNavigateToGoalTracker,
                    onGoalClick = onNavigateToGoalDetail
                )
            }
            item {
                RecentTransactionsSection(
                    transactions = uiState.recentTransactions,
                    onTransactionClick = onNavigateToTransactionDetail
                )
            }
            item { Spacer(modifier = Modifier.height(40.dp)) }
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
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
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
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OfflineIndicator() {
    Surface(
        color = Color(0xFFFFA500).copy(alpha = 0.1f),
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
                tint = Color(0xFFFFA500)
            )
            Text(
                text = "Offline",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFFA500),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
