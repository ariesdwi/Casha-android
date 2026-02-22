package com.casha.app.ui.feature.transaction.subview

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
import com.casha.app.domain.model.CashflowType
import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.model.TransactionRequest
import java.text.SimpleDateFormat
import java.util.*

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
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var category by remember { mutableStateOf(transaction.category) }
    var datetime by remember { mutableStateOf(transaction.datetime) }
    var isConfirmed by remember { mutableStateOf(transaction.isSynced) } // Using synced for the "confirmed" switch 

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant, // Light gray iOS style background
        dragHandle = null, // Custom header instead
        modifier = Modifier.padding(top = 24.dp) // Leave a bit of space at top for iOS modal look
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                        "transactions.detail.cancel", 
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Transaction Details",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        // Name Input
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            placeholder = { Text("Name") },
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                        
                        // Amount Input
                        TextField(
                            value = amount,
                            onValueChange = { amount = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            leadingIcon = { Text("Rp ", style = MaterialTheme.typography.bodyLarge) },
                            placeholder = { Text("0") },
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                        
                        // Category Picker
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Open Category Picker */ }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Category", style = MaterialTheme.typography.bodyLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = category.ifEmpty { "Shopping" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.UnfoldMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp).size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Date Group
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "transactions.detail.date",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Date Pill
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .clickable { /* Select Date */ }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(datetime),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            // Time Pill
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .clickable { /* Select Time */ }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = SimpleDateFormat("HH.mm", Locale.getDefault()).format(datetime),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Confirmed Switch
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("transactions.edit.confirmed", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isConfirmed,
                        onCheckedChange = { isConfirmed = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF34C759) // iOS Green
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
