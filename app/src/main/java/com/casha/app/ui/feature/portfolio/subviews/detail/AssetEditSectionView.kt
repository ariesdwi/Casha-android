package com.casha.app.ui.feature.portfolio.subviews.detail

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    editName: String,
    onNameChange: (String) -> Unit,
    editAmount: String,
    onAmountChange: (String) -> Unit,
    editQuantity: String,
    onQuantityChange: (String) -> Unit,
    editPricePerUnit: String,
    onPricePerUnitChange: (String) -> Unit,
    editDescription: String,
    onDescriptionChange: (String) -> Unit,
    editAcquisitionDate: Date,
    onAcquisitionDateChange: (Date) -> Unit,
    showAcquisitionDate: Boolean,
    onShowAcquisitionDateChange: (Boolean) -> Unit,
    editLocation: String,
    onLocationChange: (String) -> Unit,
    userCurrency: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val displayFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    val calendar = remember { Calendar.getInstance().apply { time = editAcquisitionDate } }
    val currencySymbol = CurrencyFormatter.symbol(userCurrency)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Name
        InputCard(title = "Nama Aset") {
            CashaFormTextField(
                value = editName,
                onValueChange = onNameChange,
                placeholder = "Masukkan nama aset"
            )
        }

        // Amount or Quantity
        if (asset.type.isQuantityBased) {
            InputCard(title = "Kuantitas") {
                CashaFormTextField(
                    value = editQuantity,
                    onValueChange = onQuantityChange,
                    placeholder = "0",
                    keyboardType = KeyboardType.Decimal,
                    leadingText = asset.unit ?: asset.type.recommendedUnit
                )
            }

            InputCard(title = "Harga per ${asset.unit ?: "Unit"}") {
                CashaFormTextField(
                    value = editPricePerUnit,
                    onValueChange = onPricePerUnitChange,
                    placeholder = "0",
                    keyboardType = KeyboardType.Decimal,
                    leadingText = currencySymbol
                )
            }

            // Calculated total
            val qty = editQuantity.toDoubleOrNull() ?: 0.0
            val price = editPricePerUnit.toDoubleOrNull() ?: 0.0
            if (qty > 0 && price > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Estimasi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.format(qty * price, userCurrency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            InputCard(title = "Nilai Aset") {
                CashaFormTextField(
                    value = editAmount,
                    onValueChange = onAmountChange,
                    placeholder = "0",
                    keyboardType = KeyboardType.Decimal,
                    leadingText = currencySymbol
                )
            }
        }

        // Acquisition Date
        InputCard(title = "Tanggal Akuisisi") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tampilkan Tanggal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = showAcquisitionDate,
                        onCheckedChange = onShowAcquisitionDateChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                if (showAcquisitionDate) {
                    Surface(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    calendar.set(year, month, day)
                                    onAcquisitionDateChange(calendar.time)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = displayFormatter.format(editAcquisitionDate),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }

        // Location (for applicable types)
        if (asset.type.category == AssetCategory.REAL_ESTATE || asset.type.category == AssetCategory.VEHICLES) {
            InputCard(title = "Lokasi / Plat Nomor") {
                CashaFormTextField(
                    value = editLocation,
                    onValueChange = onLocationChange,
                    placeholder = "Contoh: Jakarta atau B 1234 ABC"
                )
            }
        }

        // Description
        InputCard(title = "Deskripsi (Opsional)") {
            CashaFormTextField(
                value = editDescription,
                onValueChange = onDescriptionChange,
                placeholder = "Tambahkan catatan...",
                singleLine = false
            )
        }
    }
}
