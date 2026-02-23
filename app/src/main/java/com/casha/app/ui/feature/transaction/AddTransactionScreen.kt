package com.casha.app.ui.feature.transaction

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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

// ─── Design Tokens ────────────────────────────────────────────────
private val ExpenseGradient = Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53)))
private val IncomeGradient  = Brush.linearGradient(listOf(Color(0xFF00C896), Color(0xFF00B4D8)))
private val ExpenseAccent   = Color(0xFFFF6B6B)
private val IncomeAccent    = Color(0xFF00C896)
private val PageBg          = Color(0xFFF3F4FF)
private val CardBg          = Color.White
private val TextPrimary     = Color(0xFF1A1A2E)
private val TextSecondary   = Color(0xFF888AAA)
private val PurpleAccent    = Color(0xFF6C63FF)

enum class EntryType { EXPENSE, INCOME }

private val EntryType.accentColor get() = if (this == EntryType.EXPENSE) ExpenseAccent else IncomeAccent
private val EntryType.gradient    get() = if (this == EntryType.EXPENSE) ExpenseGradient else IncomeGradient
private val EntryType.gradientShadow get() = if (this == EntryType.EXPENSE) Color(0xFFFF6B6B) else Color(0xFF00C896)

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
    var selectedDate          by remember { mutableStateOf(Date()) }
    var errorMessage          by remember { mutableStateOf<String?>(null) }
    var userCurrency          by remember { mutableStateOf("IDR") }
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
    var appeared              by remember { mutableStateOf(false) }

    val isFormValid = remember(amountValue, name, entryType, selectedCategory) {
        val base = amountValue > 0 && name.isNotEmpty()
        if (entryType == EntryType.EXPENSE) base && selectedCategory.isNotEmpty() else base
    }

    LaunchedEffect(Unit) { appeared = true }

    LaunchedEffect(transactionId, uiState.rawTransactions) {
        if (isEditMode && uiState.rawTransactions.isNotEmpty()) {
            uiState.rawTransactions.find { it.id == transactionId }?.let {
                name = it.name; amountValue = it.amount
                amountText = it.amount.toLong().toString()
                selectedCategory = it.category
                note = it.note ?: ""
                selectedDate = it.datetime
                entryType = EntryType.EXPENSE
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
            viewModel.addIncome(CreateIncomeRequest(name = name.trim(), amount = amountValue, datetime = selectedDate, type = selectedIncomeType, source = source.takeIf { it.isNotEmpty() }, isRecurring = isRecurring, frequency = if (isRecurring) selectedFrequency else null, note = note.takeIf { it.isNotEmpty() }))
        }
        onNavigateBack()
    }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            Surface(color = CardBg, shadowElevation = 0.dp,
                modifier = Modifier.drawBehind {
                    drawLine(Color(0xFFEEEEF8), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(36.dp).background(Color(0xFFF0F0FF), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                            tint = PurpleAccent, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isEditMode) "Edit Transaction"
                               else if (entryType == EntryType.EXPENSE) "Add Expense" else "Add Income",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Hero Amount Card ──────────────────────────────
                AnimatedVisibility(
                    visible = appeared,
                    enter = slideInVertically(tween(500)) { -40 } + fadeIn(tween(500))
                ) {
                    AmountHeroCard(
                        entryType   = entryType,
                        amountText  = amountText,
                        amountValue = amountValue,
                        userCurrency = userCurrency,
                        onAmountChange = { digits ->
                            amountText  = digits
                            amountValue = digits.toDoubleOrNull() ?: 0.0
                            errorMessage = null
                        }
                    )
                }

                // ── Type Switcher ─────────────────────────────────
                if (!isEditMode) {
                    AnimatedVisibility(
                        visible = appeared,
                        enter = slideInVertically(tween(500, delayMillis = 80)) { -20 } + fadeIn(tween(500, 80))
                    ) {
                        TypeSwitcher(entryType = entryType, onTypeChange = { entryType = it })
                    }
                }

                // ── Name Field ────────────────────────────────────
                AnimatedVisibility(
                    visible = appeared,
                    enter = fadeIn(tween(500, delayMillis = 140))
                ) {
                    PremiumCard {
                        PremiumInputField(
                            icon        = Icons.Default.Tag,
                            iconColor   = entryType.accentColor,
                            value       = name,
                            placeholder = if (entryType == EntryType.EXPENSE) "e.g. Morning coffee" else "e.g. June Salary",
                            label       = if (entryType == EntryType.EXPENSE) "Expense Name" else "Income Name",
                            onValueChange = { name = it; errorMessage = null }
                        )
                    }
                }

                // ── Conditional Fields ────────────────────────────
                AnimatedContent(targetState = entryType, label = "fields",
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) }
                ) { type ->
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (type == EntryType.EXPENSE) {
                            // Category
                            PremiumCard {
                                PremiumDropdownField(
                                    icon       = Icons.Default.GridView,
                                    iconColor  = ExpenseAccent,
                                    label      = "Category",
                                    value      = selectedCategory.ifEmpty { "Select a category" },
                                    isPlaceholder = selectedCategory.isEmpty(),
                                    expanded   = showCategoryDropdown,
                                    onExpand   = { showCategoryDropdown = true },
                                    onDismiss  = { showCategoryDropdown = false },
                                    items      = uiState.categories.map { it.name }.ifEmpty {
                                        listOf("Food & Drink", "Transportation", "Shopping")
                                    },
                                    onSelect   = { selectedCategory = it; showCategoryDropdown = false }
                                )
                            }
                        } else {
                            // Income Type
                            PremiumCard {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    PremiumDropdownField(
                                        icon      = Icons.Default.Work,
                                        iconColor = IncomeAccent,
                                        label     = "Income Type",
                                        value     = selectedIncomeType.name.lowercase().replaceFirstChar { it.uppercase() },
                                        isPlaceholder = false,
                                        expanded  = showIncomeTypeDropdown,
                                        onExpand  = { showIncomeTypeDropdown = true },
                                        onDismiss = { showIncomeTypeDropdown = false },
                                        items     = IncomeType.values().map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                                        onSelect  = {
                                            selectedIncomeType = IncomeType.valueOf(it.uppercase())
                                            showIncomeTypeDropdown = false
                                        }
                                    )
                                    Divider(color = Color(0xFFF0F0FF))
                                    PremiumInputField(
                                        icon      = Icons.Default.Business,
                                        iconColor = IncomeAccent,
                                        value     = source,
                                        placeholder = "Source (e.g. Google)",
                                        label     = "Source (Optional)",
                                        onValueChange = { source = it }
                                    )
                                }
                            }

                            // Recurring
                            PremiumCard {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier.size(34.dp)
                                                .background(IncomeAccent.copy(alpha = 0.12f), RoundedCornerShape(9.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Repeat, contentDescription = null,
                                                tint = IncomeAccent, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Text("Recurring Income", fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                                        Switch(
                                            checked = isRecurring,
                                            onCheckedChange = { isRecurring = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = CardBg, checkedTrackColor = IncomeAccent)
                                        )
                                    }
                                    AnimatedVisibility(visible = isRecurring,
                                        enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                                        exit  = shrinkVertically(tween(300)) + fadeOut(tween(300))
                                    ) {
                                        Column {
                                            Divider(color = Color(0xFFF0F0FF))
                                            Spacer(Modifier.height(14.dp))
                                            PremiumDropdownField(
                                                icon      = Icons.Default.CalendarMonth,
                                                iconColor = IncomeAccent,
                                                label     = "Frequency",
                                                value     = selectedFrequency.name.lowercase().replaceFirstChar { it.uppercase() },
                                                isPlaceholder = false,
                                                expanded  = showFrequencyDropdown,
                                                onExpand  = { showFrequencyDropdown = true },
                                                onDismiss = { showFrequencyDropdown = false },
                                                items     = IncomeFrequency.values().map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                                                onSelect  = {
                                                    selectedFrequency = IncomeFrequency.valueOf(it.uppercase())
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

                // ── Date & Time ───────────────────────────────────
                PremiumCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(34.dp)
                                .background(PurpleAccent.copy(alpha = 0.12f), RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null,
                                tint = PurpleAccent, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Date & Time", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Text(
                                SimpleDateFormat("EEE, d MMM yyyy · HH:mm", Locale.getDefault()).format(selectedDate),
                                fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                // ── Note ──────────────────────────────────────────
                PremiumCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier.size(34.dp)
                                .background(PurpleAccent.copy(alpha = 0.12f), RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notes, contentDescription = null,
                                tint = PurpleAccent, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Note", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            BasicTextField(
                                value = note,
                                onValueChange = { note = it },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = TextPrimary),
                                cursorBrush = SolidColor(PurpleAccent),
                                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 60.dp),
                                decorationBox = { inner ->
                                    if (note.isEmpty()) Text("Add a note (optional)", fontSize = 14.sp, color = TextSecondary)
                                    inner()
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
            }

            // ── Save Button (pinned) ──────────────────────────────
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, PageBg, PageBg))
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                val scale by animateFloatAsState(if (isFormValid) 1f else 0.97f, tween(200), label = "scale")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isFormValid) entryType.gradient else Brush.linearGradient(listOf(Color(0xFFCCCCDD), Color(0xFFBBBBCC))))
                        .clickable(enabled = isFormValid) { save() }
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Save ${if (entryType == EntryType.EXPENSE) "Expense" else "Income"}",
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp
                        )
                    }
                }
            }
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
                }) { Text("OK", color = PurpleAccent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { time = selectedDate }
        val timeState = rememberTimePickerState(initialHour = cal.get(Calendar.HOUR_OF_DAY), initialMinute = cal.get(Calendar.MINUTE))
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = CardBg,
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
                }) { Text("OK", color = PurpleAccent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        )
    }
}

