package com.casha.app.ui.feature.transaction

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.CreateIncomeRequest
import com.casha.app.domain.model.IncomeFrequency
import com.casha.app.domain.model.IncomeType
import com.casha.app.domain.model.TransactionRequest
import java.text.SimpleDateFormat
import java.util.*

enum class EntryType { EXPENSE, INCOME }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = transactionId != null

    var entryType             by remember { mutableStateOf(EntryType.EXPENSE) }
    var name                  by remember { mutableStateOf("") }
    var amountText            by remember { mutableStateOf("") }
    var amountValue           by remember { mutableStateOf(0.0) }
    var isAmountFocused       by remember { mutableStateOf(false) }
    var selectedDate          by remember { mutableStateOf(Date()) }
    var errorMessage          by remember { mutableStateOf<String?>(null) }
    var selectedCategory      by remember { mutableStateOf("") }
    var showCategoryDropdown  by remember { mutableStateOf(false) }
    var selectedIncomeType    by remember { mutableStateOf(IncomeType.SALARY) }
    var showIncomeTypeDropdown by remember { mutableStateOf(false) }
    var source                by remember { mutableStateOf("") }
    var isRecurring           by remember { mutableStateOf(false) }
    var selectedFrequency     by remember { mutableStateOf(IncomeFrequency.MONTHLY) }
    var showFrequencyDropdown by remember { mutableStateOf(false) }
    var note                  by remember { mutableStateOf("") }
    var showDatePicker        by remember { mutableStateOf(false) }
    var showTimePicker        by remember { mutableStateOf(false) }

    val isFormValid = remember(amountValue, name, entryType, selectedCategory) {
        val base = amountValue > 0 && name.isNotEmpty()
        if (entryType == EntryType.EXPENSE) base && selectedCategory.isNotEmpty() else base
    }

    LaunchedEffect(transactionId, uiState.rawTransactions) {
        if (isEditMode && uiState.rawTransactions.isNotEmpty()) {
            val tx = uiState.rawTransactions.find { it.id == transactionId }
            if (tx != null) {
                name = tx.name
                amountValue = tx.amount
                amountText = if (tx.amount % 1.0 == 0.0) tx.amount.toLong().toString() else tx.amount.toString()
                selectedCategory = tx.category
                note = tx.note ?: ""
                selectedDate = tx.datetime
                entryType = EntryType.EXPENSE
            } else {
                val inc = uiState.rawIncomes.find { it.id == transactionId }
                if (inc != null) {
                    name = inc.name
                    amountValue = inc.amount
                    amountText = if (inc.amount % 1.0 == 0.0) inc.amount.toLong().toString() else inc.amount.toString()
                    selectedIncomeType = inc.type
                    source = inc.source ?: ""
                    isRecurring = inc.isRecurring
                    selectedFrequency = inc.frequency ?: IncomeFrequency.MONTHLY
                    note = inc.note ?: ""
                    selectedDate = inc.datetime
                    entryType = EntryType.INCOME
                }
            }
        }
    }

    fun save() {
        if (amountValue <= 0) { errorMessage = "Please enter a valid amount"; return }
        if (name.isEmpty())   { errorMessage = "Please enter a name"; return }
        if (entryType == EntryType.EXPENSE) {
            if (selectedCategory.isEmpty()) { errorMessage = "Please select a category"; return }
            val req = TransactionRequest(name = name, category = selectedCategory, amount = amountValue, datetime = selectedDate, note = note.takeIf { it.isNotEmpty() })
            if (isEditMode && transactionId != null) viewModel.updateTransaction(transactionId, req) else viewModel.addTransaction(req)
        } else {
            val req = CreateIncomeRequest(name = name.trim(), amount = amountValue, datetime = selectedDate, type = selectedIncomeType, source = source.takeIf { it.isNotEmpty() }, isRecurring = isRecurring, frequency = if (isRecurring) selectedFrequency else null, note = note.takeIf { it.isNotEmpty() })
            if (isEditMode && transactionId != null) viewModel.updateIncome(transactionId, req) else viewModel.addIncome(req)
        }
        onNavigateBack()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onNavigateBack,
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
                        text = if (isEditMode) "Edit Transaction"
                               else if (entryType == EntryType.EXPENSE) "Add Expense" else "Add Income",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel", color = Color.Black, fontSize = 17.sp)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { save() },
                        enabled = isFormValid
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = if (isFormValid) Color.Black else Color.Gray
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
            
            // ── Type Switcher ─────────────────────────────────
            if (!isEditMode) {
                TypeSwitcher(entryType = entryType, onTypeChange = { entryType = it; errorMessage = null })
            }

            // ── Group 1: Basic Details ──────────────────────────────
            Column {
                Text(
                    text = "Transaction Details",
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Amount",
                                    fontSize = 17.sp,
                                    color = Color.Black,
                                    modifier = Modifier.width(100.dp)
                                )
                                BasicTextField(
                                    value = if (isAmountFocused) amountText else if (amountText.isNotEmpty()) CurrencyFormatter.formatInput(amountText) else "",
                                    onValueChange = { 
                                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                            amountText = it
                                            amountValue = it.toDoubleOrNull() ?: 0.0
                                            errorMessage = null
                                        }
                                    },
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
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp),
                            color = Color(0xFFE5E5EA),
                            thickness = 0.5.dp
                        )
                        
                        // Name Input Row
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Title",
                                    fontSize = 17.sp,
                                    color = Color.Black,
                                    modifier = Modifier.width(100.dp)
                                )
                                BasicTextField(
                                    value = name,
                                    onValueChange = { name = it; errorMessage = null },
                                    textStyle = TextStyle(
                                        fontSize = 17.sp,
                                        color = Color.Black
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (name.isEmpty()) {
                                            Text(
                                                text = if (entryType == EntryType.EXPENSE) "e.g. Coffee" else "e.g. Salary",
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
                        }
                    }
                }
            }

            // ── Group 2: Categorization ──────────────────────────────
            Column {
                Text(
                    text = "Categorization",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Column {
                        if (entryType == EntryType.EXPENSE) {
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
                                            text = selectedCategory.ifEmpty { "Select Category" },
                                            fontSize = 17.sp,
                                            color = if (selectedCategory.isNotEmpty()) Color.Black else Color.Gray
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
                                                selectedCategory = category.name
                                                showCategoryDropdown = false
                                                errorMessage = null
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Income Type Selector Row
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showIncomeTypeDropdown = true }
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Income Type",
                                        fontSize = 17.sp,
                                        color = Color.Black
                                    )
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = selectedIncomeType.name.lowercase().replaceFirstChar { it.uppercase() },
                                            fontSize = 17.sp,
                                            color = Color.Black
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
                                    expanded = showIncomeTypeDropdown,
                                    onDismissRequest = { showIncomeTypeDropdown = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .background(Color.White)
                                ) {
                                    IncomeType.values().forEach { type ->
                                        val display = type.name.lowercase().replaceFirstChar { it.uppercase() }
                                        DropdownMenuItem(
                                            text = { Text(display) },
                                            onClick = {
                                                selectedIncomeType = type
                                                showIncomeTypeDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp),
                            color = Color(0xFFE5E5EA),
                            thickness = 0.5.dp
                        )

                        // Date Selector
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Date & Time",
                                    fontSize = 17.sp,
                                    color = Color.Black
                                )
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = SimpleDateFormat("EEE, d MMM yyyy · HH:mm", Locale.getDefault()).format(selectedDate),
                                        fontSize = 17.sp,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Group 3: Income Specifics ──────────────────────────────
            AnimatedVisibility(visible = entryType == EntryType.INCOME) {
                Column {
                    Text(
                        text = "Recurrence",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Column {
                            // Source
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Source",
                                        fontSize = 17.sp,
                                        color = Color.Black,
                                        modifier = Modifier.width(100.dp)
                                    )
                                    BasicTextField(
                                        value = source,
                                        onValueChange = { source = it },
                                        textStyle = TextStyle(
                                            fontSize = 17.sp,
                                            color = Color.Black
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { innerTextField ->
                                            if (source.isEmpty()) {
                                                Text(
                                                    text = "e.g. Company (Optional)",
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
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp),
                                color = Color(0xFFE5E5EA),
                                thickness = 0.5.dp
                            )

                            // Recurring Toggle
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp) // Less vertical padding for toggle
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Recurring Income",
                                        fontSize = 17.sp,
                                        color = Color.Black
                                    )
                                    Switch(
                                        checked = isRecurring,
                                        onCheckedChange = { isRecurring = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF34C759) // Standard iOS Green
                                        )
                                    )
                                }
                            }

                            if (isRecurring) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 16.dp),
                                    color = Color(0xFFE5E5EA),
                                    thickness = 0.5.dp
                                )
                                
                                // Frequency Dropdown
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showFrequencyDropdown = true }
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Frequency",
                                            fontSize = 17.sp,
                                            color = Color.Black
                                        )
                                        
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = selectedFrequency.name.lowercase().replaceFirstChar { it.uppercase() },
                                                fontSize = 17.sp,
                                                color = Color.Black
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
                                        expanded = showFrequencyDropdown,
                                        onDismissRequest = { showFrequencyDropdown = false },
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .background(Color.White)
                                    ) {
                                        IncomeFrequency.values().forEach { freq ->
                                            val display = freq.name.lowercase().replaceFirstChar { it.uppercase() }
                                            DropdownMenuItem(
                                                text = { Text(display) },
                                                onClick = {
                                                    selectedFrequency = freq
                                                    showFrequencyDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Group 4: Note ──────────────────────────────
            Column {
                Text(
                    text = "Note",
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
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        BasicTextField(
                            value = note,
                            onValueChange = { note = it },
                            textStyle = TextStyle(
                                fontSize = 17.sp,
                                color = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 80.dp),
                            decorationBox = { innerTextField ->
                                if (note.isEmpty()) {
                                    Text(
                                        text = "Add a note (Optional)",
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
                }
            }
            
            // ── Error ─────────────────────────────────────────
            AnimatedVisibility(visible = errorMessage != null,
                enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()
            ) {
                errorMessage?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFF6B6B).copy(alpha = 0.08f))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null,
                            tint = Color(0xFFFF6B6B), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(it, color = Color(0xFFFF6B6B), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(140.dp))
        }
    }

    // ── Date Picker ───────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.time)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = it
                            val cur = Calendar.getInstance().apply { time = selectedDate }
                            set(Calendar.HOUR_OF_DAY, cur.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, cur.get(Calendar.MINUTE))
                        }
                        selectedDate = cal.time
                    }
                    showDatePicker = false; showTimePicker = true
                }) { Text("OK", color = Color(0xFF6C63FF), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color.Gray) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // ── Time Picker ───────────────────────────────────────────────
    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { time = selectedDate }
        val timeState = rememberTimePickerState(initialHour = cal.get(Calendar.HOUR_OF_DAY), initialMinute = cal.get(Calendar.MINUTE))
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Select Time", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    val updated = Calendar.getInstance().apply {
                        time = selectedDate
                        set(Calendar.HOUR_OF_DAY, timeState.hour)
                        set(Calendar.MINUTE, timeState.minute)
                    }
                    selectedDate = updated.time; showTimePicker = false
                }) { Text("OK", color = Color(0xFF6C63FF), fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel", color = Color.Gray) } }
        )
    }
    }
}

// ─── Type Switcher ────────────────────────────────────────────────
@Composable
private fun TypeSwitcher(entryType: EntryType, onTypeChange: (EntryType) -> Unit) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = Color(0xFFE5E5EA), // iOS Segmented Control background
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(34.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(2.dp)
        ) {
            EntryType.values().forEach { type ->
                val selected = entryType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (selected) Color.White else Color.Transparent)
                        .clickable { onTypeChange(type) }
                        .then(
                            if (selected) Modifier.shadow(1.dp, RoundedCornerShape(7.dp))
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (type == EntryType.EXPENSE) "Expense" else "Income",
                        color = Color.Black,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}