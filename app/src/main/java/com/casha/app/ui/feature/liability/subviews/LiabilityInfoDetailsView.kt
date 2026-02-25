package com.casha.app.ui.feature.liability.subviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.Liability

@Composable
fun LiabilityInfoDetailsView(
    liability: Liability,
    userCurrency: String
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Info Tagihan",
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
                // Tgl Cetak Tagihan
                DetailRow(
                    icon = Icons.Outlined.Event,
                    iconColor = Color(0xFF1E88E5), // Blue
                    label = "Tgl Cetak Tagihan",
                    value = "Tanggal ${liability.billingDay ?: "-"}"
                )
                
                DividerRow()
                
                // Jatuh Tempo
                DetailRow(
                    icon = Icons.Outlined.CalendarToday,
                    iconColor = Color(0xFFE53935), // Red
                    label = "Jatuh Tempo",
                    value = "Tanggal ${liability.dueDay ?: "-"}"
                )
                
                // Bunga per Bulan
                val interestAmount = liability.monthlyInterestAmount ?: 0.0
                if (interestAmount >= 0) {
                    DividerRow()
                    DetailRow(
                        icon = Icons.Outlined.TrendingUp,
                        iconColor = Color(0xFFFF9800), // Orange
                        label = "Bunga per Bulan",
                        value = CurrencyFormatter.format(interestAmount, userCurrency)
                    )
                }

                // Denda Keterlambatan
                val lateFee = liability.lateFee ?: 0.0
                if (lateFee > 0) {
                    DividerRow()
                    DetailRow(
                        icon = Icons.Outlined.ReportProblem,
                        iconColor = Color(0xFFE53935), // Red
                        label = "Denda Keterlambatan",
                        value = CurrencyFormatter.format(lateFee, userCurrency)
                    )
                }

                // Bank
                liability.bankName?.let { bank ->
                    DividerRow()
                    DetailRow(
                        icon = Icons.Outlined.AccountBalance,
                        iconColor = Color(0xFF1E88E5), // Blue
                        label = "Bank",
                        value = bank
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
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
    Divider(
        modifier = Modifier.padding(start = 52.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
    )
}
