package com.casha.app.ui.feature.liability.subviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.domain.model.Liability
import com.casha.app.domain.model.LiabilityCategory
import com.casha.app.domain.model.LiabilityStatement
import com.casha.app.core.util.CurrencyFormatter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LiabilityBalanceCardView(
    liability: Liability,
    latestStatement: LiabilityStatement?,
    userCurrency: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp, 
                shape = RoundedCornerShape(24.dp), 
                spotColor = Color.Black.copy(alpha = 0.05f),
                ambientColor = Color.Transparent
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Top Section: Tagihan Berjalan & Sisa Limit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: Tagihan Berjalan
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Tagihan Berjalan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.format(liability.currentBalance, userCurrency),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Right: Sisa Limit
                if (liability.category == LiabilityCategory.CREDIT_CARD && liability.creditLimit != null) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Sisa Limit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val limit = liability.creditLimit
                        val available = liability.availableCredit ?: (limit - liability.currentBalance)
                        Text(
                            text = CurrencyFormatter.format(available, userCurrency),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Progress Bar
            if (liability.category == LiabilityCategory.CREDIT_CARD && liability.creditLimit != null && liability.creditLimit > 0) {
                val usagePercent = (liability.currentBalance / liability.creditLimit).toFloat().coerceIn(0f, 1f)
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                         Text(
                             text = "Limit Terpakai",
                             fontSize = 12.sp,
                             fontWeight = FontWeight.SemiBold,
                             color = MaterialTheme.colorScheme.onSurfaceVariant
                         )
                         Text(
                             text = "${(usagePercent * 100).toInt()}%",
                             fontSize = 12.sp,
                             fontWeight = FontWeight.ExtraBold,
                             color = if (usagePercent > 0.8f) Color(0xFFFF3B30) else MaterialTheme.colorScheme.primary
                         )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFE5E5EA))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(usagePercent)
                                .fillMaxHeight()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = if (usagePercent > 0.8f) {
                                            listOf(Color(0xFFFF9800), Color(0xFFFF3B30))
                                        } else {
                                            listOf(Color(0xFF6750A4).copy(alpha = 0.8f), Color(0xFF6750A4))
                                        }
                                    )
                                )
                        )
                    }
                }
            }

            // Bottom Section: Tagihan Saat Ini
            if (latestStatement != null) {
                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Tagihan Saat Ini",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.format(latestStatement.statementBalance, userCurrency),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Jatuh Tempo:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error // Warn user
                            )
                            Text(
                                text = formatLiabilityDate(latestStatement.dueDate),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error // Warn user
                            )
                        }
                    }

                    TextButton(
                        onClick = { /* Handle Navigate to Details */ },
                        shape = RoundedCornerShape(8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Detail",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Renamed helper functions to avoid clashes with other files in the same package
internal fun formatLiabilityDate(date: Date): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return formatter.format(date)
}
