package com.casha.app.ui.feature.portfolio.subviews.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.Asset
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AssetInfoDetailsView(
    asset: Asset,
    modifier: Modifier = Modifier
) {
    val fullDateFormatter = remember { SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Asset Details",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                .padding(vertical = 6.dp)
        ) {
            Column {
                // Category & Type
                DetailRow(icon = Icons.Default.Category, iconBg = Color(0xFFE3F2FD), iconTint = Color(0xFF1E88E5), label = "Category", value = asset.type.category.rawValue)
                DetailDivider()
                DetailRow(icon = Icons.Default.Label, iconBg = Color(0xFFE8F5E9), iconTint = Color(0xFF43A047), label = "Type", value = asset.type.displayName)

                // Gold purity
                val isGold = asset.type == com.casha.app.domain.model.AssetType.GOLD_PHYSICAL
                        || asset.type == com.casha.app.domain.model.AssetType.GOLD_DIGITAL
                if (isGold && asset.purity != null) {
                    DetailDivider()
                    DetailRow(
                        icon = Icons.Default.Star,
                        iconBg = Color(0xFFFFF8E1),
                        iconTint = Color(0xFFFF8F00),
                        label = "Karat Emas",
                        value = "${asset.purity}K",
                        valueColor = Color(0xFFFF8F00)
                    )
                }

                // Quantity-based fields
                if (asset.type.isQuantityBased && asset.quantity != null && asset.unit != null) {
                    val unitStr = asset.unit
                    DetailDivider()
                    DetailRow(icon = Icons.Default.Numbers, iconBg = Color(0xFFEDE7F6), iconTint = Color(0xFF7E57C2), label = "Jumlah", value = "${formatNumber(asset.quantity)} $unitStr")

                    if (asset.pricePerUnit != null) {
                        DetailDivider()
                        DetailRow(icon = Icons.Default.ShoppingCart, iconBg = Color(0xFFE3F2FD), iconTint = Color(0xFF1E88E5), label = "Harga Beli/$unitStr", value = formatCurrencyCompact(asset.pricePerUnit, asset.currency))
                    }

                    val marketPrice = if (asset.quantity > 0) asset.amount / asset.quantity else 0.0
                    val isUp = asset.pricePerUnit != null && marketPrice >= asset.pricePerUnit
                    val marketPriceColor = if (isUp) Color(0xFF43A047) else Color(0xFFE53935)
                    DetailDivider()
                    DetailRow(icon = if (isUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown, iconBg = if (isUp) Color(0xFFE8F5E9) else Color(0xFFFCE4EC), iconTint = marketPriceColor, label = "Harga Pasar/$unitStr", value = formatCurrencyCompact(marketPrice, asset.currency), valueColor = marketPriceColor)

                    if (asset.unrealizedReturn != null) {
                        val isPositive = asset.unrealizedReturn >= 0
                        val color = if (isPositive) Color(0xFF43A047) else Color(0xFFE53935)
                        val sign = if (isPositive) "+" else ""
                        DetailDivider()
                        DetailRow(
                            icon = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            iconBg = if (isPositive) Color(0xFFE8F5E9) else Color(0xFFFCE4EC),
                            iconTint = color,
                            label = "Keuntungan/Kerugian",
                            value = "$sign${CurrencyFormatter.format(asset.unrealizedReturn, asset.currency)}",
                            valueColor = color
                        )
                    }

                    if (asset.returnPercentage != null) {
                        val isPositive = asset.returnPercentage >= 0
                        val color = if (isPositive) Color(0xFF43A047) else Color(0xFFE53935)
                        val sign = if (isPositive) "+" else ""
                        DetailDivider()
                        DetailRow(
                            icon = Icons.Default.Percent,
                            iconBg = if (isPositive) Color(0xFFE8F5E9) else Color(0xFFFCE4EC),
                            iconTint = color,
                            label = "Return (%)",
                            value = "$sign${String.format(Locale.US, "%.2f", asset.returnPercentage)}%",
                            valueColor = color
                        )
                    }
                }

                DetailDivider()
                DetailRow(
                    icon = Icons.Default.AccessTime,
                    iconBg = Color(0xFFF3E5F5),
                    iconTint = Color(0xFF8E24AA),
                    label = "Last Updated",
                    value = fullDateFormatter.format(asset.updatedAt),
                    valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }

        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
    )
}

private fun formatNumber(number: Double): String {
    val symbols = DecimalFormatSymbols(Locale("id", "ID"))
    val decimalFormat = DecimalFormat("#,##0.####", symbols)
    return decimalFormat.format(number)
}

private fun formatCurrencyCompact(amount: Double, userCurrency: String): String {
    val symbol = CurrencyFormatter.symbol(userCurrency)
    val symbols = DecimalFormatSymbols(Locale("id", "ID"))
    val format2 = DecimalFormat("#,##0.##", symbols)
    val format0 = DecimalFormat("#,##0", symbols)
    
    return when {
        amount >= 1_000_000_000 -> "$symbol ${format2.format(amount / 1_000_000_000)} M"
        amount >= 1_000_000 -> "$symbol ${format2.format(amount / 1_000_000)} jt"
        amount >= 1_000 -> "$symbol ${format0.format(amount / 1_000)} rb"
        else -> CurrencyFormatter.format(amount, userCurrency)
    }
}