// ─── Amount Hero Card ─────────────────────────────────────────────
@Composable
private fun AmountHeroCard(
    entryType: EntryType,
    amountText: String,
    amountValue: Double,
    userCurrency: String,
    onAmountChange: (String) -> Unit
) {
    var isAmountFocused by remember { mutableStateOf(false) }
    val focusRequester  = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(brush = entryType.gradient)
            .shadow(
                elevation = 0.dp,
                shape     = RoundedCornerShape(24.dp),
                ambientColor  = entryType.gradientShadow.copy(alpha = 0.4f),
                spotColor     = entryType.gradientShadow.copy(alpha = 0.4f)
            )
    ) {
        // Decorative circles
        Box(modifier = Modifier.size(130.dp).offset(x = 230.dp, y = (-50).dp)
            .background(Color.White.copy(alpha = 0.07f), CircleShape))
        Box(modifier = Modifier.size(90.dp).offset(x = (-30).dp, y = 70.dp)
            .background(Color.White.copy(alpha = 0.05f), CircleShape))

        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (entryType == EntryType.EXPENSE) "TOTAL EXPENSE" else "TOTAL INCOME",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(8.dp))

            BasicTextField(
                value = amountText,
                onValueChange = { onAmountChange(it.filter { c -> c.isDigit() }) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Transparent, fontSize = 1.sp),
                cursorBrush = SolidColor(Color.Transparent),
                modifier = Modifier
                    .size(1.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { isAmountFocused = it.isFocused }
            ) { it() }

            Text(
                text = if (amountValue > 0) {
                    if (isAmountFocused) amountValue.toLong().toString()
                    else CurrencyFormatter.format(amountValue, userCurrency)
                } else "0",
                color = if (amountValue > 0) Color.White else Color.White.copy(alpha = 0.4f),
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.clickable { focusRequester.requestFocus() }
            )

            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("Tap amount to edit", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─── Type Switcher ────────────────────────────────────────────────
@Composable
private fun TypeSwitcher(entryType: EntryType, onTypeChange: (EntryType) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        EntryType.values().forEach { type ->
            val selected = entryType == type
            Box(
                modifier = Modifier.weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        animateColorAsState(
                            if (selected) type.accentColor.copy(alpha = 0.12f) else Color.Transparent,
                            tween(300), label = "bg"
                        ).value
                    )
                    .clickable { onTypeChange(type) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (type == EntryType.EXPENSE) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = animateColorAsState(if (selected) type.accentColor else TextSecondary, tween(300), label = "icon").value,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (type == EntryType.EXPENSE) "Expense" else "Income",
                        color = animateColorAsState(if (selected) type.accentColor else TextSecondary, tween(300), label = "text").value,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ─── Premium Card ─────────────────────────────────────────────────
@Composable
private fun PremiumCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = CardBg,
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.padding(16.dp)) { content() }
    }
}

// ─── Premium Input Field ──────────────────────────────────────────
@Composable
private fun PremiumInputField(
    icon: ImageVector,
    iconColor: Color,
    value: String,
    placeholder: String,
    label: String,
    onValueChange: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(34.dp)
                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold),
                cursorBrush = SolidColor(iconColor),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) Text(placeholder, fontSize = 14.sp, color = TextSecondary)
                    inner()
                }
            )
        }
    }
}

// ─── Premium Dropdown Field ───────────────────────────────────────
@Composable
private fun PremiumDropdownField(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    isPlaceholder: Boolean,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    items: List<String>,
    onSelect: (String) -> Unit
) {
    Box {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onExpand() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(34.dp)
                    .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = if (isPlaceholder) TextSecondary else TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.background(CardBg)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, fontWeight = FontWeight.Medium, color = TextPrimary) },
                    onClick = { onSelect(item) }
                )
            }
        }
    }
}