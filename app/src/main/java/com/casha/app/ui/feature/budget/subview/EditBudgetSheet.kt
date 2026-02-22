package com.casha.app.ui.feature.budget.subview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.BudgetCasha
import com.casha.app.domain.model.NewBudgetRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBudgetSheet(
    budget: BudgetCasha,
    onSave: (NewBudgetRequest) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var amountText by remember { mutableStateOf(budget.amount.toInt().toString()) }
    var amountValue by remember { mutableStateOf(budget.amount) }
    var isAmountFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val isFormValid = amountValue > 0 && amountValue != budget.amount

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Edit ${budget.category} Budget",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Budget Amount",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = if (isAmountFocused) amountText else CurrencyFormatter.format(amountValue),
                    onValueChange = { input ->
                        val cleaned = input.filter { it.isDigit() }
                        if (cleaned.isNotEmpty()) {
                            amountText = cleaned
                            amountValue = cleaned.toDoubleOrNull() ?: 0.0
                        } else if (input.isEmpty()) {
                            amountText = ""
                            amountValue = 0.0
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { isAmountFocused = it.isFocused },
                    placeholder = { Text("0.00") },
                    prefix = { 
                        if (isAmountFocused) {
                            Text(CurrencyFormatter.symbol())
                        }
                    },
                    suffix = {
                        if (isAmountFocused) {
                            Text(
                                text = CurrencyFormatter.defaultCurrency,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Text(
                    text = "Current: ${CurrencyFormatter.format(budget.amount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    val request = NewBudgetRequest(
                        id = budget.id,
                        amount = amountValue,
                        month = budget.period,
                        category = budget.category
                    )
                    onSave(request)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
