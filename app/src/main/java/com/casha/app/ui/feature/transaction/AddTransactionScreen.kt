package com.casha.app.ui.feature.transaction

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
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
    val userCurrency = CurrencyFormatter.defaultCurrency
    val currencySymbol = CurrencyFormatter.symbol(userCurrency)

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

    val dateFormatter = remember { SimpleDateFormat("EEE, d MMM yyyy · HH:mm", Locale("id", "ID")) }

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
        if (amountValue <= 0) { errorMessage = "Masukkan jumlah yang valid"; return }
        if (name.isEmpty())   { errorMessage = "Masukkan nama transaksi"; return }
        if (entryType == EntryType.EXPENSE) {
            if (selectedCategory.isEmpty()) { errorMessage = "Pilih kategori"; return }
            val req = TransactionRequest(name = name, category = selectedCategory, amount = amountValue, datetime = selectedDate, note = note.takeIf { it.isNotEmpty() })
            if (isEditMode && transactionId != null) viewModel.updateTransaction(transactionId, req) else viewModel.addTransaction(req)
        } else {
            val req = CreateIncomeRequest(name = name.trim(), amount = amountValue, datetime = selectedDate, type = selectedIncomeType, source = source.takeIf { it.isNotEmpty() }, isRecurring = isRecurring, frequency = if (isRecurring) selectedFrequency else null, note = note.takeIf { it.isNotEmpty() })
            if (isEditMode && transactionId != null) viewModel.updateIncome(transactionId, req) else viewModel.addIncome(req)
        }
        onNavigateBack()
    }

    val screenTitle = if (isEditMode) "Edit Transaksi"
        else if (entryType == EntryType.EXPENSE) "Tambah Pengeluaran" else "Tambah Pemasukan"

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onNavigateBack,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color(0xFFF8F9FA)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxHeight(0.9f),
            containerColor = Color(0xFFF8F9FA),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA))
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = screenTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = { save() },
                        enabled = isFormValid
                    ) {
                        Text(
                            "Simpan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isFormValid) Color(0xFF009033) else Color.Gray
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Type Switcher ─────────────────────────────────
                if (!isEditMode) {
                    TypeSwitcher(entryType = entryType, onTypeChange = { entryType = it; errorMessage = null })
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // ── Amount ───────────────────────────────────────
                InputCard(title = "Jumlah *") {
                    OutlinedTextField(
                        value = if (isAmountFocused) amountText else if (amountText.isNotEmpty()) CurrencyFormatter.formatInput(amountText) else "",
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                amountText = it
                                amountValue = it.toDoubleOrNull() ?: 0.0
                                errorMessage = null
                            }
                        },
                        placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().onFocusChanged { isAmountFocused = it.isFocused },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = cashaBorderlessTextFieldColors(),
                        leadingIcon = {
                            Text(currencySymbol, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        },
                        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                }

                // ── Name ─────────────────────────────────────────
                InputCard(title = "Nama *") {
                    CashaFormTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        placeholder = if (entryType == EntryType.EXPENSE) "e.g., Coffee" else "e.g., Salary"
                    )
                }

                // ── Category / Income Type ───────────────────────
                if (entryType == EntryType.EXPENSE) {
                    InputCard(title = "Kategori *") {
                        Box {
                            Surface(
                                onClick = { showCategoryDropdown = true },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedCategory.ifEmpty { "Pilih Kategori" },
                                        fontSize = 15.sp,
                                        fontWeight = if (selectedCategory.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selectedCategory.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
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
                    }
                } else {
                    InputCard(title = "Tipe Pemasukan") {
                        Box {
                            Surface(
                                onClick = { showIncomeTypeDropdown = true },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedIncomeType.name.lowercase().replaceFirstChar { it.uppercase() },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = showIncomeTypeDropdown,
                                onDismissRequest = { showIncomeTypeDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
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
                }

                // ── Date & Time ──────────────────────────────────
                InputCard(title = "Tanggal & Waktu") {
                    Surface(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormatter.format(selectedDate),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // ── Income Specifics ─────────────────────────────
                AnimatedVisibility(visible = entryType == EntryType.INCOME) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        InputCard(title = "Sumber") {
                            CashaFormTextField(
                                value = source,
                                onValueChange = { source = it },
                                placeholder = "e.g., Perusahaan (Opsional)"
                            )
                        }

                        InputCard(title = "Pemasukan Berulang") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Recurring",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = isRecurring,
                                    onCheckedChange = { isRecurring = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF009033)
                                    )
                                )
                            }
                        }

                        AnimatedVisibility(visible = isRecurring) {
                            InputCard(title = "Frekuensi") {
                                Box {
                                    Surface(
                                        onClick = { showFrequencyDropdown = true },
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedFrequency.name.lowercase().replaceFirstChar { it.uppercase() },
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = showFrequencyDropdown,
                                        onDismissRequest = { showFrequencyDropdown = false },
                                        modifier = Modifier.fillMaxWidth(0.9f)
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

                // ── Note ─────────────────────────────────────────
                InputCard(title = "Catatan") {
                    CashaFormTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = "Tambah catatan (Opsional)",
                        singleLine = false
                    )
                }

                // ── Error ────────────────────────────────────────
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    errorMessage?.let {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                Text(it, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
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
                    }) { Text("OK", color = Color(0xFF009033), fontWeight = FontWeight.Bold) }
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
                title = { Text("Pilih Waktu", fontWeight = FontWeight.Bold) },
                text = { TimePicker(state = timeState) },
                confirmButton = {
                    TextButton(onClick = {
                        val updated = Calendar.getInstance().apply {
                            time = selectedDate
                            set(Calendar.HOUR_OF_DAY, timeState.hour)
                            set(Calendar.MINUTE, timeState.minute)
                        }
                        selectedDate = updated.time; showTimePicker = false
                    }) { Text("OK", color = Color(0xFF009033), fontWeight = FontWeight.Bold) }
                },
                dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel", color = Color.Gray) } }
            )
        }
    }
}

// ─── Input Card (reused from liability module) ────────────────────
@Composable
private fun InputCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            letterSpacing = 0.3.sp
        )
        content()
    }
}

// ─── Borderless TextField Colors ──────────────────────────────────
@Composable
private fun cashaBorderlessTextFieldColors(): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent
    )
}

// ─── Casha Form TextField ─────────────────────────────────────────
@Composable
private fun CashaFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), fontWeight = FontWeight.Normal)
        },
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface),
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        colors = cashaBorderlessTextFieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

// ─── Type Switcher ────────────────────────────────────────────────
@Composable
private fun TypeSwitcher(entryType: EntryType, onTypeChange: (EntryType) -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(3.dp)
        ) {
            EntryType.values().forEach { type ->
                val selected = entryType == type
                val bgColor = when {
                    selected && type == EntryType.EXPENSE -> Color(0xFFFF6B6B).copy(alpha = 0.15f)
                    selected && type == EntryType.INCOME -> Color(0xFF009033).copy(alpha = 0.15f)
                    else -> Color.Transparent
                }
                val textColor = when {
                    selected && type == EntryType.EXPENSE -> Color(0xFFFF6B6B)
                    selected && type == EntryType.INCOME -> Color(0xFF009033)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(11.dp))
                        .background(bgColor)
                        .clickable { onTypeChange(type) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (type == EntryType.EXPENSE) "Pengeluaran" else "Pemasukan",
                        color = textColor,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}