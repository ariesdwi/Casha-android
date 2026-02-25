package com.casha.app.ui.feature.liability.subviews.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.InstallmentPlan

@Composable
fun ActiveInstallmentsListView(
    installments: List<InstallmentPlan>,
    userCurrency: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Cicilan Aktif",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        val activeInstallments = installments.filter { it.isActive }

        if (activeInstallments.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search, // Approximation for doc.text.magnifyingglass
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF9E9E9E) // Casha Text Secondary
                )
                Text(
                    text = "Belum ada cicilan aktif",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface // Casha Text Primary
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                activeInstallments.forEach { plan ->
                    InstallmentRow(plan = plan, userCurrency = userCurrency)
                }
            }
        }
    }
}

@Composable
private fun InstallmentRow(plan: InstallmentPlan, userCurrency: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f),
                ambientColor = Color.Transparent
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // Casha Card
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = plan.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface // Casha Text Primary
                    )
                    
                    Text(
                        text = "${CurrencyFormatter.format(plan.monthlyAmount, userCurrency)}/bln",
                        fontSize = 14.sp,
                        color = Color(0xFF666666) // Casha Text Secondary
                    )
                }
                
                Text(
                    text = "${plan.currentMonth} dari ${plan.tenor} Bln",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface // Casha Text Primary
                )
            }
            
            // Progress bar
            val progress = (plan.currentMonth.toDouble() / maxOf(plan.tenor, 1).toDouble()).toFloat().coerceIn(0f, 1f)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary) // Casha Primary
                )
            }
        }
    }
}
