package com.casha.app.ui.feature.transaction.subview
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.casha.app.R
import com.casha.app.domain.model.CashflowType
import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.model.TransactionRequest
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.casha.app.core.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionBottomSheet(
    transaction: TransactionCasha,
    cashflowType: CashflowType,
    onDismissRequest: () -> Unit,
    onSave: (TransactionRequest) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Form States
    var name by remember { mutableStateOf(transaction.name) }
    var amount by remember { 
        mutableStateOf(
            if (transaction.amount % 1.0 == 0.0) transaction.amount.toLong().toString()
            else transaction.amount.toString()
        ) 
    }
    var category by remember { mutableStateOf(transaction.category) }
    var datetime by remember { mutableStateOf(transaction.datetime) }
    var isConfirmed by remember { mutableStateOf(transaction.isSynced) } // Using synced for the "confirmed" switch 

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant, // Light gray iOS style background
        dragHandle = null, // Custom header instead
        modifier = Modifier.fillMaxSize().padding(top = 24.dp) // Fullscreen but with a bit of space at top for iOS modal look
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                        .clickable { onDismissRequest() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        stringResource(R.string.transactions_detail_cancel), 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .clickable { /* More actions */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreHoriz, 
                        contentDescription = "More",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Transaction Details Group
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = if (cashflowType == CashflowType.INCOME) "Income Details" else stringResource(R.string.transactions_edit_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") },
                    placeholder = { Text(stringResource(R.string.transactions_edit_name_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                var isAmountFocused by remember { mutableStateOf(false) }
                // Amount Input
                OutlinedTextField(
                    value = if (isAmountFocused) amount else if (amount.isNotEmpty()) CurrencyFormatter.formatInput(amount) else "",
                    onValueChange = { amount = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { isAmountFocused = it.isFocused },
                    label = { Text("Amount") },
                    placeholder = { Text("0") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Category Picker (Simulated)
                OutlinedTextField(
                    value = category.ifEmpty { "Shopping" },
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth().clickable { /* Open Category Picker */ },
                    enabled = false, // Use as a clickable button visually
                    label = { Text(stringResource(R.string.transactions_detail_category)) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.UnfoldMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Date Group
                Text(
                    text = stringResource(R.string.transactions_detail_date),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date Input
                    OutlinedTextField(
                        value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(datetime),
                        onValueChange = { },
                        modifier = Modifier.weight(1f).clickable { /* Select Date */ },
                        enabled = false,
                        label = { Text("Date") },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Time Input
                    OutlinedTextField(
                        value = SimpleDateFormat("HH.mm", Locale.getDefault()).format(datetime),
                        onValueChange = { },
                        modifier = Modifier.weight(1f).clickable { /* Select Time */ },
                        enabled = false,
                        label = { Text("Time") },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Confirmed Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.transactions_edit_confirmed), style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isConfirmed,
                        onCheckedChange = { isConfirmed = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Save Button
            // Intentionally omitting explicit save button as per screenshot, it seems 
            // edits might be saved automatically or upon dismissal, but we'll add a
            // hidden apply or rely on the caller for now. We will call onSave when confirmed is toggled,
            // or we'll trigger save on dismiss if changes occurred. To be safe, we'll keep a small Save button
            // out of view at the bottom just in case the backend requires explicit submission.
            Button(
                onClick = {
                    val amountDouble = amount.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0
                    val request = TransactionRequest(
                        name = name,
                        amount = amountDouble,
                        category = category,
                        datetime = datetime,
                        note = ""
                    )
                    onSave(request)
                    onDismissRequest()
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (cashflowType == CashflowType.INCOME) Color(0xFF34C759) else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.transactions_edit_save), fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
