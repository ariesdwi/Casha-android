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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.domain.model.Liability
import com.casha.app.domain.model.LiabilityCategory
import com.casha.app.domain.model.InterestType
import com.casha.app.core.util.CurrencyFormatter
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
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top row: Icon + Name + Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(categoryColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = liability.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Category Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = categoryColor.copy(alpha = 0.12f),
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

            // Info row: Bank + Interest
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = liability.bankName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Interest rate with type suffix
                val interestSuffix = liability.interestType?.displaySuffix ?: ""
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "% ${String.format(Locale.getDefault(), "%.1f", liability.interestRate)}$interestSuffix",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE53935)
                    )
                }
            }

            // Balance row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = CurrencyFormatter.format(liability.currentBalance, liability.currency ?: "IDR"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isCreditCard || liability.category == LiabilityCategory.PAY_LATER) {
                    // No progress bar for credit-based liabilities
                } else {
                    // Progress bar for loans
                    val principal = if (liability.principal > 0) liability.principal else 1.0
                    val progress = (1.0 - (liability.currentBalance / principal)).toFloat().coerceIn(0f, 1f)
                    val progressColor = when {
                        progress > 0.7f -> Color(0xFF4CAF50)
                        progress > 0.3f -> Color(0xFFFF9800)
                        else -> Color(0xFFE53935)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = progressColor,
                            trackColor = progressColor.copy(alpha = 0.2f),
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = progressColor
                        )
                    }
                }
            }

            // Overdue warning
            if (liability.isOverdue == true && liability.daysRemaining != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFE53935)
                    )
                    Text(
                        text = "Menunggak ${liability.daysRemaining} hari",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE53935)
                    )
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
