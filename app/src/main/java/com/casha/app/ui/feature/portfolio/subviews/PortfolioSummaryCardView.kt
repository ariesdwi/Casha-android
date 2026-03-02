package com.casha.app.ui.feature.portfolio.subviews

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.AssetBreakdown
import com.casha.app.domain.model.AssetType
import com.casha.app.domain.model.PortfolioSummary
import com.casha.app.ui.theme.*
import com.casha.app.ui.util.mapSFSymbolToImageVector
import java.util.Date

@Composable
fun PortfolioSummaryCard(
    summary: PortfolioSummary?,
    isLoading: Boolean,
    onTap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.Black.copy(alpha = 0.05f),
                ambientColor = Color.Transparent
            )
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onTap() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Total Assets",
                    style = CashaTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (summary != null) {
                    Text(
                        text = CurrencyFormatter.format(summary.totalAssets, summary.currency),
                        style = CashaTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = "--",
                        style = CashaTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.PieChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Asset Breakdown
        if (summary != null && summary.breakdown.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                summary.breakdown.take(3).forEach { breakdown ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = mapSFSymbolToImageVector(breakdown.type.icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = breakdown.type.displayName,
                            style = CashaTypography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Text(
                            text = CurrencyFormatter.format(breakdown.amount, summary.currency),
                            style = CashaTypography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (summary.breakdown.size > 3) {
                Text(
                    text = "+ ${summary.breakdown.size - 3} more",
                    style = CashaTypography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PortfolioSummaryCardPreview() {
    val sampleSummary = PortfolioSummary(
        currency = "IDR",
        totalAssets = 15000000.0,
        breakdown = listOf(
            AssetBreakdown(type = AssetType.SAVINGS_ACCOUNT, amount = 10000000.0, count = 2),
            AssetBreakdown(type = AssetType.INSURANCE_CASH_VALUE, amount = 5000000.0, count = 1),
            AssetBreakdown(type = AssetType.GOLD_PHYSICAL, amount = 1000000.0, count = 1),
            AssetBreakdown(type = AssetType.STOCK, amount = 2000000.0, count = 5)
        ),
        assets = emptyList(),
        lastUpdated = Date()
    )

    Column(modifier = Modifier.padding(16.dp)) {
        PortfolioSummaryCard(
            summary = sampleSummary,
            isLoading = false,
            onTap = {}
        )
        Spacer(modifier = Modifier.height(16.dp))
        PortfolioSummaryCard(
            summary = null,
            isLoading = true,
            onTap = {}
        )
    }
}
