package com.casha.app.ui.feature.portfolio.subviews.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.casha.app.ui.util.mapSFSymbolToImageVector
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun AssetBalanceCardView(
    asset: Asset,
    userCurrency: String,
    modifier: Modifier = Modifier
) {
    val isQuantityBased = asset.type.isQuantityBased

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                spotColor = Color(0xFF2196F3).copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2196F3), // Blue
                        Color(0xFF00ACC1) // Teal/Cyan Gradient
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Row: Icon and Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon inside circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = mapSFSymbolToImageVector(asset.type.icon),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Asset Type Badge
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = asset.type.displayName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Middle Row: Nilai Saat Ini
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Nilai Saat Ini",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = CurrencyFormatter.format(asset.amount, userCurrency),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (isQuantityBased && asset.quantity != null && asset.unit != null) {
                    Text(
                        text = "${formatNumber(asset.quantity)} ${asset.unit}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Bottom Row: 3 Columns if quantity based
            if (isQuantityBased && asset.quantity != null && asset.pricePerUnit != null) {
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    val unitStr = asset.unit ?: ""
                    val marketPrice = if (asset.quantity > 0) asset.amount / asset.quantity else 0.0
                    
                    // Col 1: Harga Beli
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                        Text("Harga Beli", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            text = "${formatCurrencyCompact(asset.pricePerUnit, userCurrency)}/$unitStr",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Col 2: Harga Pasar
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                        Text("Harga Pasar", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            text = "${formatCurrencyCompact(marketPrice, userCurrency)}/$unitStr",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Col 3: Return & Unrealized
                    val returnPct = asset.returnPercentage
                    val unrealized = asset.unrealizedReturn

                    if (returnPct != null && unrealized != null) {
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                            Text("Return", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            
                            val isPositive = returnPct >= 0
                            val retColor = if (isPositive) Color(0xFF81C784) else Color(0xFFE57373) // Lighter Green/Red on dark bg
                            val sign = if (isPositive) "+" else ""
                            val arrow = if (isPositive) "▲" else "▼"

                            Text(
                                text = "$arrow $sign${String.format(Locale.US, "%.1f", returnPct)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = retColor
                            )
                            Text(
                                text = "($sign${CurrencyFormatter.format(unrealized, userCurrency)})",
                                fontSize = 10.sp,
                                color = retColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
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
        amount >= 1_000_000_000 -> "$symbol ${format2.format(amount / 1_000_000_000)}M"
        amount >= 1_000_000 -> "$symbol ${format2.format(amount / 1_000_000)}jt"
        amount >= 1_000 -> "$symbol ${format0.format(amount / 1_000)}rb"
        else -> CurrencyFormatter.format(amount, userCurrency)
    }
}
