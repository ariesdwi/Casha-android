package com.casha.app.ui.feature.report
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.domain.model.ReportFilterPeriod
import com.casha.app.ui.feature.report.subview.ReportCategoryList
import com.casha.app.ui.feature.report.subview.ReportCategoryPieChart
import androidx.compose.ui.res.stringResource
import com.casha.app.R
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onNavigateToCategoryDetail: (String) -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // DateRangePicker ModalBottomSheet
    if (showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = uiState.customStartDate?.time,
            initialSelectedEndDateMillis = uiState.customEndDate?.time
        )
        ModalBottomSheet(
modifier = Modifier.fillMaxSize(),
onDismissRequest = { showDateRangePicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showDateRangePicker = false }) {
                        Text(stringResource(R.string.add_transaction_cancel))
                    }
                    Text(
                        text = stringResource(R.string.transactions_filter_select_date_range),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(
                        onClick = {
                            val start = dateRangePickerState.selectedStartDateMillis
                            val end = dateRangePickerState.selectedEndDateMillis
                            if (start != null && end != null) {
                                viewModel.setCustomDateRange(start, end)
                            }
                            showDateRangePicker = false
                        },
                        enabled = dateRangePickerState.selectedStartDateMillis != null &&
                                  dateRangePickerState.selectedEndDateMillis != null
                    ) {
                        Text(stringResource(R.string.profile_action_done))
                    }
                }
                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp),
                    title = null,
                    headline = null,
                    showModeToggle = false
                )
            }
        }
    }

    if (uiState.showPaywall) {
        ModalBottomSheet(
modifier = Modifier.fillMaxSize(),
onDismissRequest = { viewModel.dismissPaywall() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = null,
            containerColor = Color.Transparent,
            contentWindowInsets = { WindowInsets(0.dp) }
        ) {
            com.casha.app.ui.feature.subscription.PaywallScreen(
                onDismiss = { viewModel.dismissPaywall() }
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.report_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 4.dp)) {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = stringResource(R.string.report_action_filter),
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            // Standard period filters (WEEK, MONTH, YEAR)
                            ReportFilterPeriod.entries
                                .filter { it != ReportFilterPeriod.CUSTOM }
                                .forEach { period ->
                                DropdownMenuItem(
                                    text = { 
                                        val stringResId = when (period) {
                                            ReportFilterPeriod.WEEK -> R.string.report_filter_week
                                            ReportFilterPeriod.MONTH -> R.string.report_filter_month
                                            ReportFilterPeriod.YEAR -> R.string.report_filter_year
                                            ReportFilterPeriod.CUSTOM -> R.string.transactions_filter_custom_range
                                        }
                                        Text(stringResource(stringResId))
                                    },
                                    onClick = {
                                        viewModel.setFilter(period)
                                        showFilterMenu = false
                                    },
                                    trailingIcon = if (uiState.selectedPeriod == period) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                            // Custom Range option
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.transactions_filter_custom_range)) },
                                onClick = {
                                    showFilterMenu = false
                                    showDateRangePicker = true
                                },
                                trailingIcon = if (uiState.selectedPeriod == ReportFilterPeriod.CUSTOM) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.categorySpendings.isEmpty() && !uiState.isLoading) {
                EmptyStateView(
                    message = stringResource(R.string.report_empty_message),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                ScrollViewContent(
                    uiState = uiState,
                    onNavigateToCategoryDetail = { category ->
                        viewModel.onCategoryClicked(category, onNavigateToCategoryDetail)
                    }
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun ScrollViewContent(
    uiState: ReportUiState,
    onNavigateToCategoryDetail: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sub-Header
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 8.dp)) {
            Text(
                text = stringResource(R.string.report_section_spending_by_category),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            val subtitleText = when (uiState.selectedPeriod) {
                ReportFilterPeriod.WEEK -> stringResource(R.string.report_filter_week)
                ReportFilterPeriod.MONTH -> stringResource(R.string.report_filter_month)
                ReportFilterPeriod.YEAR -> stringResource(R.string.report_filter_year)
                ReportFilterPeriod.CUSTOM -> {
                    val fmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                    val start = uiState.customStartDate
                    val end = uiState.customEndDate
                    if (start != null && end != null) "${fmt.format(start)} – ${fmt.format(end)}"
                    else stringResource(R.string.transactions_filter_custom_range)
                }
            }
            
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Chart Section
        ReportCategoryPieChart(
            data = uiState.categorySpendings,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // List Section 
        ReportCategoryList(
            data = uiState.categorySpendings,
            hasPremiumAccess = uiState.isPremium,
            onCategoryClick = onNavigateToCategoryDetail
        )
        
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
private fun EmptyStateView(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PieChart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.report_empty_message),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
