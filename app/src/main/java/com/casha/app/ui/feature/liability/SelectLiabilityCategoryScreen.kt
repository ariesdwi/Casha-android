package com.casha.app.ui.feature.liability

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
import com.casha.app.domain.model.LiabilityCategory

data class LiabilityCategoryItem(
    val category: LiabilityCategory,
    val displayName: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

private val categoryItems = listOf(
    LiabilityCategoryItem(
        category = LiabilityCategory.CREDIT_CARD,
        displayName = "Kartu Kredit",
        description = "BCA, Mandiri, BNI...",
        icon = Icons.Default.CreditCard,
        color = Color(0xFF6750A4)
    ),
    LiabilityCategoryItem(
        category = LiabilityCategory.PERSONAL_LOAN,
        displayName = "Pinjaman",
        description = "Pinjaman tunai & KTA",
        icon = Icons.Default.Person,
        color = Color(0xFF4CAF50)
    ),
    LiabilityCategoryItem(
        category = LiabilityCategory.MORTGAGE,
        displayName = "KPR",
        description = "Cicilan rumah, ruko",
        icon = Icons.Default.Home,
        color = Color(0xFF3F51B5)
    ),
    LiabilityCategoryItem(
        category = LiabilityCategory.AUTO_LOAN,
        displayName = "Kendaraan",
        description = "Kredit mobil, motor",
        icon = Icons.Default.DirectionsCar,
        color = Color(0xFFFF9800)
    ),
    LiabilityCategoryItem(
        category = LiabilityCategory.STUDENT_LOAN,
        displayName = "Pendidikan",
        description = "Biaya sekolah, kuliah",
        icon = Icons.Default.School,
        color = Color(0xFF009688)
    ),
    LiabilityCategoryItem(
        category = LiabilityCategory.BUSINESS_LOAN,
        displayName = "Usaha",
        description = "Modal kerja, KUR",
        icon = Icons.Default.BusinessCenter,
        color = Color(0xFF9C27B0)
    ),
    LiabilityCategoryItem(
        category = LiabilityCategory.PAY_LATER,
        displayName = "Pay Later",
        description = "Shopee, GoPay, OVO",
        icon = Icons.Default.ShoppingCart,
        color = Color(0xFFE91E63)
    ),
    LiabilityCategoryItem(
        category = LiabilityCategory.OTHER,
        displayName = "Lainnya",
        description = "Pinjaman ke teman",
        icon = Icons.Default.Money,
        color = Color(0xFF9E9E9E)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectLiabilityCategoryScreen(
    onNavigateBack: () -> Unit,
    onCategorySelected: (LiabilityCategory) -> Unit
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
                    text = "Pilih Jenis Hutang",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pilih kategori yang sesuai untuk mempermudah pencatatan dan pengelolaan.",
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
    item: LiabilityCategoryItem,
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
