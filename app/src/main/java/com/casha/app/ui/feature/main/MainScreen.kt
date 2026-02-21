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
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.casha.app.navigation.NavRoutes
import com.casha.app.ui.component.CustomTabBar
import com.casha.app.ui.component.TabItem
import com.casha.app.ui.feature.dashboard.DashboardScreen
import com.casha.app.ui.feature.goaltracker.GoalTrackerScreen

@Composable
fun MainScreen(
    parentNavController: NavHostController
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    var selectedTab by remember { mutableIntStateOf(1) } // Default to Home
    var showAddTransaction by remember { mutableStateOf(false) }

    val tabs = remember {
        listOf(
            TabItem(
                title = "Wealth",
                icon = Icons.Outlined.Work,
                selectedIcon = Icons.Filled.Work,
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
        0 -> NavRoutes.Portfolio.route
        1 -> NavRoutes.Dashboard.route
        2 -> NavRoutes.TransactionList.route
        3 -> NavRoutes.Budget.route
        else -> NavRoutes.Dashboard.route
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
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
                            tabItem.copy(onAction = { showAddTransaction = true })
                        } else {
                            tabItem
                        }
                    }
                )
            }
        ) { innerPadding ->
        // We use innerPadding.calculateTopPadding() but ignore bottom padding 
        // to allow content to scroll behind the floating transparent TabBar.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.Dashboard.route
            ) {
                composable(NavRoutes.Portfolio.route) { PlaceholderTab("Portfolio Screen") }
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
                composable(NavRoutes.TransactionList.route) { PlaceholderTab("Transactions Screen") }
                composable(NavRoutes.Budget.route) { PlaceholderTab("Budget Screen") }
                composable(NavRoutes.Profile.route) { PlaceholderTab("Profile Screen") }
            }
        }
    }
}

    // Modal interaction for the center Add button
    if (showAddTransaction) {
        // TODO: Replace with actual AddTransactionBottomSheet
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddTransaction = false },
            title = { Text("Add Transaction") },
            text = { Text("This modal was triggered by the center tab button.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showAddTransaction = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun PlaceholderTab(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}
