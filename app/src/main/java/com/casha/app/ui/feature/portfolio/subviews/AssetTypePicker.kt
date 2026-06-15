package com.casha.app.ui.feature.portfolio.subviews

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.domain.model.AssetCategory
import com.casha.app.domain.model.AssetType
import com.casha.app.ui.util.mapSFSymbolToImageVector

@Composable
fun AssetTypePicker(
    selectedType: AssetType,
    category: AssetCategory? = null,
    onTypeSelected: (AssetType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(bottom = 50.dp), // Increased from 32dp to 50dp
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        val categoriesToShow = category?.let { listOf(it) } ?: AssetCategory.entries
        categoriesToShow.forEach { cat ->
            item {
                CategorySection(
                    category = cat,
                    selectedType = selectedType,
                    onTypeSelected = onTypeSelected
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    category: AssetCategory,
    selectedType: AssetType,
    onTypeSelected: (AssetType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CategoryHeader(category)
        
        // Grid layout with 3 columns
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .heightIn(max = 2000.dp), // Allow grid to expand
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false // Disable grid scroll, use parent LazyColumn
        ) {
            items(category.assetTypes) { type ->
                AssetTypeGridItem(
                    type = type,
                    isSelected = selectedType == type,
                    onClick = { onTypeSelected(type) }
                )
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: AssetCategory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = mapSFSymbolToImageVector(category.icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = category.rawValue,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun AssetTypeGridItem(
    type: AssetType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    // Professional color palette for both modes
    val (backgroundColor, borderColor, iconColor, textColor, iconBgColor) = when {
        isSelected -> {
            if (isDark) {
                listOf(
                    Color(0xFF1B4332),  // Dark green background
                    Color(0xFF52B788),  // Light green border
                    Color(0xFF52B788),  // Icon color
                    Color(0xFF95D5B2),  // Text color
                    Color(0xFF2D6A4F)   // Icon background
                )
            } else {
                listOf(
                    Color(0xFFE8F5E9),  // Very light green background
                    Color(0xFF2E7D32),  // Green border
                    Color(0xFF2E7D32),  // Icon color
                    Color(0xFF1B5E20),  // Text color
                    Color(0xFFD4EDDA)   // Icon background
                )
            }
        }
        else -> {
            if (isDark) {
                listOf(
                    Color(0xFF1F1F1F),  // Dark card background
                    Color(0xFF3D3D3D),  // Border
                    Color(0xFFB0B0B0),  // Icon color
                    Color(0xFFE0E0E0),  // Text color
                    Color(0xFF2A2A2A)   // Icon background
                )
            } else {
                listOf(
                    MaterialTheme.colorScheme.surface,  // Use theme surface color instead of hardcoded white
                    Color(0xFFE0E0E0),  // Light border
                    Color(0xFF616161),  // Icon color
                    Color(0xFF212121),  // Text color
                    Color(0xFFF5F5F5)   // Icon background
                )
            }
        }
    }
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f), // Square cards
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        shadowElevation = if (isDark) 0.dp else if (isSelected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = mapSFSymbolToImageVector(type.icon),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Asset type name
            Text(
                text = type.displayName,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp
            )
            
            // Selected indicator
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(iconColor)
                )
            }
        }
    }
}


