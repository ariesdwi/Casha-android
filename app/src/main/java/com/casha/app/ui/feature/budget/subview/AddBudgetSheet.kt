package com.casha.app.ui.feature.budget.subview

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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color(0xFFF2F2F7)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxHeight(0.9f),
            containerColor = Color(0xFFF2F2F7), // iOS grouped background color
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Budget" else "Add Budget",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Black, fontSize = 17.sp)
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
                            text = "Save",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = if (isValid) Color.Black else Color.Gray
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF2F2F7),
                    scrolledContainerColor = Color(0xFFF2F2F7)
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
                    text = "Budget Details",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
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
                                    color = Color.Black
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth().onFocusChanged { isAmountFocused = it.isFocused },
                                decorationBox = { innerTextField ->
                                    if (amountText.isEmpty()) {
                                        Text(
                                            text = "0",
                                            style = TextStyle(
                                                fontSize = 17.sp,
                                                color = Color.LightGray
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp),
                            color = Color(0xFFE5E5EA),
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
                                    text = "Category",
                                    fontSize = 17.sp,
                                    color = Color.Black
                                )
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = selectedCategoryName.ifEmpty { "Select Category" },
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
                                    .background(Color.White)
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
                    text = "Month & Year",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
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
                                text = "Month",
                                fontSize = 17.sp,
                                color = Color.Black
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
                                .background(Color.White)
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
