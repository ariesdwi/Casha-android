package com.casha.app.ui.feature.liability.forminput

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.InterestType

@Composable
fun PayLaterFormView(
    creditLimit: String,
    onCreditLimitChange: (String) -> Unit,
    currentBalance: String,
    onCurrentBalanceChange: (String) -> Unit,
    interestRate: String,
    onInterestRateChange: (String) -> Unit,
    interestType: InterestType,
    onInterestTypeChange: (InterestType) -> Unit,
    billingDay: String,
    onBillingDayChange: (String) -> Unit,
    dueDay: String,
    onDueDayChange: (String) -> Unit,
    userCurrency: String
) {
    val currencySymbol = CurrencyFormatter.symbol(userCurrency)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // ── Limit Kredit ────────────────────────────────────────
        var isCreditLimitFocused by remember { mutableStateOf(false) }
        InputCard(title = "Limit Kredit *") {
            OutlinedTextField(
                value = if (isCreditLimitFocused) creditLimit else if (creditLimit.isNotEmpty()) CurrencyFormatter.formatInput(creditLimit) else "",
                onValueChange = onCreditLimitChange,
                placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().onFocusChanged { isCreditLimitFocused = it.isFocused },
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

        // ── Saldo Saat Ini ──────────────────────────────────────
        var isCurrentBalanceFocused by remember { mutableStateOf(false) }
        InputCard(title = "Saldo Saat Ini") {
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

        // ── Bunga & Tipe Bunga (side-by-side) ───────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InputCard(title = "Bunga (%)", modifier = Modifier.weight(1f)) {
                CashaFormTextField(
                    value = interestRate,
                    onValueChange = onInterestRateChange,
                    placeholder = "e.g., 2.95",
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

        // ── Tgl Tagihan & Jatuh Tempo (side-by-side) ────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InputCard(title = "Tgl Tagihan", modifier = Modifier.weight(1f)) {
                CashaFormTextField(
                    value = billingDay,
                    onValueChange = onBillingDayChange,
                    placeholder = "1",
                    keyboardType = KeyboardType.Number
                )
            }
            InputCard(title = "Tgl Jatuh Tempo", modifier = Modifier.weight(1f)) {
                CashaFormTextField(
                    value = dueDay,
                    onValueChange = onDueDayChange,
                    placeholder = "25",
                    keyboardType = KeyboardType.Number
                )
            }
        }
    }
}
