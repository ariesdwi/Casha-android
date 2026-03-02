package com.casha.app.ui.feature.portfolio.subviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.Asset
import com.casha.app.domain.model.AssetCategory
import com.casha.app.ui.util.mapSFSymbolToImageVector

@Composable
private fun getCategoryColor(category: AssetCategory): Color {
    return when (category) {
        AssetCategory.LIQUID -> Color(0xFF2196F3)     // blue
        AssetCategory.EQUITY -> Color(0xFF9C27B0)     // purple
        AssetCategory.FIXED_INCOME -> Color(0xFF009688) // teal
        AssetCategory.COMMODITIES -> Color(0xFFFF9800)  // orange
        AssetCategory.CRYPTO -> Color(0xFFFFEB3B)       // yellow
        AssetCategory.REAL_ESTATE -> Color(0xFF4CAF50)  // green
        AssetCategory.VEHICLES -> Color(0xFF3F51B5)     // indigo
        AssetCategory.BUSINESS -> Color(0xFFE91E63)     // pink
        AssetCategory.OTHERS -> Color(0xFF9E9E9E)       // gray
    }
}

@Composable
fun AssetRow(
    asset: Asset,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userCurrency = CurrencyFormatter.defaultCurrency
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = getCategoryColor(asset.type.category),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = mapSFSymbolToImageVector(asset.type.icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Name and details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
                
                Text(
                    text = if (asset.type.isQuantityBased) {
                        "${String.format("%.2f", asset.quantity ?: 0.0)} ${asset.unit ?: asset.type.recommendedUnit ?: ""}"
                    } else {
                        asset.type.displayName
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            
            // Amount and Chevron
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CurrencyFormatter.format(asset.amount, userCurrency),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


