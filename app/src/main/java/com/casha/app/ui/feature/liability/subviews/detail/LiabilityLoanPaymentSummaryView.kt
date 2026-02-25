package com.casha.app.ui.feature.liability.subviews.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.Liability

@Composable
fun LiabilityLoanPaymentSummaryView(
    liability: Liability,
    userCurrency: String
) {
    val totalInterest = liability.totalInterestPaid ?: 0.0
    val totalPrincipal = liability.totalPrincipalPaid ?: 0.0
    val totalPaid = totalInterest + totalPrincipal

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.PieChart, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
            Text(
                text = "Ringkasan Pembayaran",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SummaryRow(
                    label = "Total Dibayar",
                    value = CurrencyFormatter.format(totalPaid, userCurrency),
                    isBold = true
                )
                SummaryRow(
                    label = "→ Pokok",
                    value = CurrencyFormatter.format(totalPrincipal, userCurrency),
                    isBold = false
                )
                SummaryRow(
                    label = "→ Bunga",
                    value = CurrencyFormatter.format(totalInterest, userCurrency),
                    isBold = false
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, isBold: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
