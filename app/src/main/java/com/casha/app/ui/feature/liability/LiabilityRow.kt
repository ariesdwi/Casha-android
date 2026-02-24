package com.casha.app.ui.feature.liability

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.domain.model.Liability
import com.casha.app.domain.model.LiabilityCategory
import com.casha.app.core.util.CurrencyFormatter
import java.text.NumberFormat
import java.util.*

@Composable
fun LiabilityRow(
    liability: Liability,
    onClick: () -> Unit
) {
    val isCreditCard = liability.category == LiabilityCategory.CREDIT_CARD
    val categoryColor = getCategoryColor(liability.category)
    val categoryIcon = getCategoryIcon(liability.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = if (isCreditCard) {
                                listOf(Color(0xFF6750A4).copy(alpha = 0.8f), Color(0xFF6750A4))
                            } else {
                                listOf(Color(0xFFFF9800).copy(alpha = 0.8f), Color(0xFFF44336).copy(alpha = 0.9f))
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Liability Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = liability.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Category Badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = categoryColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = liability.category.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = categoryColor
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!liability.bankName.isNullOrEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = liability.bankName,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Percent,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = Color(0xFFFF9800)
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f%%", liability.interestRate),
                            fontSize = 10.sp,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Balance & Progress
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = CurrencyFormatter.format(liability.currentBalance, liability.currency ?: "IDR"),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                if (isCreditCard) {
                    liability.availableCredit?.let { available ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = Color(0xFF4CAF50)
                            )
                            Text(
                                text = CurrencyFormatter.format(available, liability.currency ?: "IDR"),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val principal = if (liability.principal > 0) liability.principal else 1.0
                    val progress = (1.0 - (liability.currentBalance / principal)).toFloat().coerceIn(0f, 1f)
                    val progressColor = when {
                        progress > 0.7f -> Color(0xFF4CAF50)
                        progress > 0.3f -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.error
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)), // Make it pill-shaped
                            color = progressColor,
                            trackColor = progressColor.copy(alpha = 0.2f),
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = progressColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getCategoryColor(category: LiabilityCategory): Color {
    return when (category) {
        LiabilityCategory.MORTGAGE -> Color(0xFF2196F3) // Blue
        LiabilityCategory.PERSONAL_LOAN -> Color(0xFF9C27B0) // Purple
        LiabilityCategory.AUTO_LOAN -> Color(0xFF4CAF50) // Green
        LiabilityCategory.STUDENT_LOAN -> Color(0xFFFF9800) // Orange
        LiabilityCategory.BUSINESS_LOAN -> Color(0xFF3F51B5) // Indigo
        LiabilityCategory.CREDIT_CARD -> Color(0xFF6750A4) // Casha Primary
        LiabilityCategory.PAY_LATER -> Color(0xFFE91E63) // Pink
        LiabilityCategory.OTHER -> Color(0xFF9E9E9E) // Gray
    }
}

@Composable
private fun getCategoryIcon(category: LiabilityCategory): ImageVector {
    return when (category) {
        LiabilityCategory.MORTGAGE -> Icons.Default.Home
        LiabilityCategory.PERSONAL_LOAN -> Icons.Default.Person
        LiabilityCategory.AUTO_LOAN -> Icons.Default.DirectionsCar
        LiabilityCategory.STUDENT_LOAN -> Icons.Default.School
        LiabilityCategory.BUSINESS_LOAN -> Icons.Default.BusinessCenter
        LiabilityCategory.CREDIT_CARD -> Icons.Default.CreditCard
        LiabilityCategory.PAY_LATER -> Icons.Default.ShoppingCart
        LiabilityCategory.OTHER -> Icons.Default.Money
    }
}
