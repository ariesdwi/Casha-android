package com.casha.app.ui.feature.portfolio.subviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.Asset
import com.casha.app.domain.model.AssetCategory
import com.casha.app.ui.util.mapSFSymbolToImageVector
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
private fun getCategoryColor(category: AssetCategory): Color {
    return when (category) {
        AssetCategory.LIQUID      -> Color(0xFF1E88E5)
        AssetCategory.EQUITY      -> Color(0xFF8E24AA)
        AssetCategory.FIXED_INCOME -> Color(0xFF00897B)
        AssetCategory.COMMODITIES -> Color(0xFFFB8C00)
        AssetCategory.CRYPTO      -> Color(0xFFF9A825)
        AssetCategory.REAL_ESTATE -> Color(0xFF43A047)
        AssetCategory.VEHICLES    -> Color(0xFF3949AB)
        AssetCategory.BUSINESS    -> Color(0xFFD81B60)
        AssetCategory.OTHERS      -> Color(0xFF757575)
    }
}

private fun formatNumber(number: Double): String {
    val symbols = DecimalFormatSymbols(Locale("id", "ID"))
    return DecimalFormat("#,##0.####", symbols).format(number)
}

private fun formatCurrencyCompact(amount: Double, userCurrency: String): String {
    val symbol = CurrencyFormatter.symbol(userCurrency)
    val symbols = DecimalFormatSymbols(Locale("id", "ID"))
    val fmt2 = DecimalFormat("#,##0.##", symbols)
    val fmt0 = DecimalFormat("#,##0", symbols)
    return when {
        amount >= 1_000_000_000 -> "$symbol ${fmt2.format(amount / 1_000_000_000)}M"
        amount >= 1_000_000     -> "$symbol ${fmt2.format(amount / 1_000_000)}jt"
        amount >= 1_000         -> "$symbol ${fmt0.format(amount / 1_000)}rb"
        else -> CurrencyFormatter.format(amount, userCurrency)
    }
}

@Composable
fun AssetRow(
    asset: Asset,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userCurrency = CurrencyFormatter.defaultCurrency
    val categoryColor = getCategoryColor(asset.type.category)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp), spotColor = Color(0x1A000000)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Gradient icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(categoryColor.copy(alpha = 0.85f), categoryColor)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = mapSFSymbolToImageVector(asset.type.icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Left: name + meta
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = asset.name,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                    // Karat badge for gold
                    val isGold = asset.type == com.casha.app.domain.model.AssetType.GOLD_PHYSICAL
                            || asset.type == com.casha.app.domain.model.AssetType.GOLD_DIGITAL
                    if (isGold && asset.purity != null) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF9800).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("${asset.purity}K", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                        }
                    }
                }

                if (asset.quantity != null && asset.unit != null) {
                    Text(
                        text = "${formatNumber(asset.quantity)} ${asset.unit}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (asset.pricePerUnit != null && asset.unit != null) {
                    Text(
                        text = "Harga: ${formatCurrencyCompact(asset.pricePerUnit, userCurrency)}/${asset.unit}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Right: value + return
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = CurrencyFormatter.format(asset.amount, userCurrency),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )

                if (asset.returnPercentage != null) {
                    val isPositive = asset.returnPercentage >= 0
                    val color = if (isPositive) Color(0xFF43A047) else Color(0xFFE53935)
                    val bgColor = if (isPositive) Color(0xFFE8F5E9) else Color(0xFFFCE4EC)
                    val sign = if (isPositive) "+" else ""

                    Box(
                        modifier = Modifier
                            .background(bgColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "$sign${String.format(Locale.US, "%.1f", asset.returnPercentage)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }

                if (asset.unrealizedReturn != null) {
                    val isPositive = asset.unrealizedReturn >= 0
                    val color = if (isPositive) Color(0xFF43A047).copy(alpha = 0.8f) else Color(0xFFE53935).copy(alpha = 0.8f)
                    val sign = if (isPositive) "+" else ""
                    Text(
                        text = "$sign${CurrencyFormatter.format(asset.unrealizedReturn, userCurrency)}",
                        fontSize = 10.sp,
                        color = color
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
