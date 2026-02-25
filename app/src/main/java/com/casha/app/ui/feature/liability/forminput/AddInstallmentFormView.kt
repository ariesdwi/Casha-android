package com.casha.app.ui.feature.liability.forminput

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Save
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
import com.casha.app.ui.feature.liability.LiabilityState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInstallmentFormView(
    liability: Liability,
    liabilityState: LiabilityState,
    userCurrency: String,
    onDismissRequest: () -> Unit,
    onSubmit: (name: String, totalAmount: Double, monthlyAmount: Double, tenor: Int, currentMonth: Int, startDate: Date) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var monthlyAmount by remember { mutableStateOf("") }
    var tenor by remember { mutableStateOf("") }
    var currentMonth by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(Date()) }

    var totalAmountFocused by remember { mutableStateOf(false) }
    var monthlyAmountFocused by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() && totalAmount.isNotBlank() && monthlyAmount.isNotBlank() && tenor.isNotBlank()
    val currencySymbol = CurrencyFormatter.symbol(userCurrency)
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

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
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Tambah Cicilan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = liability.name,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
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
                // Name
                InputCard(title = "Nama Cicilan *") {
                    CashaFormTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "e.g., iPhone 16 Pro"
                    )
                }

                // Total Amount
                InputCard(title = "Total Cicilan *") {
                    OutlinedTextField(
                        value = if (totalAmountFocused) totalAmount else if (totalAmount.isNotEmpty()) CurrencyFormatter.formatInput(totalAmount) else "",
                        onValueChange = { totalAmount = it.replace(",", ".") },
                        placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().onFocusChanged { totalAmountFocused = it.isFocused },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = cashaBorderlessTextFieldColors(),
                        leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    )
                }

                // Monthly Amount
                InputCard(title = "Cicilan per Bulan *") {
                    OutlinedTextField(
                        value = if (monthlyAmountFocused) monthlyAmount else if (monthlyAmount.isNotEmpty()) CurrencyFormatter.formatInput(monthlyAmount) else "",
                        onValueChange = { monthlyAmount = it.replace(",", ".") },
                        placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().onFocusChanged { monthlyAmountFocused = it.isFocused },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = cashaBorderlessTextFieldColors(),
                        leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    )
                }

                // Tenor with quick picks
                InputCard(title = "Tenor (bulan) *") {
                    CashaFormTextField(
                        value = tenor,
                        onValueChange = { tenor = it },
                        placeholder = "12",
                        keyboardType = KeyboardType.Number
                    )
                }
                Row(
                    modifier = Modifier.padding(start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    listOf("3", "6", "12", "24", "36").forEach { t ->
                        QuickPickChip(text = t, onClick = { tenor = t })
                    }
                }

                // Current Month
                InputCard(title = "Sudah Berjalan (bulan)") {
                    CashaFormTextField(
                        value = currentMonth,
                        onValueChange = { currentMonth = it },
                        placeholder = "0",
                        keyboardType = KeyboardType.Number
                    )
                }
                HintRow(text = "Isi jika cicilan sudah berjalan sebelum pakai Casha", icon = Icons.Default.Info)

                // Start Date
                InputCard(title = "Tanggal Mulai Cicilan") {
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
                            Text(dateFormatter.format(startDate), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Submit
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
                            name,
                            totalAmount.toDoubleOrNull() ?: 0.0,
                            monthlyAmount.toDoubleOrNull() ?: 0.0,
                            tenor.toIntOrNull() ?: 0,
                            currentMonth.toIntOrNull() ?: 0,
                            startDate
                        )
                    },
                    enabled = isFormValid && !liabilityState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009033),
                        disabledContainerColor = Color(0xFF009033).copy(alpha = 0.4f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
                ) {
                    if (liabilityState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                            Text("Simpan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate.time)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDate = Date(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}
