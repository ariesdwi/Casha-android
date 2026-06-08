package com.casha.app.ui.feature.transaction

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import com.casha.app.ui.component.CurrencyInputField
import com.casha.app.ui.theme.*
import com.casha.app.domain.model.CreateIncomeRequest
import com.casha.app.domain.model.IncomeFrequency
import com.casha.app.domain.model.IncomeType
import com.casha.app.domain.model.TransactionRequest
import androidx.compose.ui.res.stringResource
import com.casha.app.R
import java.text.SimpleDateFormat
import java.util.*

enum class EntryType { EXPENSE, INCOME }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel(),
    walletViewModel: com.casha.app.ui.feature.wallet.WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val walletUiState by walletViewModel.uiState.collectAsState()
    val isEditMode = transactionId != null
    val userCurrency = CurrencyFormatter.defaultCurrency
    val currencySymbol = CurrencyFormatter.symbol(userCurrency)

    var entryType             by remember { mutableStateOf(EntryType.EXPENSE) }
    var name                  by remember { mutableStateOf("") }
    var amountText            by remember { mutableStateOf("") }
    var amountValue           by remember { mutableStateOf(0.0) }
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
    var selectedWalletId      by remember { mutableStateOf<String?>(null) }
    var showWalletDropdown    by remember { mutableStateOf(false) }

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
                amountText = if (tx.amount % 1.0 == 0.0) tx.amount.toLong().toString() else String.format("%.2f", tx.amount)
                selectedCategory = tx.category
                note = tx.note ?: ""
                selectedDate = tx.datetime
                entryType = EntryType.EXPENSE
            } else {
                val inc = uiState.rawIncomes.find { it.id == transactionId }
                if (inc != null) {
                    name = inc.name
                    amountValue = inc.amount
                    amountText = if (inc.amount % 1.0 == 0.0) inc.amount.toLong().toString() else String.format("%.2f", inc.amount)
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
            val req = TransactionRequest(name = name, category = selectedCategory, amount = amountValue, datetime = selectedDate, note = note.takeIf { it.isNotEmpty() }, assetId = selectedWalletId)
            if (isEditMode && transactionId != null) viewModel.updateTransaction(transactionId, req) else viewModel.addTransaction(req)
        } else {
            val req = CreateIncomeRequest(name = name.trim(), amount = amountValue, datetime = selectedDate, type = selectedIncomeType, source = source.takeIf { it.isNotEmpty() }, isRecurring = isRecurring, frequency = if (isRecurring) selectedFrequency else null, note = note.takeIf { it.isNotEmpty() })
            if (isEditMode && transactionId != null) viewModel.updateIncome(transactionId, req) else viewModel.addIncome(req)
        }
        onNavigateBack()
    }

    val screenTitle = if (isEditMode) "Edit Transaksi"
        else if (entryType == EntryType.EXPENSE) stringResource(R.string.add_transaction_title_expense) else stringResource(R.string.add_transaction_title_income)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
