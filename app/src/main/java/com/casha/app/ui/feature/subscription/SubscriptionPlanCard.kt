package com.casha.app.ui.feature.subscription

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.casha.app.R
import com.casha.app.ui.theme.CashaPrimaryLight

@Composable
fun SubscriptionPlanCard(
    product: ProductDetails,
    isSelected: Boolean,
    isBestValue: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) CashaPrimaryLight else Color.Black.copy(alpha = 0.05f),
        label = "borderColor"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        label = "borderWidth"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) CashaPrimaryLight.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.5f),
        label = "containerColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio Button
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(2.dp, if (isSelected) CashaPrimaryLight else Color.Black.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(CashaPrimaryLight)
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                val subtitle = when {
                    product.productId.contains("yearly") -> stringResource(R.string.subscription_plan_yearly_save)
                    product.productId.contains("monthly") -> stringResource(R.string.subscription_plan_monthly_desc)
                    product.productId.contains("weekly") -> stringResource(R.string.subscription_plan_weekly_desc)
                    product.productId.contains("lifetime") -> stringResource(R.string.subscription_plan_lifetime_desc)
                    else -> product.description
                }
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.productId.contains("yearly")) CashaPrimaryLight else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = if (product.productId.contains("yearly")) FontWeight.Medium else FontWeight.Normal
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val price = product.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    ?: product.oneTimePurchaseOfferDetails?.formattedPrice
                    ?: ""
                
                Text(
                    text = price,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )
                
                val suffix = when {
                    product.productId.contains("yearly") -> stringResource(R.string.subscription_label_per_year)
                    product.productId.contains("monthly") -> stringResource(R.string.subscription_label_per_month)
                    product.productId.contains("weekly") -> stringResource(R.string.subscription_label_per_week)
                    else -> stringResource(R.string.subscription_label_one_time)
                }
                
                Text(
                    text = suffix,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        if (isBestValue) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-12).dp, y = (-12).dp),
                color = CashaPrimaryLight,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = stringResource(R.string.subscription_label_best_value).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// Extension to help with opacity since Compose Color doesn't have it directly like SwiftUI
private fun Color.opacity(alpha: Float): Color = this.copy(alpha = alpha)
