package com.casha.app.ui.feature.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.casha.app.R
import com.casha.app.domain.model.AssetCategory

data class AssetCategoryItem(
    val category: AssetCategory,
    val displayName: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

private val categoryItems = listOf(
    AssetCategoryItem(
        category = AssetCategory.LIQUID,
        displayName = "Aset Likuid",
        description = "Kas, tabungan, deposito",
        icon = Icons.Default.AccountBalanceWallet,
        color = Color(0xFF03A9F4) // Light Blue
    ),
    AssetCategoryItem(
        category = AssetCategory.EQUITY,
        displayName = "Saham & Ekuitas",
        description = "Saham, reksadana, ETF",
        icon = Icons.Default.TrendingUp,
        color = Color(0xFF4CAF50) // Green
    ),
    AssetCategoryItem(
        category = AssetCategory.FIXED_INCOME,
        displayName = "Pendapatan Tetap",
        description = "Obligasi, SBN, Sukuk",
        icon = Icons.Default.Description,
        color = Color(0xFF3F51B5) // Indigo
    ),
    AssetCategoryItem(
        category = AssetCategory.COMMODITIES,
        displayName = "Komoditas",
        description = "Emas, perak",
        icon = Icons.Default.Stars,
        color = Color(0xFFFF9800) // Orange
    ),
    AssetCategoryItem(
        category = AssetCategory.CRYPTO,
        displayName = "Kripto",
        description = "Bitcoin, Ethereum",
        icon = Icons.Default.MonetizationOn,
        color = Color(0xFFFBC02D) // Yellow
    ),
    AssetCategoryItem(
        category = AssetCategory.REAL_ESTATE,
        displayName = "Properti",
        description = "Rumah, tanah, ruko",
        icon = Icons.Default.Home,
        color = Color(0xFF795548) // Brown
    ),
    AssetCategoryItem(
        category = AssetCategory.VEHICLES,
        displayName = "Kendaraan",
        description = "Mobil, motor",
        icon = Icons.Default.DirectionsCar,
        color = Color(0xFFF44336) // Red
    ),
    AssetCategoryItem(
        category = AssetCategory.BUSINESS,
        displayName = "Bisnis",
        description = "Kepemilikan bisnis",
        icon = Icons.Default.BusinessCenter,
        color = Color(0xFF9C27B0) // Purple
    ),
    AssetCategoryItem(
        category = AssetCategory.OTHERS,
        displayName = "Lainnya",
        description = "Aset lainnya",
        icon = Icons.Default.MoreHoriz,
        color = Color(0xFF9E9E9E) // Grey
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAssetCategoryScreen(
    onNavigateBack: () -> Unit,
    onCategorySelected: (AssetCategory) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onNavigateBack,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color(0xFFF8F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        ) {
            // Title Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.portfolio_asset_type_select),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.portfolio_asset_type_description),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            // Category Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                items(categoryItems) { item ->
                    CategoryGridItem(
                        item = item,
                        onClick = { onCategorySelected(item.category) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryGridItem(
    item: AssetCategoryItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Text
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