onDismissRequest = onNavigateBack,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
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
                        enabled = isFormValid && !uiState.isLoading
                    ) {
                        Text(
                            stringResource(R.string.add_transaction_save),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isFormValid) MaterialTheme.colorScheme.primary else Color.Gray
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
                    CurrencyInputField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            amountValue = it.toDoubleOrNull() ?: 0.0
                            errorMessage = null
                        },
                        placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                        modifier = Modifier.fillMaxWidth(),
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
                        placeholder = if (entryType == EntryType.EXPENSE) stringResource(R.string.add_transaction_name_placeholder_expense) else stringResource(R.string.add_transaction_name_placeholder_income)
                    )
                }

                // ── Category / Income Type ───────────────────────
                if (entryType == EntryType.EXPENSE) {
                    InputCard(title = stringResource(R.string.add_transaction_category) + " *") {
                        Surface(
                            onClick = { showCategoryDropdown = true },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (selectedCategory.isNotEmpty()) {
                                        val categoryIcon = getCategoryIcon(selectedCategory)
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                                    RoundedCornerShape(10.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(categoryIcon, fontSize = 18.sp)
                                        }
                                    }
                                    Text(
                                        text = selectedCategory.ifEmpty { stringResource(R.string.add_transaction_select_category) },
                                        fontSize = 15.sp,
                                        fontWeight = if (selectedCategory.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selectedCategory.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    InputCard(title = stringResource(R.string.add_transaction_income_category)) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                onClick = { showIncomeTypeDropdown = true },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedIncomeType.name.lowercase().replaceFirstChar { it.uppercase() },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showIncomeTypeDropdown,
                                onDismissRequest = { showIncomeTypeDropdown = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IncomeType.values().forEach { type ->
                                    val display = type.name.lowercase().replaceFirstChar { it.uppercase() }
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = display,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        },
                                        onClick = {
                                            selectedIncomeType = type
                                            showIncomeTypeDropdown = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Date & Time ──────────────────────────────────
                InputCard(title = stringResource(R.string.add_transaction_datetime)) {
                    Surface(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormatter.format(selectedDate),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
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
                                placeholder = stringResource(R.string.add_transaction_source_placeholder)
                            )
                        }

                        InputCard(title = stringResource(R.string.add_transaction_recurring_income)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.add_transaction_recurring),
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = isRecurring,
                                    onCheckedChange = { isRecurring = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        AnimatedVisibility(visible = isRecurring) {
                            InputCard(title = stringResource(R.string.add_transaction_frequency)) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Surface(
                                        onClick = { showFrequencyDropdown = true },
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedFrequency.name.lowercase().replaceFirstChar { it.uppercase() },
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = showFrequencyDropdown,
                                        onDismissRequest = { showFrequencyDropdown = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        IncomeFrequency.values().forEach { freq ->
                                            val display = freq.name.lowercase().replaceFirstChar { it.uppercase() }
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = display,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                },
                                                onClick = {
                                                    selectedFrequency = freq
                                                    showFrequencyDropdown = false
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Wallet Picker ─────────────────────────────────────
                InputCard(title = "Wallet") {
                    Surface(
                        onClick = { showWalletDropdown = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selectedWalletId != null) {
                                    val wallet = walletUiState.wallets.find { it.id == selectedWalletId }
                                    wallet?.let {
                                        val walletIcon = getWalletIcon(it.type)
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    getWalletColor(it.type).copy(alpha = 0.2f),
                                                    RoundedCornerShape(10.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(walletIcon, fontSize = 18.sp)
                                        }
                                    }
                                }
                                Text(
                                    text = walletUiState.wallets.find { it.id == selectedWalletId }?.name ?: "Select wallet (optional)",
                                    fontSize = 15.sp,
                                    fontWeight = if (selectedWalletId != null) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedWalletId != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // ── Note ─────────────────────────────────────────
                InputCard(title = stringResource(R.string.add_transaction_note)) {
                    CashaFormTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = stringResource(R.string.add_transaction_note_placeholder),
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
                    }) { Text(stringResource(R.string.add_transaction_ok), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.add_transaction_cancel), color = Color.Gray) }
                }
            ) { DatePicker(state = datePickerState) }
        }

        // ── Time Picker ───────────────────────────────────────────────
        if (showTimePicker) {
            val cal = Calendar.getInstance().apply { time = selectedDate }
            val timeState = rememberTimePickerState(initialHour = cal.get(Calendar.HOUR_OF_DAY), initialMinute = cal.get(Calendar.MINUTE))
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                containerColor = MaterialTheme.colorScheme.surface,
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
                    }) { Text(stringResource(R.string.add_transaction_ok), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                },
                dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.add_transaction_cancel), color = Color.Gray) } }
            )
        }
        
        // ── Category Selector Bottom Sheet ────────────────────────────
        if (showCategoryDropdown && entryType == EntryType.EXPENSE) {
            CategorySelectorBottomSheet(
                categories = uiState.categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = category
                    errorMessage = null
                },
                onDismiss = { showCategoryDropdown = false }
            )
        }
        
        // ── Wallet Selector Bottom Sheet ──────────────────────────────
        if (showWalletDropdown) {
            WalletSelectorBottomSheet(
                wallets = walletUiState.wallets,
                selectedWalletId = selectedWalletId,
                onWalletSelected = { walletId ->
                    selectedWalletId = walletId
                },
                onDismiss = { showWalletDropdown = false }
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
            .border(BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)), RoundedCornerShape(16.dp))
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
                    selected && type == EntryType.EXPENSE -> CashaDanger.copy(alpha = 0.15f)
                    selected && type == EntryType.INCOME  -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else -> Color.Transparent
                }
                val textColor = when {
                    selected && type == EntryType.EXPENSE -> CashaDanger
                    selected && type == EntryType.INCOME  -> MaterialTheme.colorScheme.primary
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
                        text = if (type == EntryType.EXPENSE) stringResource(R.string.add_transaction_type_expense) else stringResource(R.string.add_transaction_type_income),
                        color = textColor,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}


// ─── Category Icon Mapping ───────────────────────────────────────
private fun getCategoryIcon(categoryName: String): String {
    return when (categoryName.lowercase()) {
        "food & dining", "food", "dining", "restaurant" -> "🍴"
        "transportation", "transport" -> "🚗"
        "shopping" -> "🛍️"
        "bills & utilities", "bills", "utilities" -> "📄"
        "entertainment" -> "🎭"
        "housing", "rent" -> "🏠"
        "healthcare", "health" -> "❤️"
        "education" -> "📚"
        "financial", "investment" -> "📊"
        "personal care", "beauty" -> "💇"
        "gifts & donations", "gifts" -> "🎁"
        "travel" -> "✈️"
        "insurance" -> "🛡️"
        "miscellaneous", "other" -> "📦"
        else -> "📦"
    }
}

private fun getCategoryGroup(categoryName: String): String {
    return when (categoryName.lowercase()) {
        "food & dining", "food", "dining", "restaurant",
        "transportation", "transport",
        "shopping",
        "bills & utilities", "bills", "utilities" -> "Essentials"
        
        "entertainment",
        "personal care", "beauty",
        "gifts & donations", "gifts",
        "travel" -> "Lifestyle"
        
        "housing", "rent",
        "healthcare", "health",
        "education",
        "insurance" -> "Important"
        
        "financial", "investment",
        "miscellaneous", "other" -> "Others"
        
        else -> "Others"
    }
}

// ─── Wallet Icon & Color Mapping ──────────────────────────────────
private fun getWalletIcon(type: com.casha.app.domain.model.WalletType): String {
    return when (type) {
        com.casha.app.domain.model.WalletType.CASH -> "💵"
        com.casha.app.domain.model.WalletType.SAVINGS_ACCOUNT -> "🏦"
        com.casha.app.domain.model.WalletType.CHECKING_ACCOUNT -> "🏦"
        com.casha.app.domain.model.WalletType.E_WALLET -> "📱"
        com.casha.app.domain.model.WalletType.CREDIT_CARD -> "💳"
        com.casha.app.domain.model.WalletType.OTHER -> "💼"
    }
}

@Composable
private fun getWalletColor(type: com.casha.app.domain.model.WalletType): Color {
    return when (type) {
        com.casha.app.domain.model.WalletType.CASH -> Color(0xFF4CAF50)
        com.casha.app.domain.model.WalletType.SAVINGS_ACCOUNT -> Color(0xFF2196F3)
        com.casha.app.domain.model.WalletType.CHECKING_ACCOUNT -> Color(0xFF03A9F4)
        com.casha.app.domain.model.WalletType.E_WALLET -> Color(0xFF9C27B0)
        com.casha.app.domain.model.WalletType.CREDIT_CARD -> Color(0xFFFF5722)
        com.casha.app.domain.model.WalletType.OTHER -> Color(0xFF607D8B)
    }
}

private fun getWalletTypeName(type: com.casha.app.domain.model.WalletType): String {
    return when (type) {
        com.casha.app.domain.model.WalletType.CASH -> "Cash"
        com.casha.app.domain.model.WalletType.SAVINGS_ACCOUNT -> "Savings"
        com.casha.app.domain.model.WalletType.CHECKING_ACCOUNT -> "Checking"
        com.casha.app.domain.model.WalletType.E_WALLET -> "E-Wallet"
        com.casha.app.domain.model.WalletType.CREDIT_CARD -> "Credit Card"
        com.casha.app.domain.model.WalletType.OTHER -> "Other"
    }
}

// ─── Category Selector Bottom Sheet ───────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectorBottomSheet(
    categories: List<com.casha.app.domain.model.CategoryCasha>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Essentials") }
    
    val tabs = listOf("Recent", "Essentials", "Lifestyle", "Important", "Others")
    
    val groupedCategories = categories.groupBy { category -> getCategoryGroup(category.name) }
    
    val filteredCategories = if (searchQuery.isNotEmpty()) {
        categories.filter { category -> category.name.contains(searchQuery, ignoreCase = true) }
    } else {
        if (selectedTab == "Recent") {
            categories.take(5) // Show recent categories
        } else {
            groupedCategories[selectedTab] ?: emptyList()
        }
    }
    
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Select Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search categories") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            
            // Tabs
            if (searchQuery.isEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = tabs.indexOf(selectedTab),
                    modifier = Modifier.fillMaxWidth(),
                    edgePadding = 20.dp,
                    indicator = {},
                    divider = {}
                ) {
                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            onClick = { selectedTab = tab },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                   else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val tabIcon = when (tab) {
                                    "Recent" -> "🕐"
                                    "Essentials" -> "🏠"
                                    "Lifestyle" -> "✨"
                                    "Important" -> "📊"
                                    "Others" -> "📦"
                                    else -> "📦"
                                }
                                Text(tabIcon, fontSize = 16.sp)
                                Text(
                                    tab,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer 
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                items(filteredCategories.size) { index ->
                    val category = filteredCategories[index]
                    val isSelected = category.name == selectedCategory
                    val icon = getCategoryIcon(category.name)
                    
                    Surface(
                        onClick = {
                            onCategorySelected(category.name)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                               else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(icon, fontSize = 24.sp)
                            }
                            
                            Text(
                                text = category.name,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                lineHeight = 16.sp
                            )
                            
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ─── Wallet Selector Bottom Sheet ─────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSelectorBottomSheet(
    wallets: List<com.casha.app.domain.model.Wallet>,
    selectedWalletId: String?,
    onWalletSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredWallets = if (searchQuery.isNotEmpty()) {
        wallets.filter { wallet -> 
            wallet.name.contains(searchQuery, ignoreCase = true) ||
            wallet.bankName?.contains(searchQuery, ignoreCase = true) == true
        }
    } else {
        wallets
    }
    
    // Group wallets by source
    val assetWallets = filteredWallets.filter { it.source == com.casha.app.domain.model.WalletSource.ASSET }
    val loanWallets = filteredWallets.filter { it.source == com.casha.app.domain.model.WalletSource.LOAN }
    
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Select Wallet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search wallets") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Wallet List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // None option
                WalletCard(
                    wallet = null,
                    isSelected = selectedWalletId == null,
                    onClick = {
                        onWalletSelected(null)
                        onDismiss()
                    }
                )
                
                // Asset Wallets
                if (assetWallets.isNotEmpty()) {
                    Text(
                        "Assets",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    assetWallets.forEach { wallet ->
                        WalletCard(
                            wallet = wallet,
                            isSelected = wallet.id == selectedWalletId,
                            onClick = {
                                onWalletSelected(wallet.id)
                                onDismiss()
                            }
                        )
                    }
                }
                
                // Loan Wallets
                if (loanWallets.isNotEmpty()) {
                    Text(
                        "Credit Cards",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    loanWallets.forEach { wallet ->
                        WalletCard(
                            wallet = wallet,
                            isSelected = wallet.id == selectedWalletId,
                            onClick = {
                                onWalletSelected(wallet.id)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletCard(
    wallet: com.casha.app.domain.model.Wallet?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val userCurrency = CurrencyFormatter.defaultCurrency
    val currencySymbol = CurrencyFormatter.symbol(userCurrency)
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
               else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (wallet == null) MaterialTheme.colorScheme.surface
                            else getWalletColor(wallet.type).copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        wallet?.let { getWalletIcon(it.type) } ?: "❌",
                        fontSize = 24.sp
                    )
                }
                
                // Wallet Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = wallet?.name ?: "None",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    if (wallet != null) {
                        Text(
                            text = getWalletTypeName(wallet.type),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        
                        // Balance
                        val formattedBalance = CurrencyFormatter.format(wallet.balance, userCurrency)
                        Text(
                            text = if (wallet.source == com.casha.app.domain.model.WalletSource.LOAN) {
                                "Debt: $formattedBalance"
                            } else {
                                formattedBalance
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (wallet.source == com.casha.app.domain.model.WalletSource.LOAN) {
                                CashaDanger
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            }
            
            // Checkmark
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
