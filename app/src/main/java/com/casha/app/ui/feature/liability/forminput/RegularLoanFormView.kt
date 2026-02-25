package com.casha.app.ui.feature.liability.forminput

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.InterestType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RegularLoanFormView(
    principal: String,
    onPrincipalChange: (String) -> Unit,
    currentBalance: String,
    onCurrentBalanceChange: (String) -> Unit,
    tenor: String,
    onTenorChange: (String) -> Unit,
    interestRate: String,
    onInterestRateChange: (String) -> Unit,
    interestType: InterestType,
    onInterestTypeChange: (InterestType) -> Unit,
    startDate: Date,
    onStartDateChange: (Date) -> Unit,
    dueDay: String,
    onDueDayChange: (String) -> Unit,
    monthlyInstallment: String,
    onMonthlyInstallmentChange: (String) -> Unit,
    userCurrency: String
) {
    val context = LocalContext.current
    val displayFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    val calendar = remember { Calendar.getInstance().apply { time = startDate } }
    val currencySymbol = CurrencyFormatter.symbol(userCurrency)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // ── Jumlah Pinjaman Awal ────────────────────────────────
        var isPrincipalFocused by remember { mutableStateOf(false) }
        InputCard(title = "Jumlah Pinjaman Awal *") {
            OutlinedTextField(
                value = if (isPrincipalFocused) principal else if (principal.isNotEmpty()) CurrencyFormatter.formatInput(principal) else "",
                onValueChange = onPrincipalChange,
                placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().onFocusChanged { isPrincipalFocused = it.isFocused },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = cashaBorderlessTextFieldColors(),
                leadingIcon = {
                    Text(
                        currencySymbol,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        // ── Sisa Hutang Saat Ini ────────────────────────────────
        var isCurrentBalanceFocused by remember { mutableStateOf(false) }
        InputCard(title = "Sisa Hutang Saat Ini") {
            OutlinedTextField(
                value = if (isCurrentBalanceFocused) currentBalance else if (currentBalance.isNotEmpty()) CurrencyFormatter.formatInput(currentBalance) else "",
                onValueChange = onCurrentBalanceChange,
                placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().onFocusChanged { isCurrentBalanceFocused = it.isFocused },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = cashaBorderlessTextFieldColors(),
                leadingIcon = {
                    Text(
                        currencySymbol,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        HintRow(text = "Isi jika sudah berjalan", icon = Icons.Default.Info)

        // ── Tenor ───────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            InputCard(title = "Tenor (bulan) *") {
                CashaFormTextField(
                    value = tenor,
                    onValueChange = onTenorChange,
                    placeholder = "240",
                    keyboardType = KeyboardType.Number
                )
            }
            // Quick picks
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                listOf("12", "24", "36", "60", "120", "180", "240").forEach { t ->
                    QuickPickChip(text = t, onClick = { onTenorChange(t) })
                }
            }
        }

        // ── Bunga & Tipe Bunga (side-by-side) ───────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InputCard(title = "Bunga (%) *", modifier = Modifier.weight(1f)) {
                CashaFormTextField(
                    value = interestRate,
                    onValueChange = onInterestRateChange,
                    placeholder = "0.0",
                    keyboardType = KeyboardType.Decimal
                )
            }
            InputCard(title = "Tipe Bunga", modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SelectableChip(
                        text = "Monthly",
                        selected = interestType == InterestType.MONTHLY,
                        onClick = { onInterestTypeChange(InterestType.MONTHLY) },
                        modifier = Modifier.weight(1f)
                    )
                    SelectableChip(
                        text = "Yearly",
                        selected = interestType == InterestType.YEARLY,
                        onClick = { onInterestTypeChange(InterestType.YEARLY) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Tanggal Mulai Pinjaman ──────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            InputCard(title = "Tanggal Mulai Pinjaman") {
                Surface(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendar.set(year, month, day)
                                onStartDateChange(calendar.time)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = displayFormatter.format(startDate),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
            HintRow(text = "Default: hari ini", icon = Icons.Default.Info)
        }

        // ── Tgl Jatuh Tempo Bayar ───────────────────────────────
        InputCard(title = "Tgl Jatuh Tempo Bayar") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Setiap tanggal:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(end = 8.dp)
                )
                OutlinedTextField(
                    value = dueDay,
                    onValueChange = onDueDayChange,
                    placeholder = { Text("25", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = cashaBorderlessTextFieldColors(),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        // ── Cicilan per Bulan ───────────────────────────────────
        var isInstallmentFocused by remember { mutableStateOf(false) }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            InputCard(title = "Cicilan per Bulan") {
                OutlinedTextField(
                    value = if (isInstallmentFocused) monthlyInstallment else if (monthlyInstallment.isNotEmpty()) CurrencyFormatter.formatInput(monthlyInstallment) else "",
                    onValueChange = onMonthlyInstallmentChange,
                    placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { isInstallmentFocused = it.isFocused },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = cashaBorderlessTextFieldColors(),
                    leadingIcon = {
                        Text(
                            currencySymbol,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            HintRow(text = "Auto-calculated, bisa diedit", icon = Icons.Default.Info)
        }
    }
}
