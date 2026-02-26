package com.casha.app.ui.feature.portfolio

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.*
import com.casha.app.ui.feature.liability.forminput.CashaFormTextField
import com.casha.app.ui.feature.liability.forminput.InputCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetTransactionScreen(
    asset: Asset,
    transactionType: AssetTransactionType,
    viewModel: PortfolioViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val userCurrency = CurrencyFormatter.defaultCurrency
    val currencySymbol = CurrencyFormatter.symbol(userCurrency)
    val displayDateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    // Form States
    var quantity by remember { mutableStateOf("") }
    var pricePerUnit by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(Date()) }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isQuantityBased = asset.type.isQuantityBased
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val navigationTitle = if (!isQuantityBased) {
        if (transactionType == AssetTransactionType.SAVING) "Saving" else "Withdraw"
    } else {
        if (transactionType == AssetTransactionType.SAVING) "Buy More" else "Sell / Withdraw"
    }

    val confirmButtonLabel = if (!isQuantityBased) {
        if (transactionType == AssetTransactionType.SAVING) "Saving" else "Withdraw"
    } else {
        if (transactionType == AssetTransactionType.SAVING) "Buy" else "Sell"
    }

    val isValid = if (isQuantityBased) {
        quantity.isNotBlank() && pricePerUnit.isNotBlank()
    } else {
        amount.isNotBlank()
    }

    ModalBottomSheet(
        onDismissRequest = onNavigateBack,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color(0xFFF8F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            // ── Header ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = navigationTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Mohon lengkapi detail informasi berikut",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // ── Error Message ───────────────────────────────────
            errorMessage?.let { errorMsg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Section
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date Picker
                    InputCard(title = "Tanggal") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                onClick = {
                                    val cal = Calendar.getInstance().apply { time = date }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            cal.set(y, m, d)
                                            date = cal.time
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = displayDateFormatter.format(date),
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (!isQuantityBased) {
                        // Cash/Savings: amount only
                        InputCard(title = "Nilai Aset") {
                            CashaFormTextField(
                                value = amount,
                                onValueChange = { amount = it },
                                placeholder = "0",
                                keyboardType = KeyboardType.Decimal,
                                leadingText = currencySymbol
                            )
                        }
                    } else {
                        // Quantity-based: qty and price
                        InputCard(title = "Kuantitas") {
                            CashaFormTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                placeholder = "0",
                                keyboardType = KeyboardType.Decimal
                            )
                        }

                        InputCard(title = "Harga per unit") {
                            CashaFormTextField(
                                value = pricePerUnit,
                                onValueChange = { pricePerUnit = it },
                                placeholder = "0",
                                keyboardType = KeyboardType.Decimal,
                                leadingText = currencySymbol
                            )
                        }

                        // Total preview
                        val qtyVal = quantity.toDoubleOrNull() ?: 0.0
                        val priceVal = pricePerUnit.toDoubleOrNull() ?: 0.0
                        if (qtyVal > 0 && priceVal > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Estimasi", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = CurrencyFormatter.format(qtyVal * priceVal, userCurrency),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Notes
                    InputCard(title = "Catatan (Opsional)") {
                        CashaFormTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = "Tambahkan catatan...",
                            singleLine = false
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Submit Section ──────────────────────────────────
            Surface(
                color = Color(0xFFF8F9FA),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = {
                        val request = if (isQuantityBased) {
                            val qtyVal = quantity.toDoubleOrNull()
                            val priceVal = pricePerUnit.toDoubleOrNull()
                            if (qtyVal == null || qtyVal <= 0 || priceVal == null || priceVal <= 0) {
                                errorMessage = "Kuantitas dan harga harus diisi"
                                return@Button
                            }
                            CreateAssetTransactionRequest(
                                type = transactionType,
                                quantity = qtyVal,
                                pricePerUnit = priceVal,
                                datetime = date,
                                note = notes.ifBlank { null }
                            )
                        } else {
                            val amtVal = amount.toDoubleOrNull()
                            if (amtVal == null || amtVal <= 0) {
                                errorMessage = "Nilai harus valid"
                                return@Button
                            }
                            CreateAssetTransactionRequest(
                                type = transactionType,
                                amount = amtVal,
                                datetime = date,
                                note = notes.ifBlank { null }
                            )
                        }

                        viewModel.addAssetTransaction(asset.id, request) {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isLoading && isValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (transactionType == AssetTransactionType.SAVING) Color(0xFF009033) else MaterialTheme.colorScheme.error,
                        disabledContainerColor = (if (transactionType == AssetTransactionType.SAVING) Color(0xFF009033) else MaterialTheme.colorScheme.error).copy(alpha = 0.4f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                            Text(confirmButtonLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
