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
import androidx.compose.ui.res.stringResource
import com.casha.app.R
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.*
import com.casha.app.ui.feature.liability.forminput.CashaFormTextField
import com.casha.app.ui.feature.liability.forminput.InputCard
import com.casha.app.ui.feature.portfolio.subviews.AssetTypePicker
import com.casha.app.ui.feature.portfolio.subviews.create.CreateAssetAmountSection
import com.casha.app.ui.feature.portfolio.subviews.create.CreateAssetGenericInvestmentSection
import com.casha.app.ui.feature.portfolio.subviews.create.CreateAssetOptionalFieldsSection
import com.casha.app.ui.feature.portfolio.subviews.create.CreateAssetStockSection
import com.casha.app.ui.util.mapSFSymbolToImageVector
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssetScreen(
    selectedCategory: AssetCategory,
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
    var selectedType by remember { mutableStateOf(selectedCategory.assetTypes.first()) }
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

    // Gold karat purity (only applicable for GOLD_PHYSICAL, GOLD_DIGITAL, SILVER_PHYSICAL)
    var selectedPurity by remember { mutableStateOf<Int?>(null) }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showingTypePicker by remember { mutableStateOf(false) }

    val isQuantityBased = selectedType.isQuantityBased
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val calculatedAmount = remember(quantity, pricePerUnit) {
        val qty = quantity.toDoubleOrNull()
        val price = pricePerUnit.toDoubleOrNull()
        if (qty != null && price != null && qty > 0) qty * price else null
    }

    // Reset unit when type changes
    LaunchedEffect(selectedType) {
        if (unit.isEmpty()) {
            unit = selectedType.recommendedUnit ?: ""
        }
    }

    ModalBottomSheet(
        modifier = Modifier.fillMaxSize(),
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
                    text = stringResource(R.string.portfolio_asset_create_title),
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
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type Selection Card
                Surface(
                    onClick = { showingTypePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest
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
                                text = stringResource(R.string.portfolio_asset_type),
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
                InputCard(title = stringResource(R.string.portfolio_asset_name)) {
                    CashaFormTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = stringResource(R.string.portfolio_asset_name_placeholder)
                    )
                }

                if (isQuantityBased) {
                    if (selectedType == AssetType.STOCK) {
                        CreateAssetStockSection(
                            unit = unit, onUnitChange = { unit = it },
                            quantity = quantity, onQuantityChange = { quantity = it },
                            pricePerUnit = pricePerUnit, onPricePerUnitChange = { pricePerUnit = it },
                            calculatedAmount = calculatedAmount,
                            currencySymbol = currencySymbol,
                            formatCurrency = { CurrencyFormatter.format(it, userCurrency) }
                        )
                    } else {
                        CreateAssetGenericInvestmentSection(
                            selectedType = selectedType,
                            unit = unit, onUnitChange = { unit = it },
                            quantity = quantity, onQuantityChange = { quantity = it },
                            pricePerUnit = pricePerUnit, onPricePerUnitChange = { pricePerUnit = it },
                            calculatedAmount = calculatedAmount,
                            currencySymbol = currencySymbol,
                            formatCurrency = { CurrencyFormatter.format(it, userCurrency) },
                            selectedPurity = selectedPurity,
                            onPurityChange = { selectedPurity = it }
                        )
                    }
                } else {
                    CreateAssetAmountSection(
                        amount = amount, onAmountChange = { amount = it },
                        currencySymbol = currencySymbol
                    )
                }

                val showLocation = when (selectedType.category) {
                    AssetCategory.REAL_ESTATE, AssetCategory.VEHICLES -> true
                    else -> false
                }
                
                CreateAssetOptionalFieldsSection(
                    showAcquisitionDate = showAcquisitionDate, onShowAcquisitionDateChange = { showAcquisitionDate = it },
                    acquisitionDate = acquisitionDate, onAcquisitionDateChange = { acquisitionDate = it },
                    shouldShowLocation = showLocation,
                    location = location, onLocationChange = { location = it },
                    description = description, onDescriptionChange = { description = it }
                )

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
                                errorMessage = "Kuantitas dan harga wajib diisi" // Should match iOS portfolio.error.quantity_price_required
                                return@Button
                            }
                            if (qtyVal <= 0) {
                                errorMessage = "Kuantitas tidak valid" // portfolio.error.invalid_quantity
                                return@Button
                            }
                            if (priceVal <= 0) {
                                errorMessage = "Harga tidak valid" // portfolio.error.invalid_price
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
                                location = location.takeIf { it.isNotBlank() },
                                purity = selectedPurity
                            )
                        } else {
                            val amtVal = amount.toDoubleOrNull()
                            if (amtVal == null || amtVal <= 0) {
                                errorMessage = "Nilai aset tidak valid" // portfolio.error.invalid_amount
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
                            Text(stringResource(R.string.portfolio_action_save), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showingTypePicker) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
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
                    category = selectedCategory,
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
