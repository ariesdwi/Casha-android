package com.casha.app.ui.feature.liability.forminput

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.Liability
import com.casha.app.domain.model.PaymentType
import com.casha.app.ui.feature.liability.LiabilityState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentFormView(
    liability: Liability,
    liabilityState: LiabilityState,
    userCurrency: String,
    onDismissRequest: () -> Unit,
    onSubmit: (amount: Double, date: Date, type: PaymentType, principal: Double?, interest: Double?, notes: String?) -> Unit
) {
    var paymentAmountText by remember { mutableStateOf("") }
    var paymentDate by remember { mutableStateOf(Date()) }
    var paymentType by remember { mutableStateOf(PaymentType.PARTIAL) }

    var principalAmountText by remember { mutableStateOf("") }
    var interestAmountText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    var paymentAmountFocused by remember { mutableStateOf(false) }

    val paymentAmountValue = paymentAmountText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val principalAmountValue = principalAmountText.replace(",", ".").toDoubleOrNull()
    val interestAmountValue = interestAmountText.replace(",", ".").toDoubleOrNull()

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    val currencySymbol = CurrencyFormatter.symbol(userCurrency)

    var showDatePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color(0xFFF8F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bayar Cicilan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${liability.name} · Sisa: ${CurrencyFormatter.format(liability.currentBalance, userCurrency)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Amount
                InputCard(title = "Jumlah Bayar *") {
                    OutlinedTextField(
                        value = if (paymentAmountFocused) paymentAmountText else if (paymentAmountText.isNotEmpty()) CurrencyFormatter.formatInput(paymentAmountText) else "",
                        onValueChange = { paymentAmountText = it.replace(",", ".") },
                        placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().onFocusChanged { paymentAmountFocused = it.isFocused },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = cashaBorderlessTextFieldColors(),
                        leadingIcon = {
                            Text(currencySymbol, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        },
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    )
                }

                // Quick amount buttons
                Row(
                    modifier = Modifier.padding(start = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    liability.monthlyInstallment?.let { monthly ->
                        Surface(
                            onClick = {
                                paymentAmountText = monthly.toLong().toString()
                                paymentType = PaymentType.PARTIAL
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF3E0)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFF9800))
                                Text("Cicilan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                            }
                        }
                    }

                    Surface(
                        onClick = {
                            paymentAmountText = liability.currentBalance.toLong().toString()
                            paymentType = PaymentType.FULL
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            "Pelunasan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Date
                InputCard(title = "Tanggal Bayar *") {
                    Surface(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(dateFormatter.format(paymentDate), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Payment Type
                InputCard(title = "Jenis Pembayaran") {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentTypeChip("Minimum", paymentType == PaymentType.MINIMUM) { paymentType = PaymentType.MINIMUM }
                        PaymentTypeChip("Sebagian", paymentType == PaymentType.PARTIAL) { paymentType = PaymentType.PARTIAL }
                        PaymentTypeChip("Lunas", paymentType == PaymentType.FULL) {
                            paymentType = PaymentType.FULL
                            paymentAmountText = liability.currentBalance.toLong().toString()
                        }
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))

                // Optional section
                SectionHeader(title = "OPSIONAL")

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InputCard(title = "Porsi Pokok", modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = principalAmountText,
                            onValueChange = { principalAmountText = it.replace(",", ".") },
                            placeholder = { Text("auto", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = cashaBorderlessTextFieldColors(),
                            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        )
                    }
                    InputCard(title = "Porsi Bunga", modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = interestAmountText,
                            onValueChange = { interestAmountText = it.replace(",", ".") },
                            placeholder = { Text("auto", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = cashaBorderlessTextFieldColors(),
                            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        )
                    }
                }
                HintRow(text = "Biarkan kosong untuk auto", icon = Icons.Default.Info)

                // Notes
                InputCard(title = "Catatan") {
                    CashaFormTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        placeholder = "e.g., Transfer via ATM",
                        singleLine = false
                    )
                }

                // Preview
                val monthlyInterest = liability.monthlyInterestAmount ?: 0.0
                val simulatedPrincipal = principalAmountValue ?: if (paymentAmountValue > monthlyInterest) paymentAmountValue - monthlyInterest else 0.0
                val simulatedInterest = interestAmountValue ?: if (paymentAmountValue > 0) minOf(paymentAmountValue, monthlyInterest) else 0.0
                val remainingBalance = maxOf(liability.currentBalance - simulatedPrincipal, 0.0)

                if (paymentAmountValue > 0) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Preview:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Pokok: ${CurrencyFormatter.format(simulatedPrincipal, userCurrency)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            Text("Bunga: ${CurrencyFormatter.format(simulatedInterest, userCurrency)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            Text(
                                "Sisa setelah bayar: ${CurrencyFormatter.format(remainingBalance, userCurrency)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Submit Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FA))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = {
                        onSubmit(
                            paymentAmountValue,
                            paymentDate,
                            paymentType,
                            principalAmountValue,
                            interestAmountValue,
                            notesText.takeIf { it.isNotBlank() }
                        )
                    },
                    enabled = paymentAmountValue > 0 && !liabilityState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009033),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
                ) {
                    if (liabilityState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Bayar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = paymentDate.time)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { paymentDate = Date(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun PaymentTypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.White,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f)),
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = Modifier.height(38.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
