package com.casha.app.ui.feature.portfolio

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.casha.app.ui.feature.portfolio.subviews.AssetTypePicker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssetScreen(
    viewModel: PortfolioViewModel,
    onNavigateBack: () -> Unit,
    onSuccess: (Asset) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val userCurrency = CurrencyFormatter.defaultCurrency
    val currencySymbol = CurrencyFormatter.symbol(userCurrency)
    val displayDateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    // Form States
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AssetType.SAVINGS_ACCOUNT) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // Quantity-based fields
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var pricePerUnit by remember { mutableStateOf("") }
    
    // Additional fields
    var acquisitionDate by remember { mutableStateOf(Date()) }
    var showAcquisitionDate by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf("") }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showingTypePicker by remember { mutableStateOf(false) }

    val isQuantityBased = selectedType.isQuantityBased
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Reset unit when type changes
    LaunchedEffect(selectedType) {
        if (unit.isEmpty()) {
            unit = selectedType.recommendedUnit ?: ""
        }
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
                    text = "Tambah Aset",
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
                // Type Selection Card
                Surface(
                    onClick = { showingTypePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = mapSFSymbolToImageVector(selectedType.icon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tipe Aset",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedType.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Basic Info Section
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
                        // Quantity Section
                        InputCard(title = "Kuantitas") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CashaFormTextField(
                                    value = quantity,
                                    onValueChange = { quantity = it },
                                    placeholder = "0",
                                    keyboardType = KeyboardType.Decimal,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "|",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                )
                                Spacer(Modifier.width(8.dp))
                                CashaFormTextField(
                                    value = unit,
                                    onValueChange = { unit = it },
                                    placeholder = selectedType.recommendedUnit ?: "unit",
                                    modifier = Modifier.width(80.dp)
                                )
                            }
                        }

                        InputCard(title = "Harga per ${if (unit.isEmpty()) "unit" else unit}") {
                            CashaFormTextField(
                                value = pricePerUnit,
                                onValueChange = { pricePerUnit = it },
                                placeholder = "0",
                                keyboardType = KeyboardType.Decimal,
                                leadingText = currencySymbol
                            )
                        }

                        // Total Amount Preview
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
                    } else {
                        // Amount Section
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

                // Optional Info Section
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Acquisition Date
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

                    // Location
                    val showLocation = when (selectedType.category) {
                        AssetCategory.REAL_ESTATE, AssetCategory.VEHICLES -> true
                        else -> false
                    }
                    if (showLocation) {
                        InputCard(title = "Lokasi / Deskripsi") {
                            CashaFormTextField(
                                value = location,
                                onValueChange = { location = it },
                                placeholder = "e.g., Jakarta Pusat atau B 1234 ABC"
                            )
                        }
                    }

                    // Description
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
                            if (qtyVal == null || qtyVal <= 0 || priceVal == null || priceVal <= 0) {
                                errorMessage = "Kuantitas dan harga harus valid"
                                return@Button
                            }
                            CreateAssetRequest(
                                name = name,
                                type = selectedType,
                                amount = qtyVal * priceVal,
                                quantity = qtyVal,
                                unit = unit.ifBlank { selectedType.recommendedUnit },
                                pricePerUnit = priceVal,
                                description = description.takeIf { it.isNotBlank() },
                                acquisitionDate = if (showAcquisitionDate) acquisitionDate else null,
                                location = location.takeIf { it.isNotBlank() }
                            )
                        } else {
                            val amtVal = amount.toDoubleOrNull()
                            if (amtVal == null || amtVal <= 0) {
                                errorMessage = "Nilai aset harus valid"
                                return@Button
                            }
                            CreateAssetRequest(
                                name = name,
                                type = selectedType,
                                amount = amtVal,
                                quantity = 1.0,
                                pricePerUnit = amtVal,
                                description = description.takeIf { it.isNotBlank() },
                                acquisitionDate = if (showAcquisitionDate) acquisitionDate else null,
                                location = location.takeIf { it.isNotBlank() }
                            )
                        }
                        
                        viewModel.createAsset(request) { newAsset ->
                            onSuccess(newAsset)
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
                            Text("Simpan Aset", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showingTypePicker) {
        ModalBottomSheet(
            onDismissRequest = { showingTypePicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color(0xFFF8F9FA)
        ) {
            Column(modifier = Modifier.fillMaxHeight(0.8f)) {
                Text(
                    text = "Pilih Tipe Aset",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                AssetTypePicker(
                    selectedType = selectedType,
                    onTypeSelected = {
                        selectedType = it
                        unit = it.recommendedUnit ?: ""
                        showingTypePicker = false
                    }
                )
            }
        }
    }
}

// Reusing the mapping logic
private fun mapSFSymbolToImageVector(sfSymbol: String): ImageVector {
    return when (sfSymbol) {
        else -> Icons.AutoMirrored.Filled.ArrowBack
    }
}
