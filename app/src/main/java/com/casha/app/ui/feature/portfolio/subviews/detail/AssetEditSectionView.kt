package com.casha.app.ui.feature.portfolio.subviews.detail

import android.app.DatePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.casha.app.R
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.Asset
import com.casha.app.domain.model.AssetCategory
import com.casha.app.ui.feature.liability.forminput.CashaFormTextField
import com.casha.app.ui.feature.liability.forminput.InputCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AssetEditSectionView(
    asset: Asset,
    editName: String, onNameChange: (String) -> Unit,
    editAmount: String, onAmountChange: (String) -> Unit,
    editQuantity: String, onQuantityChange: (String) -> Unit,
    editPricePerUnit: String, onPricePerUnitChange: (String) -> Unit,
    editDescription: String, onDescriptionChange: (String) -> Unit,
    editAcquisitionDate: Date, onAcquisitionDateChange: (Date) -> Unit,
    showAcquisitionDate: Boolean, onShowAcquisitionDateChange: (Boolean) -> Unit,
    editLocation: String, onLocationChange: (String) -> Unit,
    editPurity: Int?, onPurityChange: (Int?) -> Unit,
    userCurrency: String
) {
    val context = LocalContext.current
    val isQuantityBased = asset.type.isQuantityBased
    val displayDateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    val currencySymbol = CurrencyFormatter.symbol(userCurrency)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Info Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InputCard(title = stringResource(R.string.portfolio_asset_name)) {
                CashaFormTextField(
                    value = editName,
                    onValueChange = onNameChange,
                    placeholder = "e.g., Tabungan BCA"
                )
            }

            if (isQuantityBased) {
                InputCard(title = stringResource(R.string.portfolio_asset_quantity)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CashaFormTextField(
                            value = editQuantity,
                            onValueChange = onQuantityChange,
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

                InputCard(title = stringResource(R.string.portfolio_asset_price_per_unit, asset.unit ?: asset.type.recommendedUnit ?: "unit")) {
                    CashaFormTextField(
                        value = editPricePerUnit,
                        onValueChange = onPricePerUnitChange,
                        placeholder = "0",
                        keyboardType = KeyboardType.Decimal,
                        leadingText = currencySymbol
                    )
                }
            } else {
                InputCard(title = stringResource(R.string.portfolio_asset_value)) {
                    CashaFormTextField(
                        value = editAmount,
                        onValueChange = onAmountChange,
                        placeholder = "0",
                        keyboardType = KeyboardType.Decimal,
                        leadingText = currencySymbol
                    )
                }
            }

            // Gold karat purity picker
            val isGoldType = asset.type == com.casha.app.domain.model.AssetType.GOLD_PHYSICAL
                    || asset.type == com.casha.app.domain.model.AssetType.GOLD_DIGITAL
            if (isQuantityBased && isGoldType) {
                val purityOptions = listOf(24, 22, 18, 16, 14, 10, 9)
                InputCard(title = "Karat Emas (Purity)") {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        purityOptions.forEach { karat ->
                            val isSelected = editPurity == karat
                            Surface(
                                onClick = { onPurityChange(if (isSelected) null else karat) },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${karat}K",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) androidx.compose.ui.graphics.Color.White
                                           else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    if (editPurity == null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Default: 24K jika tidak dipilih",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Additional Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InputCard(title = stringResource(R.string.portfolio_asset_acquisition_date)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        onClick = {
                            val cal = Calendar.getInstance().apply { time = editAcquisitionDate }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    cal.set(y, m, d)
                                    onAcquisitionDateChange(cal.time)
                                    onShowAcquisitionDateChange(true)
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
                            text = if (showAcquisitionDate) displayDateFormatter.format(editAcquisitionDate) else "Pilih Tanggal",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (showAcquisitionDate) {
                        IconButton(onClick = { onShowAcquisitionDateChange(false) }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            if (asset.type.category == AssetCategory.REAL_ESTATE || asset.type.category == AssetCategory.VEHICLES) {
                InputCard(title = stringResource(R.string.portfolio_asset_location)) {
                    CashaFormTextField(
                        value = editLocation,
                        onValueChange = onLocationChange,
                        placeholder = stringResource(R.string.portfolio_asset_location_placeholder)
                    )
                }
            }

            InputCard(title = stringResource(R.string.portfolio_asset_description_optional)) {
                CashaFormTextField(
                    value = editDescription,
                    onValueChange = onDescriptionChange,
                    placeholder = stringResource(R.string.portfolio_asset_description_placeholder),
                    singleLine = false
                )
            }
        }
    }
}
