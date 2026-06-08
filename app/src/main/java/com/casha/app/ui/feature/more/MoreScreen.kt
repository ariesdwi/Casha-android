package com.casha.app.ui.feature.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.BuildConfig
import com.casha.app.R
import com.casha.app.ui.feature.more.components.MoreHeaderView
import com.casha.app.ui.feature.more.components.MoreMenuCard
import com.casha.app.ui.feature.profile.ProfileViewModel
import com.casha.app.ui.theme.CashaDanger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToWallets: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToLiabilities: () -> Unit,
    onNavigateToGoalTracker: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Handle logout
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.more_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp),
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error Banner
                if (uiState.errorMessage != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CashaDanger,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = Color.White,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Profile Header
                item {
                    MoreHeaderView(
                        profile = uiState.profile,
                        isPremium = uiState.isPremium,
                        onTap = onNavigateToEditProfile
                    )
                }

                // Grid Row 1: Budget & My Wallets
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            MoreMenuCard(
                                icon = Icons.Default.PieChart,
                                title = "Budget",
                                description = "Track spending & manage budget",
                                iconTint = Color(0xFF4CAF50),
                                iconBackgroundColor = Color(0xFFE8F5E9),
                                onClick = onNavigateToBudget
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            MoreMenuCard(
                                icon = Icons.Default.AccountBalanceWallet,
                                title = stringResource(R.string.more_menu_wallets),
                                description = "View and manage your wallets",
                                iconTint = Color(0xFF2196F3),
                                iconBackgroundColor = Color(0xFFE3F2FD),
                                badge = "1", // TODO: Connect to wallet count
                                onClick = onNavigateToWallets
                            )
                        }
                    }
                }

                // Grid Row 2: Goal Tracker & Portfolio
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            MoreMenuCard(
                                icon = Icons.Default.Flag,
                                title = stringResource(R.string.profile_menu_goal_tracker),
                                description = "Monitor and achieve your goals",
                                iconTint = Color(0xFFFF9800),
                                iconBackgroundColor = Color(0xFFFFF3E0),
                                isLocked = !uiState.isPremium,
                                onClick = onNavigateToGoalTracker
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            MoreMenuCard(
                                icon = Icons.Default.Work,
                                title = stringResource(R.string.profile_menu_portfolio),
                                description = "Manage your assets and investments",
                                iconTint = Color(0xFF9C27B0),
                                iconBackgroundColor = Color(0xFFF3E5F5),
                                isLocked = !uiState.isPremium,
                                onClick = onNavigateToPortfolio
                            )
                        }
                    }
                }

                // Grid Row 3: Liabilities & Categories
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            MoreMenuCard(
                                icon = Icons.Default.CreditCard,
                                title = stringResource(R.string.profile_menu_liabilities),
                                description = "Track and manage your debts",
                                iconTint = Color(0xFFF44336),
                                iconBackgroundColor = Color(0xFFFFEBEE),
                                isLocked = !uiState.isPremium,
                                onClick = onNavigateToLiabilities
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            MoreMenuCard(
                                icon = Icons.Default.Label,
                                title = stringResource(R.string.profile_menu_manage_categories),
                                description = "Customize your transaction categories",
                                iconTint = Color(0xFF00BCD4),
                                iconBackgroundColor = Color(0xFFE0F7FA),
                                onClick = onNavigateToCategories
                            )
                        }
                    }
                }

                // Spacer before bottom section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Developer Tools (DEBUG only)
                if (BuildConfig.DEBUG) {
                    item {
                        Surface(
                            onClick = { viewModel.togglePremiumDebug() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BugReport,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "Toggle Premium (Debug)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Current: ${if (uiState.isPremium) "Premium" else "Free"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (uiState.isPremium) Color(0xFF4CAF50) else Color.Gray
                                ) {
                                    Text(
                                        text = if (uiState.isPremium) "ON" else "OFF",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Logout Button
                item {
                    Surface(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CashaDanger.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                tint = CashaDanger,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = stringResource(R.string.profile_action_logout),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = CashaDanger
                            )
                        }
                    }
                }

                // Delete Account Button
                item {
                    Surface(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = CashaDanger,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = stringResource(R.string.profile_action_delete_account),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = CashaDanger
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Delete Account Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.profile_delete_confirm_title)) },
            text = { Text(stringResource(R.string.profile_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = CashaDanger)
                ) {
                    Text(stringResource(R.string.profile_action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.profile_action_cancel))
                }
            }
        )
    }
}
