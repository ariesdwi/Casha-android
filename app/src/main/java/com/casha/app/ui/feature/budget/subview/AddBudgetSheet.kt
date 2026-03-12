package com.casha.app.ui.feature.budget.subview
import androidx.compose.foundation.layout.fillMaxSize

import com.casha.app.ui.feature.budget.BudgetViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.core.util.DateHelper
import com.casha.app.domain.model.NewBudgetRequest
import com.casha.app.core.util.CurrencyFormatter
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import com.casha.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetSheet(
    budgetId: String? = null,
    onDismiss: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = budgetId != null
    
    // Form State
    var selectedCategoryName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isAmountFocused by remember { mutableStateOf(false) }
    var selectedMonthYear by remember { mutableStateOf(uiState.currentMonthYear ?: DateHelper.generateMonthYearOptions().firstOrNull() ?: "") }
    
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }

    // Initialize Edit Mode
    LaunchedEffect(budgetId, uiState.budgets) {
        if (isEditMode && uiState.budgets.isNotEmpty()) {
            val budget = uiState.budgets.find { it.id == budgetId }
            budget?.let {
                selectedCategoryName = it.category
                // If it ends with .0, remove it for cleaner display
                amountText = if (it.amount % 1.0 == 0.0) it.amount.toLong().toString() else it.amount.toString()
                selectedMonthYear = it.period
            }
        }
    }

    val isValid = selectedCategoryName.isNotEmpty() && (amountText.toDoubleOrNull() ?: 0.0) > 0

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
modifier = Modifier.fillMaxSize(),
onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            modifier = Modifier.fillMaxHeight(0.9f),
            containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(if (isEditMode) R.string.budget_edit_title else R.string.budget_add_title),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.budget_action_cancel), color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (isValid) {
                                val request = NewBudgetRequest(
                                    id = budgetId,
                                    amount = amount,
                                    month = selectedMonthYear,
                                    category = selectedCategoryName
                                )
                                if (isEditMode) {
                                    viewModel.updateBudget(request)
                                } else {
                                    viewModel.addBudget(request)
                                }
                                onDismiss()
                            }
                        },
                        enabled = isValid
                    ) {
                        Text(
                            text = stringResource(R.string.budget_action_save),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = if (isValid) MaterialTheme.colorScheme.onBackground else Color.Gray
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Group 1: Budget Details
            Column {
                Text(
                    text = stringResource(R.string.budget_details_header),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        // Amount Input Row
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            BasicTextField(
                                value = if (isAmountFocused) amountText else if (amountText.isNotEmpty()) CurrencyFormatter.formatInput(amountText) else "",
                                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) amountText = it },
                                textStyle = TextStyle(
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth().onFocusChanged { isAmountFocused = it.isFocused },
                                decorationBox = { innerTextField ->
                                    if (amountText.isEmpty()) {
                                        Text(
                                            text = "0",
                                            style = TextStyle(
                                                fontSize = 17.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            thickness = 0.5.dp
                        )
                        
                        // Category Selector Row
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCategoryDropdown = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.budget_details_category),
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = selectedCategoryName.ifEmpty { stringResource(R.string.budget_details_select_category) },
                                        fontSize = 17.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.UnfoldMore,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            
                            DropdownMenu(
                                expanded = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                uiState.categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            selectedCategoryName = category.name
                                            showCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Group 2: Month & Year
            Column {
                Text(
                    text = stringResource(R.string.budget_month_header),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMonthDropdown = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.budget_month_label),
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = DateHelper.formatMonthYearDisplay(selectedMonthYear),
                                    fontSize = 17.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.UnfoldMore,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        val monthOptions = DateHelper.generateMonthYearOptions()
                        DropdownMenu(
                            expanded = showMonthDropdown,
                            onDismissRequest = { showMonthDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            monthOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(DateHelper.formatMonthYearDisplay(option)) },
                                    onClick = {
                                        selectedMonthYear = option
                                        showMonthDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    }
}
