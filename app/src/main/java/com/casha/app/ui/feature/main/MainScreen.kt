package com.casha.app.ui.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.casha.app.ui.component.CustomTabBar
import com.casha.app.ui.component.TabItem

val MAIN_TABS = listOf(
    TabItem("Wealth", Icons.Default.ShoppingCart, 0), // Placeholder icon
    TabItem("Home", Icons.Default.Home, 1),
    TabItem("Add", Icons.Default.Add, 5, isCenterButton = true),
    TabItem("Transactions", Icons.Default.List, 2),
    TabItem("Budget", Icons.Default.Person, 3) // Placeholder icon
)

@Composable
fun MainScreen(
    parentNavController: NavHostController
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var selectedTab by remember { mutableStateOf(1) } // Default to Home
    var showAddTransaction by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            CustomTabBar(
                selectedTab = selectedTab,
                onTabSelected = { tag ->
                    selectedTab = tag
                    val route = when (tag) {
                        0 -> "wealth"
                        1 -> "home"
                        2 -> "transactions"
                        3 -> "budget"
                        else -> "home"
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                tabs = MAIN_TABS.map { tab ->
                    if (tab.isCenterButton) {
                        tab.copy(action = { showAddTransaction = true })
                    } else tab
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("wealth") { PlaceholderTab("Wealth Screen") }
                composable("home") { PlaceholderTab("Home Screen") }
                composable("transactions") { PlaceholderTab("Transactions Screen") }
                composable("budget") { PlaceholderTab("Budget Screen") }
            }
        }
    }

    // Modal interaction example for the center Add button
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
