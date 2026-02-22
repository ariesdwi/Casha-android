package com.casha.app.ui.feature.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.core.util.DateHelper
import com.casha.app.ui.feature.transaction.subview.TransactionList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (String) -> Unit,
    onNavigateToTransactionDetail: (String, String) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // For iOS matching background
    val gradientBackground = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    )

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                windowInsets = WindowInsets(0.dp),
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, // Transparent to show gradient
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.searchTransactions(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search transactions...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                // Filter Pill Row
                val monthOptions = remember { DateHelper.generateMonthYearOptions() }
                val thisMonthRaw = monthOptions.firstOrNull() ?: "This month"
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "This month" option
                    val isThisMonthSelected = uiState.selectedMonth == "This month" || uiState.selectedMonth == thisMonthRaw
                    FilterPill(
                        title = "This month",
                        isSelected = isThisMonthSelected,
                        onClick = { viewModel.filterTransactionsByMonth("This month") } // Or pass thisMonthRaw
                    )

                    // "Other month" dropdown
                    var expanded by remember { mutableStateOf(false) }
                    val isOtherMonthSelected = !isThisMonthSelected && uiState.selectedMonth != "This year"
                    val otherMonthTitle = if (isOtherMonthSelected) {
                        if (uiState.selectedMonth in monthOptions) DateHelper.formatMonthYearDisplay(uiState.selectedMonth)
                        else uiState.selectedMonth
                    } else "Other month"

                    Box {
                        FilterPill(
                            title = otherMonthTitle,
                            isSelected = isOtherMonthSelected,
                            showChevron = true,
                            onClick = { expanded = true }
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            val otherOptions = monthOptions.filter { it != thisMonthRaw }
                            otherOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = DateHelper.formatMonthYearDisplay(option),
                                            color = if (uiState.selectedMonth == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        ) 
                                    },
                                    onClick = {
                                        viewModel.filterTransactionsByMonth(option)
                                        expanded = false
                                    },
                                    trailingIcon = if (uiState.selectedMonth == option) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                    } else null
                                )
                            }
                        }
                    }

                    // "This year" option
                    FilterPill(
                        title = "This year",
                        isSelected = uiState.selectedMonth == "This year",
                        onClick = { viewModel.filterTransactionsByMonth("This year") }
                    )
                }

                // Search Context Indicator
                AnimatedVisibility(
                    visible = uiState.isSearching && uiState.searchQuery.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Results for \"${uiState.searchQuery}\"",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Content with Pull-to-Refresh
                PullToRefreshBox(
                    isRefreshing = uiState.isLoading,
                    onRefresh = { viewModel.syncData() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    val sectionsToDisplay = if (uiState.isSearching) uiState.filteredTransactions else uiState.cashflowSections
                    
                    if (sectionsToDisplay.isEmpty() && !uiState.isLoading) {
                        if (uiState.isSearching) {
                            EmptySearchStateView(searchQuery = uiState.searchQuery)
                        } else {
                            // Let the existing EmptyTransactionsState handle or add general empty state here
                            TransactionList(
                                sections = emptyList(),
                                isLoading = false,
                                onDelete = {},
                                onEdit = {},
                                onClick = { _, _ -> }
                            )
                        }
                    } else {
                        TransactionList(
                            sections = sectionsToDisplay,
                            isLoading = uiState.isLoading,
                            onDelete = { viewModel.deleteTransaction(it) },
                            onEdit = { onNavigateToEditTransaction(it) },
                            onClick = { id, type -> onNavigateToTransactionDetail(id, type) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterPill(
    title: String,
    isSelected: Boolean,
    showChevron: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
            )
            if (showChevron) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmptySearchStateView(searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No search results",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "No matches found for \"$searchQuery\". Try adjusting your filters.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
