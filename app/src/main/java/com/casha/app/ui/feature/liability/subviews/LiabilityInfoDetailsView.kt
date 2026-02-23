package com.casha.app.ui.feature.liability.subviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.casha.app.domain.model.Liability
import com.casha.app.domain.model.LiabilityCategory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LiabilityInfoDetailsView(
    liability: Liability,
    userCurrency: String
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Liability Details",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Primary Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (liability.category == LiabilityCategory.CREDIT_CARD) {
                    DetailRow(
                        icon = Icons.Default.CreditCard,
                        iconColor = Color(0xFF6750A4), // Modern Purple
                        label = "Credit Limit",
                        value = formatLiabilityCurrency(liability.creditLimit ?: 0.0, userCurrency)
                    )
                    DividerRow()
                    DetailRow(
                        icon = Icons.Default.CalendarToday,
                        iconColor = Color(0xFFFF9800), // Muted Orange
                        label = "Billing Day",
                        value = "Day ${liability.billingDay ?: 1}"
                    )
                    DividerRow()
                    DetailRow(
                        icon = Icons.Default.Schedule,
                        iconColor = Color(0xFFE53935), // Sophisticated Red
                        label = "Due Day",
                        value = "Day ${liability.dueDay ?: 25}"
                    )
                    DividerRow()
                    DetailRow(
                        icon = Icons.Default.AttachMoney,
                        iconColor = Color(0xFF43A047), // Material Green
                        label = "Minimum Payment",
                        value = "${liability.minPaymentPercentage ?: 10}%"
                    )
                } else {
                    DetailRow(
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = Color(0xFF1E88E5), // Material Blue
                        label = "Principal",
                        value = formatLiabilityCurrency(liability.principal ?: 0.0, userCurrency)
                    )
                    liability.monthlyPayment?.let { monthlyPayment ->
                        DividerRow()
                        DetailRow(
                            icon = Icons.Default.EventRepeat,
                            iconColor = Color(0xFF43A047),
                            label = "Monthly Payment",
                            value = formatLiabilityCurrency(monthlyPayment, userCurrency)
                        )
                    }
                }
            }
        }

        // Interest & Bank Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(
                    icon = Icons.Default.Percent,
                    iconColor = Color(0xFF6750A4), // Modern Purple
                    label = "Interest Rate",
                    value = "${liability.interestRate}%"
                )
                DividerRow()
                DetailRow(
                    icon = Icons.Default.TrendingUp,
                    iconColor = Color(0xFFFF9800), // Muted Orange
                    label = "Interest Type",
                    value = liability.interestType?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "N/A"
                )

                liability.bankName?.let { bank ->
                    DividerRow()
                    DetailRow(
                        icon = Icons.Default.AccountBalance,
                        iconColor = Color(0xFF1E88E5), // Material Blue
                        label = "Bank",
                        value = bank
                    )
                }
            }
        }

        // Date Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(
                    icon = Icons.Default.EventAvailable,
                    iconColor = Color(0xFF43A047), // Material Green
                    label = "Start Date",
                    value = formatLiabilityDate(liability.startDate)
                )
                DividerRow()
                DetailRow(
                    icon = Icons.Default.EventBusy,
                    iconColor = Color(0xFFE53935), // Material Red
                    label = "End Date",
                    value = formatLiabilityDate(liability.endDate)
                )
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

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
        modifier = Modifier.padding(start = 52.dp, top = 4.dp, bottom = 4.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    )
}

// Renamed helper functions to avoid clashes with other files in the same package
internal fun formatLiabilityDate(date: Date): String {
    return try {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        date.toString()
    }
}
