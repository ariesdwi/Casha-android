package com.casha.app.ui.feature.liability.subviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import com.casha.app.domain.model.Liability
import com.casha.app.domain.model.LiabilityCategory
import com.casha.app.domain.model.LiabilityStatement
import com.casha.app.core.util.CurrencyFormatter
import java.text.NumberFormat
import java.util.*
import kotlin.math.round

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
                elevation = 10.dp, 
                shape = RoundedCornerShape(20.dp), 
                spotColor = Color.Black.copy(alpha = 0.05f),
                ambientColor = Color.Transparent
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val limit = liability.creditLimit ?: 0.0
            val currentBalance = liability.currentBalance
            val installmentUsage = liability.remainingInstallment 
                ?: liability.installmentPlans?.filter { it.isActive }?.sumOf { it.totalAmount - (it.monthlyAmount * it.currentMonth) } 
                ?: 0.0
            
            val totalUsed = currentBalance + installmentUsage
            val available = liability.availableCredit ?: (limit - totalUsed).coerceAtLeast(0.0)

            // Top Section: Tagihan Berjalan & Tersedia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: Tagihan Berjalan
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tagihan Berjalan",
                        fontSize = 15.sp,
                        color = Color(0xFF666666) // Casha Text Secondary
                    )
                    Text(
                        text = formatCurrencyWithSuperscript(currentBalance, userCurrency),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333) // Casha Text Primary
                    )
                }

                // Right: Tersedia
                if (liability.category.isRevolving && limit > 0) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tersedia",
                            fontSize = 15.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = formatCurrencyWithSuperscript(available, userCurrency),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (available > 0) Color(0xFF4CAF50) else Color(0xFFF44336) // Green or Red
                        )
                    }
                }
            }

            // Progress Bar
            if (liability.category.isRevolving && limit > 0) {
                val usagePercent = (totalUsed / limit).toFloat().coerceIn(0f, 1f)
                val balancePercent = (currentBalance / limit).toFloat().coerceIn(0f, 1f)
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Stacked Progress Bar (balance + installment)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Gray.copy(alpha = 0.12f))
                    ) {
                        // Installment portion (orange, behind)
                        if (usagePercent > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(usagePercent)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFF9800).copy(alpha = 0.5f)) // Orange
                            )
                        }
                        
                        // Balance portion (blue/red, in front)
                        if (currentBalance > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(balancePercent)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (usagePercent > 0.8f) Color(0xFFF44336) else Color(0xFF2196F3)) // Red or Blue
                            )
                        }
                    }
                    
                    // Percentage label
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                         Text(
                             text = "${(usagePercent * 100).toInt()}% terpakai",
                             fontSize = 12.sp,
                             fontWeight = FontWeight.Bold,
                             color = if (usagePercent > 0.8f) Color(0xFFF44336) else Color(0xFF666666)
                         )
                         Text(
                             text = "Limit ${formatCurrencyCompact(limit, userCurrency)}",
                             fontSize = 12.sp,
                             color = Color(0xFF666666)
                         )
                    }
                    
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    
                    // Breakdown rows
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LimitDetailRow(
                            icon = Icons.Filled.CreditCard,
                            iconColor = Color(0xFF2196F3), // Blue
                            label = "Tagihan",
                            value = formatCurrencyCompact(currentBalance, userCurrency)
                        )
                        
                        if (installmentUsage > 0) {
                            LimitDetailRow(
                                icon = Icons.Filled.AccessTimeFilled,
                                iconColor = Color(0xFFFF9800), // Orange
                                label = "Cicilan Aktif",
                                value = formatCurrencyCompact(installmentUsage, userCurrency)
                            )
                        }
                        
                        LimitDetailRow(
                            icon = Icons.Filled.CheckCircle,
                            iconColor = Color(0xFF4CAF50), // Green
                            label = "Tersedia",
                            value = formatCurrencyCompact(available, userCurrency)
                        )
                    }
                }
            }

            // Bottom Section: Tagihan Saat Ini
            if (latestStatement != null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Tagihan Saat Ini",
                                fontSize = 15.sp,
                                color = Color(0xFF666666)
                            )
                            
                            Text(
                                text = formatCurrencyWithSuperscript(latestStatement.statementBalance, userCurrency),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Jatuh Tempo:",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                                Text(
                                    text = formatLiabilityDateStyle(latestStatement.dueDate),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF333333)
                                )
                            }
                        }

                        TextButton(
                            onClick = { /* Handle Navigate to Details */ },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Detail Tagihan",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2196F3) // Blue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LimitDetailRow(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF333333)
        )
    }
}

// Compact currency format
private fun formatCurrencyCompact(amount: Double, userCurrency: String): String {
    return CurrencyFormatter.format(amount, userCurrency)
}

// Helper to format currency with superscript decimals
private fun formatCurrencyWithSuperscript(amount: Double, userCurrency: String): androidx.compose.ui.text.AnnotatedString {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    formatter.maximumFractionDigits = 0
    
    val integerPart = amount.toInt()
    val decimalPart = round((amount - integerPart) * 100).toInt()
    
    val formattedInteger = formatter.format(integerPart)
    val symbol = CurrencyFormatter.defaultCurrency // Simple fallback to global helper if needed, or pass it in
    
    val prefixStr = if (userCurrency == "IDR") "Rp " else "$userCurrency "
    
    val decimalPattern = if (decimalPart > 0) String.format("%02d", decimalPart) else ""
    
    return buildAnnotatedString {
        append(prefixStr)
        append(formattedInteger)
        if (decimalPattern.isNotEmpty()) {
            withStyle(style = SpanStyle(fontSize = 14.sp, baselineShift = androidx.compose.ui.text.style.BaselineShift.Superscript)) {
                append(decimalPattern)
            }
        }
    }
}

private fun formatLiabilityDateStyle(date: Date): String {
    val formatter = java.text.SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return formatter.format(date)
}
