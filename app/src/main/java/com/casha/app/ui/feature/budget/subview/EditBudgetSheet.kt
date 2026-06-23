package com.casha.app.ui.feature.budget.subview
import androidx.compose.foundation.layout.fillMaxSize

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
import com.casha.app.ui.component.CurrencyInputField
import com.casha.app.domain.model.BudgetCasha
import com.casha.app.domain.model.NewBudgetRequest
import androidx.compose.ui.res.stringResource
import com.casha.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBudgetSheet(
    budget: BudgetCasha,
    onSave: (NewBudgetRequest) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountText by remember { mutableStateOf(budget.amount.toInt().toString()) }
    var amountValue by remember { mutableStateOf(budget.amount) }
    val focusRequester = remember { FocusRequester() }

    val isFormValid = amountValue > 0 && amountValue != budget.amount

    ModalBottomSheet(
modifier = Modifier.fillMaxSize(),
onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "${stringResource(R.string.budget_edit_title)} - ${budget.category}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.budget_advisor_fixed_amount),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                CurrencyInputField(
                    value = amountText,
                    onValueChange = { input ->
                        amountText = input
                        amountValue = input.toDoubleOrNull() ?: 0.0
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = { Text("0") },
                    prefix = { Text(CurrencyFormatter.symbol()) },
                    suffix = {
                        Text(
                            text = CurrencyFormatter.defaultCurrency,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
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
                Text(stringResource(R.string.budget_action_save), fontWeight = FontWeight.Bold)
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
