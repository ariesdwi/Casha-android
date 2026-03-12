package com.casha.app.ui.feature.portfolio.subviews.create

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.R
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.AssetCategory
import com.casha.app.domain.model.AssetType
import com.casha.app.ui.feature.liability.forminput.CashaFormTextField
import com.casha.app.ui.feature.liability.forminput.InputCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CreateAssetStockSection(
    unit: String, onUnitChange: (String) -> Unit,
    quantity: String, onQuantityChange: (String) -> Unit,
    pricePerUnit: String, onPricePerUnitChange: (String) -> Unit,
    calculatedAmount: Double?,
    currencySymbol: String,
    formatCurrency: (Double) -> String
) {
    val commonStockTickers = listOf(
        "BBCA.JK" to "BCA", "BBRI.JK" to "BRI", "BMRI.JK" to "Mandiri",
        "TLKM.JK" to "Telkom", "ASII.JK" to "Astra", "GOTO.JK" to "GoTo",
        "UNVR.JK" to "Unilever", "BYAN.JK" to "Bayan", "ICBP.JK" to "Indofood",
        "AAPL" to "Apple", "GOOGL" to "Google", "MSFT" to "Microsoft"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Info banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF4CAF50).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF33994C))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Panduan Pengisian Saham",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "• unit = Ticker Yahoo Finance (contoh: BBCA.JK)\n• quantity = Jumlah lembar (1 lot = 100 lembar)\n• pricePerUnit = Harga beli per lembar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Ticker field wrapper (InputCard equivalent for this screen context)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Kode Saham (Ticker)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                CashaFormTextField(
                    value = unit,
                    onValueChange = { onUnitChange(it.uppercase()) }, // Auto capitalize
                    placeholder = "contoh: BBCA.JK atau AAPL"
                )
                
                // Quick-pick buttons
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commonStockTickers.forEach { (ticker, label) ->
                        val isSelected = unit == ticker
                        Surface(
                            onClick = { onUnitChange(ticker) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = ticker, 
                                    style = MaterialTheme.typography.labelSmall, 
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = label, 
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quantity (lembar)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Jumlah Lembar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CashaFormTextField(
                        value = quantity,
                        onValueChange = onQuantityChange,
                        placeholder = "0",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    val qtyVal = quantity.toDoubleOrNull()
                    if (qtyVal != null && qtyVal > 0) {
                        val lots = (qtyVal / 100).toInt()
                        val remainder = (qtyVal % 100).toInt()
                        val remainderText = if (remainder > 0) "+ $remainder lembar" else ""
                        Text(
                            text = "$lots lot $remainderText",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "1 lot = 100 lembar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Harga per lembar
        InputCard(title = "Harga Beli per Lembar") {
            CashaFormTextField(
                value = pricePerUnit,
                onValueChange = onPricePerUnitChange,
                placeholder = "0",
                keyboardType = KeyboardType.Decimal,
                leadingText = currencySymbol
            )
        }

        // Total preview
        if (calculatedAmount != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Total Nilai Investasi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatCurrency(calculatedAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                val qtyVal = quantity.toDoubleOrNull()
                if (qtyVal != null && qtyVal >= 100) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${(qtyVal / 100).toInt()} lot",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateAssetGenericInvestmentSection(
    selectedType: AssetType,
    unit: String, onUnitChange: (String) -> Unit,
    quantity: String, onQuantityChange: (String) -> Unit,
    pricePerUnit: String, onPricePerUnitChange: (String) -> Unit,
    calculatedAmount: Double?,
    currencySymbol: String,
    formatCurrency: (Double) -> String,
    selectedPurity: Int? = null,
    onPurityChange: (Int?) -> Unit = {}
) {
    LaunchedEffect(selectedType) {
        if (unit.isEmpty()) {
            onUnitChange(selectedType.recommendedUnit ?: "unit")
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Info banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Masukkan jumlah & harga beli satuan",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Backend akan otomatis menghitung total nilai dan melacak untung/rugi secara real-time.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Gold purity picker
        val isGoldType = selectedType == AssetType.GOLD_PHYSICAL || selectedType == AssetType.GOLD_DIGITAL
        if (isGoldType) {
            val purityOptions = listOf(24, 22, 18, 16, 14, 10, 9)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Karat Emas (Purity)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        purityOptions.forEach { karat ->
                            val isSelected = selectedPurity == karat
                            Surface(
                                onClick = {
                                    onPurityChange(if (isSelected) null else karat)
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${karat}K",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White
                                           else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    if (selectedPurity == null) {
                        Text(
                            "Default: 24K jika tidak dipilih",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        InputCard(title = stringResource(R.string.portfolio_asset_quantity)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CashaFormTextField(
                    value = quantity,
                    onValueChange = onQuantityChange,
                    placeholder = "0",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                
                Divider(
                    modifier = Modifier
                        .height(30.dp)
                        .padding(horizontal = 8.dp)
                        .width(1.dp)
                )
                
                CashaFormTextField(
                    value = unit,
                    onValueChange = onUnitChange,
                    placeholder = selectedType.recommendedUnit ?: "unit",
                    modifier = Modifier.width(80.dp)
                )
            }
        }
        
        InputCard(title = stringResource(R.string.portfolio_asset_price_per_unit, if (unit.isEmpty()) "unit" else unit)) {
            CashaFormTextField(
                value = pricePerUnit,
                onValueChange = onPricePerUnitChange,
                placeholder = "0",
                keyboardType = KeyboardType.Decimal,
                leadingText = currencySymbol
            )
        }
        
        // Calculated Total
        if (calculatedAmount != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.portfolio_asset_total_amount), 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(calculatedAmount),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CreateAssetAmountSection(
    amount: String, onAmountChange: (String) -> Unit,
    currencySymbol: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFF9800).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFF9800))
            Text(
                text = "Masukkan total saldo/nilai aset saat ini.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        InputCard(title = stringResource(R.string.portfolio_asset_amount)) {
            CashaFormTextField(
                value = amount,
                onValueChange = onAmountChange,
                placeholder = "0",
                keyboardType = KeyboardType.Decimal,
                leadingText = currencySymbol
            )
        }
    }
}

@Composable
fun CreateAssetOptionalFieldsSection(
    showAcquisitionDate: Boolean, onShowAcquisitionDateChange: (Boolean) -> Unit,
    acquisitionDate: Date, onAcquisitionDateChange: (Date) -> Unit,
    shouldShowLocation: Boolean,
    location: String, onLocationChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit
) {
    val context = LocalContext.current
    val displayDateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Acquisition Date Toggle
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.portfolio_asset_acquisition_date), style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = showAcquisitionDate,
                    onCheckedChange = onShowAcquisitionDateChange,
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                )
            }
        }
        
        if (showAcquisitionDate) {
            Surface(
                onClick = {
                    val cal = Calendar.getInstance().apply { time = acquisitionDate }
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            cal.set(y, m, d)
                            onAcquisitionDateChange(cal.time)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = displayDateFormatter.format(acquisitionDate),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // Location
        if (shouldShowLocation) {
            InputCard(title = stringResource(R.string.portfolio_asset_location)) {
                CashaFormTextField(
                    value = location,
                    onValueChange = onLocationChange,
                    placeholder = stringResource(R.string.portfolio_asset_location_placeholder)
                )
            }
        }
        
        // Description
        InputCard(title = stringResource(R.string.portfolio_asset_description_optional)) {
            CashaFormTextField(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = stringResource(R.string.portfolio_asset_description_placeholder),
                singleLine = false
            )
        }
    }
}
