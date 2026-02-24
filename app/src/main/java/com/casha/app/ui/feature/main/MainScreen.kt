package com.casha.app.ui.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MainScreen(
    parentNavController: NavHostController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.notificationEvents.collect { notification ->
            val result = snackbarHostState.showSnackbar(
                message = "${notification.title}: ${notification.body}",
                actionLabel = "View",
                duration = androidx.compose.material3.SnackbarDuration.Long
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
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
            }
        }
    }

    var selectedTab by remember { mutableIntStateOf(1) } // Default to Home

    val tabs = remember {
        listOf(
            TabItem(
                title = "Report",
                icon = Icons.Outlined.PieChart,
                selectedIcon = Icons.Filled.PieChart,
                tag = 0
            ),
            TabItem(
                title = "Home",
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Filled.Home,
                tag = 1
            ),
            TabItem(
                title = "Add",
                icon = Icons.Filled.Add,
                selectedIcon = Icons.Filled.Add,
                tag = 5,
                isCenterButton = true
            ),
            TabItem(
                title = "Transactions",
                icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                selectedIcon = Icons.AutoMirrored.Filled.FormatListBulleted,
                tag = 2
            ),
            TabItem(
                title = "Budget",
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

    val currentRoute = navBackStackEntry?.destination?.route

    val hideBottomBar = remember(currentRoute) {
        currentRoute == NavRoutes.Chat.route ||
        currentRoute == NavRoutes.ReceiptCamera.route
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
                    TransactionListByCategoryView(
                        category = category,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(NavRoutes.Profile.route) {
                    com.casha.app.ui.feature.profile.ProfileScreen(
                        onBackClick = { navController.popBackStack() },
                        onNavigateToEditProfile = { navController.navigate(NavRoutes.ProfileEdit.route) },
                        onNavigateToNotifications = { /* TODO */ },
                        onNavigateToPortfolio = { navController.navigate(NavRoutes.Portfolio.route) },
                        onNavigateToLiabilities = { navController.navigate(NavRoutes.Liabilities.route) },
                        onNavigateToGoalTracker = { navController.navigate(NavRoutes.GoalTracker.route) },
                        onNavigateToCategories = { navController.navigate(NavRoutes.Categories.route) },
                        onNavigateToSubscription = { navController.navigate(NavRoutes.Subscription.route) },
                        onLogout = {
                            parentNavController.navigate(NavRoutes.Splash.route) {
                                popUpTo(NavRoutes.Dashboard.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(NavRoutes.Liabilities.route) {
                    val viewModel: com.casha.app.ui.feature.liability.LiabilityViewModel = hiltViewModel()
                    com.casha.app.ui.feature.liability.LiabilitiesListScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDetail = { id -> navController.navigate(NavRoutes.LiabilityDetail.createRoute(id)) },
                        onNavigateToCreate = { navController.navigate(NavRoutes.CreateLiability.route) }
                    )
                }

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
                    val uiState by viewModel.uiState.collectAsState()
                    
                    // The view expects `Liability` as initialLiability parameter, it can be passed via the list or fetched individually
                    // Usually we look inside the current state if provided
                    val matchingLiability = uiState.liabilities.firstOrNull { it.id == liabilityId } 
                    
                    if (matchingLiability != null) {
                        com.casha.app.ui.feature.liability.LiabilityDetailView(
                            initialLiability = matchingLiability,
                            liabilityState = uiState,
                            onBack = { navController.popBackStack() },
                            onRecordPayment = { amount, paymentType ->
                                viewModel.recordPayment(liabilityId, amount, paymentType, onSuccess = {})
                            },
                            onAddTransaction = {
                                // You might need a specific AddLiabilityTransactionScreen route or use the main AddTransaction mechanism
                            },
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

                composable(NavRoutes.ReceiptCamera.route) {
                    PlaceholderTab(title = "Scan Receipt (Premium ✨)")
                }

                composable(NavRoutes.Subscription.route) {
                    PlaceholderTab(title = "Upgrade to Premium ✨")
                }
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
