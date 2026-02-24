package com.casha.app.ui.feature.budget

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.core.util.DateHelper
import com.casha.app.domain.model.BudgetCasha
import com.casha.app.ui.feature.budget.subview.BudgetList
import com.casha.app.ui.feature.budget.subview.AddBudgetSheet
import com.casha.app.ui.feature.budget.subview.BudgetAIAdvisorSheet
import com.casha.app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var editBudgetId by remember { mutableStateOf<String?>(null) }
    var showAddBudgetSheet by remember { mutableStateOf(false) }
    var showAIAdvisorSheet by remember { mutableStateOf(false) }
    
    // Subscriptions simulator (for now, as per iOS implementation)
    val hasPremiumAccess by remember { mutableStateOf(true) } 
    var showPaywall by remember { mutableStateOf(false) }

    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Budgets",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Surface(
                        modifier = Modifier.padding(end = 16.dp),
                        shape = RoundedCornerShape(32.dp),
                        color = Color.White,
                        tonalElevation = 4.dp,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // AI Recommendations Button
                            IconButton(onClick = {
                                if (hasPremiumAccess) showAIAdvisorSheet = true else showPaywall = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Recommendations",
                                    tint = Color(0xFF9C27B0),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Add Budget Button
                            IconButton(onClick = {
                                if (hasPremiumAccess) showAddBudgetSheet = true else showPaywall = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Budget",
                                    tint = CashaSuccess,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Month Filter Menu
                            val monthOptions = DateHelper.generateMonthYearOptions()
                            var showMonthMenu by remember { mutableStateOf(false) }
                            
                            Box {
                                IconButton(onClick = { showMonthMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter Month",
                                        tint = CashaSuccess
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMonthMenu,
                                    onDismissRequest = { showMonthMenu = false }
                                ) {
                                    monthOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(option)
                                                    if (option == uiState.currentMonthYear) {
                                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            },
                                            onClick = {
                                                viewModel.setMonth(option)
                                                showMonthMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Inline Error Banner (Toast simulator)
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    LaunchedEffect(uiState.errorMessage) {
                        if (uiState.errorMessage != null) {
                            delay(3000)
                            viewModel.clearError()
                        }
                    }
                }
            }

            // Main Content with Pull-to-Refresh
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refreshBudgetData() },
                state = pullToRefreshState,
                modifier = Modifier.weight(1f)
            ) {
                BudgetList(
                    budgets = uiState.budgets,
                    summary = uiState.budgetSummary,
                    isLoading = uiState.isLoading,
                    onDelete = { viewModel.deleteBudget(it) },
                    onEdit = { id -> 
                        editBudgetId = id
                    },
                    onNavigateToAIRecommendations = { showAIAdvisorSheet = true }
                )
            }
        }

        // Add or Edit Budget Sheet
        if (showAddBudgetSheet || editBudgetId != null) {
            AddBudgetSheet(
                budgetId = editBudgetId,
                onDismiss = {
                    showAddBudgetSheet = false
                    editBudgetId = null
                }
            )
        }

        // AI Advisor Sheet
        if (showAIAdvisorSheet) {
            BudgetAIAdvisorSheet(onDismiss = { showAIAdvisorSheet = false })
        }

        // Error Snackbar
        uiState.errorMessage?.let { error ->
            LaunchedEffect(error) {
                // TODO: Show snackbar or toast
            }
        }
    }
}
