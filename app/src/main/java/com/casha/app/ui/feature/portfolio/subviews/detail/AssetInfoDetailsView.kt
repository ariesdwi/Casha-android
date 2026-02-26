package com.casha.app.ui.feature.portfolio.subviews.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.domain.model.Asset
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AssetInfoDetailsView(
    asset: Asset,
    modifier: Modifier = Modifier
) {
    val mediumDateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val fullDateFormatter = remember { SimpleDateFormat("MMM dd, yyyy, HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Detail Aset",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoRow(
                    icon = Icons.Outlined.Category,
                    iconColor = Color(0xFF1E88E5), // Blue
                    label = "Kategori",
                    value = asset.type.category.rawValue
                )

                DividerRow()

                InfoRow(
                    icon = Icons.Outlined.Label,
                    iconColor = Color(0xFF8E24AA), // Purple
                    label = "Tipe",
                    value = asset.type.displayName
                )

                asset.acquisitionDate?.let { date ->
                    DividerRow()
                    InfoRow(
                        icon = Icons.Outlined.Event,
                        iconColor = Color(0xFF4CAF50), // Green
                        label = "Tanggal Akuisisi",
                        value = mediumDateFormatter.format(date)
                    )
                }

                asset.location?.let { location ->
                    DividerRow()
                    InfoRow(
                        icon = Icons.Outlined.Place,
                        iconColor = Color(0xFFFF9800), // Orange
                        label = "Lokasi",
                        value = location
                    )
                }

                asset.description?.let { description ->
                    DividerRow()
                    InfoRow(
                        icon = Icons.Outlined.Description,
                        iconColor = Color(0xFF757575), // Grey
                        label = "Deskripsi",
                        value = description
                    )
                }

                DividerRow()

                InfoRow(
                    icon = Icons.Outlined.Update,
                    iconColor = Color(0xFF00ACC1), // Cyan
                    label = "Terakhir Diperbarui",
                    value = fullDateFormatter.format(asset.updatedAt)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
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
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DividerRow() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
    )
}
