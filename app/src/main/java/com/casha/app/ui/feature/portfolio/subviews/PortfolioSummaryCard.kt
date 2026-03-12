package com.casha.app.ui.feature.portfolio.subviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.R
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.AssetCategory
import com.casha.app.domain.model.PortfolioSummary
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun PortfolioSummaryCard(
    summary: PortfolioSummary,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).apply {
            timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        }
    }

    val returnAmt = summary.totalUnrealizedReturn
    val returnPct = summary.totalReturnPercentage
    val isPositive = returnAmt == null || returnAmt >= 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Color(0x332196F3))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1565C0), Color(0xFF0097A7))
                )
            )
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.portfolio_dashboard_total_assets),
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        text = CurrencyFormatter.format(summary.totalAssets, summary.currency),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                // return badge
                if (returnAmt != null && returnPct != null) {
                    val sign = if (isPositive) "+" else ""
                    val badgeBg = if (isPositive) Color(0x2500C853) else Color(0x25FF1744)
                    val badgeText = if (isPositive) Color(0xFF69F0AE) else Color(0xFFFF5252)
                    Box(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = badgeText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$sign${String.format(Locale.US, "%.1f", abs(returnPct))}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeText
                            )
                        }
                    }
                }
            }

            // Return row
            if (returnAmt != null && returnPct != null) {
                val color = if (isPositive) Color(0xFF69F0AE) else Color(0xFFFF5252)
                val sign = if (isPositive) "+" else ""
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Total Return", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
                    Text(
                        text = "$sign${CurrencyFormatter.format(abs(returnAmt), summary.currency)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = color
                    )
                }
            }

            // Allocation bar
            if (summary.breakdown.isNotEmpty()) {
                val categoryGroups = summary.breakdown
                    .groupBy { it.type.category }
                    .map { Pair(it.key, it.value.sumOf { a -> a.amount }) }
                    .filter { it.second > 0 }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Asset Allocation", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))

                    // Segmented bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        categoryGroups.forEachIndexed { index, pair ->
                            val weight = if (summary.totalAssets > 0) (pair.second / summary.totalAssets).toFloat() else 0f
                            if (weight > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(weight)
                                        .fillMaxHeight()
                                        .background(allocationColor(index))
                                )
                            }
                        }
                    }

                    // Legend chips
                    val chunked = categoryGroups.chunked(3)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        chunked.forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                rowItems.forEachIndexed { itemIndex, pair ->
                                    val originalIndex = categoryGroups.indexOf(pair)
                                    val pct = if (summary.totalAssets > 0) (pair.second / summary.totalAssets) * 100 else 0.0
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(modifier = Modifier.size(7.dp).background(allocationColor(originalIndex), CircleShape))
                                        Text(
                                            text = "${pair.first.rawValue} (${formatPct(pct)}%)",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.75f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }

            // Last updated
            Text(
                text = "Updated ${dateFormatter.format(summary.lastUpdated)} WIB",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

private fun formatPct(pct: Double): String =
    if (pct > 0 && pct < 1) String.format(Locale.US, "%.1f", pct)
    else String.format(Locale.US, "%.0f", pct)

private fun allocationColor(index: Int): Color {
    val colors = listOf(
        Color(0xFFFFEB3B),
        Color(0xFFFF9800),
        Color(0xFF9C27B0),
        Color(0xFF4CAF50),
        Color(0xFFE91E63),
        Color(0xFF009688),
        Color(0xFF2196F3)
    )
    return colors[index % colors.size]
}
