package com.casha.app.ui.feature.liability.subviews.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LiabilityLoanInfoBoxView(
    liability: Liability,
    userCurrency: String
) {
    val dateFormatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.BarChart, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
            Text(
                text = "Info Pinjaman",
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
                DetailRow(
                    icon = Icons.Default.CalendarToday,
                    iconColor = Color(0xFFE53935),
                    label = "Mulai",
                    value = dateFormatter.format(liability.startDate)
                )
                HorizontalDivider(modifier = Modifier.padding(start = 36.dp))

                DetailRow(
                    icon = Icons.Default.CalendarToday,
                    iconColor = Color(0xFFE53935),
                    label = "Selesai",
                    value = dateFormatter.format(liability.endDate)
                )
                HorizontalDivider(modifier = Modifier.padding(start = 36.dp))

                DetailRow(
                    icon = Icons.Default.Numbers,
                    iconColor = Color(0xFF2196F3),
                    label = "Tenor",
                    value = "${liability.tenor ?: 0} bulan"
                )
                HorizontalDivider(modifier = Modifier.padding(start = 36.dp))

                if (liability.remainingTenor != null) {
                    DetailRow(
                        icon = Icons.Default.HourglassBottom,
                        iconColor = Color(0xFFFF9800),
                        label = "Sisa Tenor",
                        value = "${liability.remainingTenor} bulan"
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 36.dp))
                }

                val interestSuffix = liability.interestType?.displaySuffix ?: "/yr"
                DetailRow(
                    icon = Icons.Default.TrendingUp,
                    iconColor = Color(0xFFE53935),
                    label = "Bunga",
                    value = "${String.format("%.2f", liability.interestRate)}%$interestSuffix"
                )
                HorizontalDivider(modifier = Modifier.padding(start = 36.dp))

                liability.monthlyInstallment?.let { installment ->
                    DetailRow(
                        icon = Icons.Default.Payments,
                        iconColor = Color(0xFF4CAF50),
                        label = "Cicilan/bln",
                        value = CurrencyFormatter.format(installment, userCurrency)
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 36.dp))
                }

                liability.monthlyInterestAmount?.let { interest ->
                    DetailRow(
                        icon = Icons.Default.Percent,
                        iconColor = Color(0xFF9C27B0),
                        label = "Bunga/bln",
                        value = CurrencyFormatter.format(interest, userCurrency)
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 36.dp))
                }

                liability.dueDay?.let { day ->
                    DetailRow(
                        icon = Icons.Default.Schedule,
                        iconColor = Color(0xFFE53935),
                        label = "Tgl Bayar",
                        value = "Tgl $day"
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 36.dp))
                }

                liability.bankName?.let { bank ->
                    DetailRow(
                        icon = Icons.Default.AccountBalance,
                        iconColor = Color(0xFF2196F3),
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )

        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
