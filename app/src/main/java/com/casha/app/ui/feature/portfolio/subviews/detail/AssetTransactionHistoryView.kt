package com.casha.app.ui.feature.portfolio.subviews.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.Asset
import com.casha.app.domain.model.AssetTransaction
import com.casha.app.domain.model.AssetTransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AssetTransactionHistoryView(
    asset: Asset,
    transactions: List<AssetTransaction>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Riwayat Transaksi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLowest,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada riwayat transaksi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    transactions.forEachIndexed { index, transaction ->
                        AssetTransactionRow(transaction = transaction, asset = asset)
                        if (index < transactions.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssetTransactionRow(
    transaction: AssetTransaction,
    asset: Asset
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val numberFormatter = remember { 
        NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
    }
    val userCurrency = CurrencyFormatter.defaultCurrency

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        val isSaving = transaction.type == AssetTransactionType.SAVING
        Icon(
            imageVector = if (isSaving) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
            contentDescription = null,
            tint = if (isSaving) Color(0xFF4CAF50) else Color(0xFFE53935), // Green vs Red
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Left Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.type.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (transaction.quantity != null && asset.unit != null) {
                Text(
                    text = "${numberFormatter.format(transaction.quantity)} ${asset.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Text(
                text = dateFormatter.format(transaction.datetime),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        // Right Content
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = CurrencyFormatter.format(transaction.totalAmount, userCurrency),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSaving) Color(0xFF4CAF50) else Color(0xFFE53935)
            )
            
            if (transaction.pricePerUnit != null) {
                Text(
                    text = "@ ${CurrencyFormatter.format(transaction.pricePerUnit!!, userCurrency)}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            // Profit/Loss for withdrawals
            if (transaction.type == AssetTransactionType.WITHDRAW && transaction.profitLoss != null) {
                val pl = transaction.profitLoss!!
                val plText = if (pl >= 0) "+${CurrencyFormatter.format(pl, userCurrency)}" else CurrencyFormatter.format(pl, userCurrency)
                Text(
                    text = plText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (pl >= 0) Color(0xFF4CAF50) else Color(0xFFE53935)
                )
            }
        }
    }
}
