package com.casha.app.ui.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.res.stringResource
import com.casha.app.R
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casha.app.domain.model.Asset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import android.net.Uri
import com.casha.app.navigation.NavRoutes
import com.casha.app.ui.component.CustomTabBar
import com.casha.app.ui.component.TabItem
import com.casha.app.ui.feature.dashboard.DashboardScreen
import com.casha.app.ui.feature.goaltracker.GoalTrackerScreen
import com.casha.app.ui.feature.budget.BudgetScreen
import com.casha.app.ui.feature.transaction.TransactionScreen
import com.casha.app.ui.feature.transaction.AddTransactionScreen
import com.casha.app.ui.feature.transaction.subview.TransactionDetailScreen
import com.casha.app.ui.feature.report.ReportScreen
import com.casha.app.ui.feature.report.subview.TransactionListByCategoryView
import com.casha.app.ui.feature.transaction.coordinator.AddMessageScreen
import com.casha.app.ui.feature.portfolio.AssetsScreen
import com.casha.app.ui.feature.portfolio.CreateAssetScreen
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    parentNavController: NavHostController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var activeNotification by remember { mutableStateOf<com.casha.app.domain.model.NotificationCasha?>(null) }

    LaunchedEffect(Unit) {
        viewModel.notificationEvents.collect { notification ->
            activeNotification = notification
            // Auto dismiss after a delay
            kotlinx.coroutines.delay(4000)
            if (activeNotification == notification) {
                activeNotification = null
            }
        }
    }

    // A separate effect to handle notification clicks if needed outside the display
    fun handleNotificationClick(notification: com.casha.app.domain.model.NotificationCasha) {
        // Navigate based on type
        val route = when (notification.type) {
            com.casha.app.domain.model.NotificationType.TRANSACTION_ADDED -> {
                val id = notification.data["transactionId"]
                val type = notification.data["cashflowType"] ?: "EXPENSE"
                if (id != null) NavRoutes.TransactionDetail.createRoute(id, type) else null
            }
            com.casha.app.domain.model.NotificationType.BUDGET_ALERT,
            com.casha.app.domain.model.NotificationType.BUDGET_CREATED -> NavRoutes.Budget.route
            com.casha.app.domain.model.NotificationType.MONTHLY_SUMMARY -> NavRoutes.Report.route
            else -> null
        }
        route?.let { navController.navigate(it) }
        activeNotification = null
    }

    var selectedTab by remember { mutableIntStateOf(1) } // Default to Home

    val navReport = stringResource(R.string.nav_report)
    val navHome = stringResource(R.string.nav_dashboard)
    val navAdd = stringResource(R.string.nav_add)
    val navTransactions = stringResource(R.string.nav_transactions)
    val navBudget = stringResource(R.string.nav_budget)

    val tabs = remember(navReport, navHome, navAdd, navTransactions, navBudget) {
        listOf(
            TabItem(
                title = navReport,
                icon = Icons.Outlined.PieChart,
                selectedIcon = Icons.Filled.PieChart,
                tag = 0
            ),
            TabItem(
                title = navHome,
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Filled.Home,
                tag = 1
            ),
            TabItem(
                title = navAdd,
                icon = Icons.Filled.Add,
                selectedIcon = Icons.Filled.Add,
                tag = 5,
                isCenterButton = true
            ),
            TabItem(
                title = navTransactions,
                icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                selectedIcon = Icons.AutoMirrored.Filled.FormatListBulleted,
                tag = 2
            ),
            TabItem(
                title = navBudget,
                icon = Icons.Outlined.CreditCard,
                selectedIcon = Icons.Filled.CreditCard,
                tag = 3
            )
        )
    }

    // Map tag -> NavRoute for tab navigation
    fun tagToRoute(tag: Int): String = when (tag) {
        0 -> NavRoutes.Report.route
        1 -> NavRoutes.Dashboard.route
        2 -> NavRoutes.Transactions.route
        3 -> NavRoutes.Budget.route
        else -> NavRoutes.Dashboard.route
    }

    var showCoordinator by remember { mutableStateOf(false) }
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var editTransactionId by remember { mutableStateOf<String?>(null) }
    var showProfileEditSheet by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showPaywallSheet by remember { mutableStateOf(false) }

    val subViewModel: com.casha.app.ui.feature.subscription.SubscriptionManager = hiltViewModel()
    val isPremium by subViewModel.hasPremiumAccess.collectAsState()

    val currentRoute = navBackStackEntry?.destination?.route

    val hideBottomBar = remember(currentRoute) {
        currentRoute == NavRoutes.Chat.route
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!hideBottomBar) {
                    CustomTabBar(
                        selectedTab = selectedTab,
                        onTabSelected = { tag ->
                            selectedTab = tag
                            navController.navigate(tagToRoute(tag)) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onTabDoubleTapped = { tag ->
                            if (tag == selectedTab) {
                                // Double tapped the currently active tab -> reset to its root
                                navController.popBackStack(tagToRoute(tag), inclusive = false)
                            } else {
                                // Double tapped a different tab -> treat like single tap
                                selectedTab = tag
                                navController.navigate(tagToRoute(tag)) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        tabs = tabs.map { tabItem ->
                            if (tabItem.isCenterButton) {
                                tabItem.copy(onAction = { showCoordinator = true })
                            } else {
                                tabItem
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
        com.casha.app.ui.feature.transaction.coordinator.AddTransactionCoordinator(
            isPresented = showCoordinator,
            onDismiss = { showCoordinator = false },
            onNavigate = { route ->
                if (route == NavRoutes.AddTransaction.route) {
                    editTransactionId = null
                    showAddTransactionSheet = true
                } else if (route == NavRoutes.Subscription.route) {
                    showPaywallSheet = true
                } else {
                    navController.navigate(route)
                }
                showCoordinator = false
            }
        )

        if (showAddTransactionSheet) {
            AddTransactionScreen(
                transactionId = editTransactionId,
                onNavigateBack = { showAddTransactionSheet = false }
            )
        }

        if (showProfileEditSheet) {
            val viewModel: com.casha.app.ui.feature.profile.ProfileViewModel = hiltViewModel()
            com.casha.app.ui.feature.profile.ProfileEditScreen(
                viewModel = viewModel,
                onNavigateBack = { showProfileEditSheet = false }
            )
        }

        if (showNotificationsSheet) {
            com.casha.app.ui.feature.profile.NotificationHistoryScreen(
                onBackClick = { showNotificationsSheet = false }
            )
        }

        // We use innerPadding.calculateTopPadding() but ignore bottom padding 
        // to allow content to scroll behind the floating transparent TabBar.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding(), bottom = if (hideBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.Dashboard.route
            ) {
                composable(NavRoutes.Dashboard.route) {
                    DashboardScreen(navController = navController)
                }
                composable(NavRoutes.GoalTracker.route) {
                    GoalTrackerScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAddGoal = { navController.navigate(NavRoutes.AddGoal.route) },
                        onNavigateToGoalDetail = { goalId -> navController.navigate(NavRoutes.GoalTrackerDetail.createRoute(goalId)) }
                    )
                }
                composable(
                    route = NavRoutes.GoalTrackerDetail.route,
                    arguments = listOf(navArgument("goalId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val goalId = backStackEntry.arguments?.getString("goalId") ?: ""
                    val viewModel: com.casha.app.ui.feature.goaltracker.GoalTrackerViewModel = hiltViewModel()
                    com.casha.app.ui.feature.goaltracker.GoalTrackerDetailScreen(
                        goalId = goalId,
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
               composable(NavRoutes.AddGoal.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(NavRoutes.GoalTracker.route)
                    }
                    val viewModel: com.casha.app.ui.feature.goaltracker.GoalTrackerViewModel = hiltViewModel(parentEntry)
                    com.casha.app.ui.feature.goaltracker.AddGoalScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(NavRoutes.Transactions.route) {
                    TransactionScreen(
                        onNavigate = { route -> 
                            if (route == NavRoutes.AddTransaction.route) {
                                editTransactionId = null
                                showAddTransactionSheet = true
                            } else {
                                navController.navigate(route)
                            }
                        },
                        onNavigateToEditTransaction = { id -> 
                            editTransactionId = id
                            showAddTransactionSheet = true
                        },
                        onNavigateToTransactionDetail = { id, type -> navController.navigate(NavRoutes.TransactionDetail.createRoute(id, type)) }
                    )
                }
                composable(
                    route = NavRoutes.TransactionDetail.route,
                    arguments = listOf(
                        navArgument("transactionId") { type = NavType.StringType },
                        navArgument("cashflowType") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("transactionId") ?: ""
                    val typeString = backStackEntry.arguments?.getString("cashflowType") ?: "EXPENSE"
                    val cashflowType = try {
                        com.casha.app.domain.model.CashflowType.valueOf(typeString.uppercase())
                    } catch (e: Exception) {
                        com.casha.app.domain.model.CashflowType.EXPENSE
                    }
                    TransactionDetailScreen(
                        transactionId = id,
                        cashflowType = cashflowType,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEdit = { editId ->
                            editTransactionId = editId
                            showAddTransactionSheet = true
                        }
                    )
                }
                composable(NavRoutes.Budget.route) { 
                    BudgetScreen()
                }
                composable(NavRoutes.Report.route) {
                    ReportScreen(
                        onNavigateToCategoryDetail = { category ->
                            navController.navigate(NavRoutes.ReportCategoryDetail.createRoute(category))
                        }
                    )
                }
                composable(
                    route = NavRoutes.ReportCategoryDetail.route,
                    arguments = listOf(navArgument("category") { type = NavType.StringType })
                ) { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category") ?: ""
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(NavRoutes.Report.route)
                    }
                    val viewModel: com.casha.app.ui.feature.report.ReportViewModel = hiltViewModel(parentEntry)
                    TransactionListByCategoryView(
                        category = category,
                        onBackClick = { navController.popBackStack() },
                        viewModel = viewModel
                    )
                }
                composable(NavRoutes.Profile.route) {
                    com.casha.app.ui.feature.profile.ProfileScreen(
                        onBackClick = { navController.popBackStack() },
                        onNavigateToEditProfile = { showProfileEditSheet = true },
                        onNavigateToNotifications = { showNotificationsSheet = true },
                        onNavigateToPortfolio = { 
                            if (isPremium) navController.navigate(NavRoutes.Portfolio.route)
                            else showPaywallSheet = true
                        },
                        onNavigateToLiabilities = { 
                            if (isPremium) navController.navigate(NavRoutes.Liabilities.route)
                            else showPaywallSheet = true
                        },
                        onNavigateToGoalTracker = { 
                            if (isPremium) navController.navigate(NavRoutes.GoalTracker.route)
                            else showPaywallSheet = true
                        },
                        onNavigateToCategories = { navController.navigate(NavRoutes.Categories.route) },
                        onNavigateToSubscription = {
                            if (isPremium) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "You are already a Premium user! 🌟",
                                        duration = androidx.compose.material3.SnackbarDuration.Short
                                    )
                                }
                            } else {
                                showPaywallSheet = true
                            }
                        },
                        onLogout = {
                            parentNavController.navigate(NavRoutes.Splash.route) {
                                popUpTo(NavRoutes.Dashboard.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Routes below are kept for navigation stability but logic is moved to modals above
                composable(NavRoutes.ProfileEdit.route) {
                    val viewModel: com.casha.app.ui.feature.profile.ProfileViewModel = hiltViewModel()
                    com.casha.app.ui.feature.profile.ProfileEditScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.Notifications.route) {
                    com.casha.app.ui.feature.profile.NotificationHistoryScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.Portfolio.route) {
                    val portfolioViewModel: com.casha.app.ui.feature.portfolio.PortfolioViewModel = hiltViewModel()
                    AssetsScreen(
                        viewModel = portfolioViewModel,
                        onNavigateToAssetDetail = { asset ->
                            navController.navigate(NavRoutes.AssetDetail.createRoute(asset.id))
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = NavRoutes.AssetDetail.route,
                    arguments = listOf(navArgument("assetId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val assetId = backStackEntry.arguments?.getString("assetId") ?: return@composable
                    val viewModel: com.casha.app.ui.feature.portfolio.PortfolioViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    
                    val matchingAsset = uiState.assets.firstOrNull { it.id == assetId }
                    
                    if (matchingAsset != null) {
                        com.casha.app.ui.feature.portfolio.AssetDetailScreen(
                            assetId = assetId,
                            initialAsset = matchingAsset,
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    } else {
                        // Fallback or loading if needed, though AssetsScreen should have loaded it
                        LaunchedEffect(assetId) {
                            viewModel.fetchPortfolioSummary()
                        }
                    }
                }


                composable(NavRoutes.Liabilities.route) {
                    val viewModel: com.casha.app.ui.feature.liability.LiabilityViewModel = hiltViewModel()
                    var showCategoryPicker by remember { mutableStateOf(false) }
                    var showCreateLiability by remember { mutableStateOf(false) }
                    var selectedCategory by remember { mutableStateOf<com.casha.app.domain.model.LiabilityCategory?>(null) }

                    com.casha.app.ui.feature.liability.LiabilitiesListScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDetail = { id -> navController.navigate(NavRoutes.LiabilityDetail.createRoute(id)) },
                        onNavigateToCreate = { showCategoryPicker = true }
                    )

                    if (showCategoryPicker) {
                        com.casha.app.ui.feature.liability.SelectLiabilityCategoryScreen(
                            onNavigateBack = { showCategoryPicker = false },
                            onCategorySelected = { category ->
                                selectedCategory = category
                                showCategoryPicker = false
                                showCreateLiability = true
                            }
                        )
                    }

                    if (showCreateLiability && selectedCategory != null) {
                        com.casha.app.ui.feature.liability.CreateLiabilityScreen(
                            selectedCategory = selectedCategory!!,
                            viewModel = viewModel,
                            onNavigateBack = { 
                                showCreateLiability = false
                                selectedCategory = null
                            }
                        )
                    }
                }

                composable(
                    route = NavRoutes.CreateLiabilityWithCategory.route,
                    arguments = listOf(navArgument("category") { type = NavType.StringType })
                ) { backStackEntry ->
                    val categoryStr = backStackEntry.arguments?.getString("category") ?: ""
                    val category = com.casha.app.domain.model.LiabilityCategory.fromValue(categoryStr)
                    val viewModel: com.casha.app.ui.feature.liability.LiabilityViewModel = hiltViewModel()
                    com.casha.app.ui.feature.liability.CreateLiabilityScreen(
                        selectedCategory = category,
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Legacy route kept for backward compatibility
                composable(NavRoutes.CreateLiability.route) {
                    val viewModel: com.casha.app.ui.feature.liability.LiabilityViewModel = hiltViewModel()
                    com.casha.app.ui.feature.liability.CreateLiabilityScreen(
                        onNavigateBack = { navController.popBackStack() },
                        viewModel = viewModel
                    )
                }

                composable(
                    route = NavRoutes.LiabilityDetail.route,
                    arguments = listOf(navArgument("liabilityId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val liabilityId = backStackEntry.arguments?.getString("liabilityId") ?: return@composable
                    val viewModel: com.casha.app.ui.feature.liability.LiabilityViewModel = hiltViewModel()
                    val txViewModel: com.casha.app.ui.feature.transaction.TransactionViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsState()
                    val txUiState by txViewModel.uiState.collectAsState()
                    
                    // The view expects `Liability` as initialLiability parameter, it can be passed via the list or fetched individually
                    // Usually we look inside the current state if provided
                    val matchingLiability = uiState.liabilities.firstOrNull { it.id == liabilityId } 
                    
                    if (matchingLiability != null) {
                        LaunchedEffect(liabilityId) {
                            viewModel.fetchPaymentHistory(liabilityId)
                            if (matchingLiability.category.isRevolving) {
                                viewModel.fetchLatestStatement(liabilityId)
                                viewModel.fetchAllStatements(liabilityId)
                                viewModel.fetchUnbilledTransactions(liabilityId)
                            }
                        }

                        com.casha.app.ui.feature.liability.LiabilityDetailView(
                            initialLiability = matchingLiability,
                            liabilityState = uiState,
                            onBack = { navController.popBackStack() },
                            onRecordPayment = { amount, paymentType, principal, interest, notes ->
                                viewModel.recordPayment(
                                    liabilityId = liabilityId,
                                    amount = amount,
                                    paymentType = paymentType,
                                    principal = principal,
                                    interest = interest,
                                    notes = notes,
                                    onSuccess = {}
                                )
                            },
                            onAddInstallment = { name, total, monthly, tenor, current, start ->
                                viewModel.addInstallment(liabilityId, name, total, monthly, tenor, current, start, onSuccess = {})
                            },
                            onSimulatePayoff = { strategy, additionalPayment ->
                                viewModel.simulatePayoff(strategy, additionalPayment)
                            },
                            onAddTransaction = {},
                            onCreateTransaction = { name, amount, category, description ->
                                viewModel.createTransaction(
                                    liabilityId = liabilityId,
                                    name = name,
                                    amount = amount,
                                    category = category,
                                    description = description,
                                    onSuccess = {}
                                )
                            },
                            categories = txUiState.categories,
                            onStatementClick = { statement ->
                                navController.navigate("statement_detail/$liabilityId/${statement.id}")
                            }
                        )
                    } else {
                        // Normally handle fetch state here. But as backup invoke list fetch
                        LaunchedEffect(liabilityId) { viewModel.fetchLiabilities() }
                    }
                }

                composable(
                    route = "statement_detail/{liabilityId}/{statementId}",
                    arguments = listOf(
                        navArgument("liabilityId") { type = NavType.StringType },
                        navArgument("statementId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val liabilityId = backStackEntry.arguments?.getString("liabilityId") ?: return@composable
                    val statementId = backStackEntry.arguments?.getString("statementId") ?: return@composable
                    val viewModel: com.casha.app.ui.feature.liability.LiabilityViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsState()
                    
                    val matchingLiability = uiState.liabilities.firstOrNull { it.id == liabilityId }
                    val matchingStatement = uiState.statements.firstOrNull { it.id == statementId } ?: uiState.latestStatement

                    if (matchingLiability != null && matchingStatement != null) {
                        com.casha.app.ui.feature.liability.StatementDetailView(
                            statement = matchingStatement,
                            liability = matchingLiability,
                            liabilityState = uiState,
                            onBack = { navController.popBackStack() },
                            onFetchStatementDetails = viewModel::fetchStatementDetails,
                            onTransactionClick = { tx ->
                                // Optional link to transaction detail if supported
                             }
                        )
                    }
                }

                composable(NavRoutes.Categories.route) {
                    com.casha.app.ui.feature.profile.CategoryListScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = NavRoutes.Chat.route,
                    arguments = listOf(
                        navArgument("imageUri") { 
                            type = NavType.StringType
                            nullable = true 
                        }
                    )
                ) { backStackEntry ->
                    val imageUriString = backStackEntry.arguments?.getString("imageUri")
                    val imageUri = imageUriString?.let { Uri.parse(java.net.URLDecoder.decode(it, "UTF-8")) }
                    
                    AddMessageScreen(
                        initialImageUri = imageUri,
                        onClose = { navController.popBackStack() }
                    )
                }



                composable(NavRoutes.Subscription.route) {
                    PlaceholderTab(title = "Upgrade to Premium ✨")
                }
            }
        }

        if (showPaywallSheet) {
            ModalBottomSheet(
modifier = Modifier.fillMaxSize(),
onDismissRequest = { showPaywallSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                dragHandle = null,
                containerColor = Color.Transparent,
                contentWindowInsets = { WindowInsets(0.dp) }
            ) {
                com.casha.app.ui.feature.subscription.PaywallScreen(
                    onDismiss = { showPaywallSheet = false }
                )
            }
        }
        
        // Render top floating notification banner if present
        androidx.compose.animation.AnimatedVisibility(
            visible = activeNotification != null,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { -it }) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }) + androidx.compose.animation.fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
        ) {
            activeNotification?.let { notif ->
                com.casha.app.ui.component.BudgetAlertBanner(
                    notification = notif,
                    onTap = { handleNotificationClick(notif) },
                    onDismiss = { activeNotification = null }
                )
            }
        }
        }
    }
}

@Composable
private fun PlaceholderTab(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}
