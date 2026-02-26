package com.casha.app.ui.feature.portfolio

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun EditAssetScreen(
    asset: Asset,
    viewModel: PortfolioViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var name by remember { mutableStateOf(asset.name) }
    var amount by remember { mutableStateOf(String.format("%.0f", asset.amount)) }
    var quantity by remember { mutableStateOf(asset.quantity?.let { String.format("%.2f", it) } ?: "") }
    var pricePerUnit by remember { mutableStateOf(asset.pricePerUnit?.let { String.format("%.0f", it) } ?: "") }
    var description by remember { mutableStateOf(asset.description ?: "") }
    var acquisitionDate by remember { mutableStateOf(asset.acquisitionDate ?: Date()) }
    var showAcquisitionDate by remember { mutableStateOf(asset.acquisitionDate != null) }
    var location by remember { mutableStateOf(asset.location ?: "") }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val isQuantityBased = asset.type.isQuantityBased
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val displayDateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    val userCurrency = CurrencyFormatter.defaultCurrency
    val currencySymbol = CurrencyFormatter.symbol(userCurrency)

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
                    text = "Edit Aset",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Perbarui detail informasi aset Anda",
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
                    InputCard(title = "Nama Aset") {
                        CashaFormTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "e.g., Tabungan BCA"
                        )
                    }

                    if (isQuantityBased) {
                        InputCard(title = "Kuantitas") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CashaFormTextField(
                                    value = quantity,
                                    onValueChange = { quantity = it },
                                    placeholder = "0",
                                    keyboardType = KeyboardType.Decimal,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = asset.unit ?: asset.type.recommendedUnit ?: "unit",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                    } else {
                        InputCard(title = "Nilai Aset") {
                            CashaFormTextField(
                                value = amount,
                                onValueChange = { amount = it },
                                placeholder = "0",
                                keyboardType = KeyboardType.Decimal,
                                leadingText = currencySymbol
                            )
                        }
                    }
                }

                // Additional Section
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InputCard(title = "Tanggal Akuisisi (Opsional)") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                onClick = {
                                    val cal = Calendar.getInstance().apply { time = acquisitionDate }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            cal.set(y, m, d)
                                            acquisitionDate = cal.time
                                            showAcquisitionDate = true
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
                                    text = if (showAcquisitionDate) displayDateFormatter.format(acquisitionDate) else "Pilih Tanggal",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (showAcquisitionDate) {
                                IconButton(onClick = { showAcquisitionDate = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    if (asset.type.category == AssetCategory.REAL_ESTATE || asset.type.category == AssetCategory.VEHICLES) {
                        InputCard(title = "Lokasi") {
                            CashaFormTextField(
                                value = location,
                                onValueChange = { location = it },
                                placeholder = "e.g., Jakarta, Indonesia"
                            )
                        }
                    }

                    InputCard(title = "Catatan (Opsional)") {
                        CashaFormTextField(
                            value = description,
                            onValueChange = { description = it },
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
                        if (name.isBlank()) {
                            errorMessage = "Nama aset harus diisi"
                            return@Button
                        }
                        
                        val request = if (isQuantityBased) {
                            val qtyVal = quantity.toDoubleOrNull()
                            val priceVal = pricePerUnit.toDoubleOrNull()
                            if (qtyVal == null || priceVal == null) {
                                errorMessage = "Kuantitas dan harga harus valid"
                                return@Button
                            }
                            UpdateAssetRequest(
                                name = name,
                                amount = null,
                                quantity = qtyVal,
                                unit = asset.unit,
                                pricePerUnit = priceVal,
                                acquisitionDate = if (showAcquisitionDate) acquisitionDate else null,
                                location = location.ifBlank { null },
                                description = description.ifBlank { null }
                            )
                        } else {
                            val amtVal = amount.toDoubleOrNull()
                            if (amtVal == null) {
                                errorMessage = "Nilai aset harus valid"
                                return@Button
                            }
                            UpdateAssetRequest(
                                name = name,
                                amount = amtVal,
                                quantity = 1.0,
                                unit = null,
                                pricePerUnit = amtVal,
                                acquisitionDate = if (showAcquisitionDate) acquisitionDate else null,
                                location = location.ifBlank { null },
                                description = description.ifBlank { null }
                            )
                        }
                        
                        viewModel.updateAsset(asset.id, request) {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009033),
                        disabledContainerColor = Color(0xFF009033).copy(alpha = 0.4f)
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
                            Text("Simpan Perubahan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun validateInputs(
    isQuantityBased: Boolean,
    name: String,
    amount: String,
    quantity: String,
    pricePerUnit: String
): Boolean {
    if (name.isBlank()) return false
    
    return if (isQuantityBased) {
        quantity.isNotBlank() && pricePerUnit.isNotBlank()
    } else {
        amount.isNotBlank()
    }
}

private fun createUpdateRequest(
    isQuantityBased: Boolean,
    name: String,
    amount: String,
    quantity: String,
    pricePerUnit: String,
    showAcquisitionDate: Boolean,
    acquisitionDate: Date,
    description: String,
    location: String
): UpdateAssetRequest {
    return if (isQuantityBased) {
        UpdateAssetRequest(
            name = name,
            amount = null,
            quantity = quantity.toDoubleOrNull() ?: 0.0,
            unit = null,
            pricePerUnit = pricePerUnit.toDoubleOrNull() ?: 0.0,
            acquisitionDate = if (showAcquisitionDate) acquisitionDate else null,
            location = location.ifBlank { null },
            description = description.ifBlank { null }
        )
    } else {
        val amt = amount.toDoubleOrNull() ?: 0.0
        UpdateAssetRequest(
            name = name,
            amount = amt,
            quantity = 1.0,
            unit = null,
            pricePerUnit = amt,
            acquisitionDate = if (showAcquisitionDate) acquisitionDate else null,
            location = location.ifBlank { null },
            description = description.ifBlank { null }
        )
    }
}
