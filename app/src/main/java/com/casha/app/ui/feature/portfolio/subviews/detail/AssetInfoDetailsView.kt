package com.casha.app.ui.feature.portfolio.subviews.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoRow(
                    label = "Kategori",
                    value = asset.type.category.rawValue
                )

                DividerRow()

                InfoRow(
                    label = "Tipe",
                    value = asset.type.displayName
                )

                asset.acquisitionDate?.let { date ->
                    DividerRow()
                    InfoRow(
                        label = "Tanggal Akuisisi",
                        value = mediumDateFormatter.format(date)
                    )
                }

                asset.location?.let { location ->
                    DividerRow()
                    InfoRow(
                        label = "Lokasi",
                        value = location
                    )
                }

                asset.description?.let { description ->
                    DividerRow()
                    InfoRow(
                        label = "Deskripsi",
                        value = description
                    )
                }

                DividerRow()

                InfoRow(
                    label = "Terakhir Diperbarui",
                    value = fullDateFormatter.format(asset.updatedAt)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DividerRow() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
    )
}
