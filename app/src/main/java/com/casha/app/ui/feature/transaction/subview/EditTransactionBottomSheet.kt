package com.casha.app.ui.feature.transaction.subview
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.casha.app.R
import com.casha.app.domain.model.CashflowType
import com.casha.app.domain.model.CategoryCasha
import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.model.TransactionRequest
import java.text.SimpleDateFormat
import java.util.*
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.ui.component.CurrencyInputField
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionBottomSheet(
    transaction: TransactionCasha,
    cashflowType: CashflowType,
    categories: List<CategoryCasha> = emptyList(),
    onDismissRequest: () -> Unit,
    onSave: (TransactionRequest) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val userCurrency = CurrencyFormatter.defaultCurrency
    val currencySymbol = CurrencyFormatter.symbol(userCurrency)

    // Form States
    var name by remember { mutableStateOf(transaction.name) }
    var amount by remember {
        mutableStateOf(
            if (transaction.amount % 1.0 == 0.0) transaction.amount.toLong().toString()
            else String.format("%.2f", transaction.amount)
        )
    }
    var amountValue by remember { mutableStateOf(transaction.amount) }
    var category by remember { mutableStateOf(transaction.category) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var datetime by remember { mutableStateOf(transaction.datetime) }
    var note by remember { mutableStateOf(transaction.note ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("EEE, d MMM yyyy · HH:mm", Locale("id", "ID")) }

    val isFormValid = amountValue > 0 && name.isNotEmpty() && category.isNotEmpty()
    
    // Task 11.2: Sync state for inline indicator
    val isSynced = transaction.isSynced

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null,
        modifier = Modifier.fillMaxSize().padding(top = 24.dp)
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(
                            stringResource(R.string.transactions_detail_cancel),
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = if (cashflowType == CashflowType.INCOME) "Edit Income" else stringResource(R.string.transactions_edit_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    TextButton(
                        onClick = {
                            val request = TransactionRequest(
                                name = name,
                                amount = amountValue,
                                category = category,
                                datetime = datetime,
                                note = note.takeIf { it.isNotEmpty() }
                            )
                            onSave(request)
                            onDismissRequest()
                        },
                        enabled = isFormValid
                    ) {
                        Text(
                            stringResource(R.string.transactions_edit_save),
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
                    .imePadding()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Task 11.2: Inline sync status indicator
                if (!isSynced) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Syncing with server...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // ── Amount ───────────────────────────────────────
                InputCard(title = "Jumlah *") {
                    CurrencyInputField(
                        value = amount,
                        onValueChange = {
                            amount = it
                            amountValue = it.toDoubleOrNull() ?: 0.0
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
                        onValueChange = { name = it },
                        placeholder = stringResource(R.string.transactions_edit_name_placeholder)
                    )
                }

                // ── Category ─────────────────────────────────────
                InputCard(title = stringResource(R.string.transactions_detail_category) + " *") {
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
                                    text = category.ifEmpty { "Select" },
                                    fontSize = 15.sp,
                                    fontWeight = if (category.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (category.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        category = cat.name
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ── Date & Time ──────────────────────────────────
                InputCard(title = stringResource(R.string.transactions_detail_date)) {
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
                                text = dateFormatter.format(datetime),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
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

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ── Date Picker ─────────────────────────────────────────
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = datetime.time
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = millis
                                val cur = Calendar.getInstance().apply { time = datetime }
                                set(Calendar.HOUR_OF_DAY, cur.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, cur.get(Calendar.MINUTE))
                            }
                            datetime = cal.time
                        }
                        showDatePicker = false
                        showTimePicker = true
                    }) {
                        Text("OK", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.transactions_detail_cancel), color = Color.Gray)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // ── Time Picker ─────────────────────────────────────────
        if (showTimePicker) {
            val cal = Calendar.getInstance().apply { time = datetime }
            val timeState = rememberTimePickerState(
                initialHour = cal.get(Calendar.HOUR_OF_DAY),
                initialMinute = cal.get(Calendar.MINUTE)
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
                title = { Text("Pilih Waktu", fontWeight = FontWeight.Bold) },
                text = { TimePicker(state = timeState) },
                confirmButton = {
                    TextButton(onClick = {
                        val updated = Calendar.getInstance().apply {
                            time = datetime
                            set(Calendar.HOUR_OF_DAY, timeState.hour)
                            set(Calendar.MINUTE, timeState.minute)
                        }
                        datetime = updated.time
                        showTimePicker = false
                    }) {
                        Text("OK", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text(stringResource(R.string.transactions_detail_cancel), color = Color.Gray)
                    }
                }
            )
        }
    }
}

// ─── InputCard ────────────────────────────────────────────────────
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
