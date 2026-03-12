package com.casha.app.ui.feature.liability.forminput
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.R
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.SimulationStrategy
import com.casha.app.domain.model.SimulatePayoffResponse
import com.casha.app.domain.model.LiabilityBreakdown
import com.casha.app.ui.feature.liability.LiabilityState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayoffSimulationView(
    liabilityState: LiabilityState,
    userCurrency: String,
    onDismissRequest: () -> Unit,
    onSubmit: (strategy: SimulationStrategy, additionalPayment: Double) -> Unit
) {
    var selectedStrategy by remember { mutableStateOf(SimulationStrategy.AVALANCHE) }
    var additionalPayment by remember { mutableStateOf("") }
    var additionalPaymentValue by remember { mutableStateOf(0.0) }
    var hasSimulated by remember { mutableStateOf(false) }
    var isAdditionalPaymentFocused by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
modifier = Modifier.fillMaxSize(),
onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            // ── Header ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.liabilities_simulation_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.liabilities_simulation_subtitle),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Strategy Picker
                InputCard(title = stringResource(R.string.liabilities_simulation_strategy)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        SimulationStrategy.entries.forEach { strategy ->
                            val isSelected = selectedStrategy == strategy
                            Surface(
                                onClick = { selectedStrategy = strategy },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(strategy.displayNameRes),
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Additional Payment Input
                InputCard(title = stringResource(R.string.liabilities_simulation_additional_budget)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = CurrencyFormatter.symbol(userCurrency),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.compose.foundation.text.BasicTextField(
                            value = if (isAdditionalPaymentFocused) additionalPayment else if (additionalPayment.isNotEmpty()) CurrencyFormatter.formatInput(additionalPayment) else "",
                            onValueChange = {
                                additionalPayment = it.filter { char -> char.isDigit() }
                                additionalPaymentValue = additionalPayment.toDoubleOrNull() ?: 0.0
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isAdditionalPaymentFocused = it.isFocused },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            decorationBox = { innerTextField ->
                                if (additionalPayment.isEmpty() && !isAdditionalPaymentFocused) {
                                    Text("0", color = Color.Gray.copy(alpha = 0.3f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                // Simulate Button
                Button(
                    onClick = {
                        hasSimulated = true
                        onSubmit(selectedStrategy, additionalPaymentValue)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = additionalPaymentValue >= 0 && !liabilityState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    if (liabilityState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text(stringResource(R.string.liabilities_simulation_calculate), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Results
                if (liabilityState.simulationResult != null && !liabilityState.isLoading) {
                    ResultSection(
                        result = liabilityState.simulationResult!!,
                        userCurrency = userCurrency
                    )
                } else if (hasSimulated && !liabilityState.isLoading && liabilityState.simulationResult == null) {
                    Text(
                        text = stringResource(R.string.liabilities_simulation_no_data),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ResultSection(result: SimulatePayoffResponse, userCurrency: String) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Box(modifier = Modifier.height(1.dp).weight(1f).background(Color.Gray.copy(alpha = 0.2f)))
            Text(stringResource(R.string.liabilities_simulation_analysis_result), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Box(modifier = Modifier.height(1.dp).weight(1f).background(Color.Gray.copy(alpha = 0.2f)))
        }

        // Top Summary Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                icon = Icons.Default.CalendarToday,
                iconColor = Color(0xFF3F51B5),
                title = stringResource(R.string.liabilities_simulation_estimated_payoff),
                mainValue = formatPayoffDate(result.estimatedPayoffDate),
                subtitle = "${result.totalMonthsToPayOff} ${stringResource(R.string.liabilities_tenure_months)}",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                icon = Icons.Default.Timer,
                iconColor = Color(0xFFFFA500),
                title = stringResource(R.string.liabilities_simulation_time_saved),
                mainValue = "${result.monthsSaved} ${stringResource(R.string.liabilities_tenure_months)}",
                subtitle = stringResource(R.string.liabilities_simulation_faster),
                modifier = Modifier.weight(1f)
            )
        }

        SummaryCard(
            icon = Icons.Default.Savings,
            iconColor = Color(0xFF009033),
            title = stringResource(R.string.liabilities_simulation_interest_saved),
            mainValue = CurrencyFormatter.format(result.interestSaved, userCurrency),
            subtitle = stringResource(R.string.liabilities_simulation_potential_savings),
            modifier = Modifier.fillMaxWidth()
        )

        // Recommendation
        if (result.recommendation.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(
                        text = result.recommendation,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Per Liability Breakdown
        if (result.liabilityBreakdown.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.liabilities_simulation_payoff_order), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                result.liabilityBreakdown.forEachIndexed { index, item ->
                    BreakdownRow(index = index + 1, item = item, userCurrency = userCurrency)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    mainValue: String,
    subtitle: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(mainValue, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(index: Int, item: LiabilityBreakdown, userCurrency: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    stringResource(R.string.liabilities_simulation_pay_per_month, CurrencyFormatter.format(item.monthlyPayment, userCurrency)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${item.monthsToPayOff} ${stringResource(R.string.liabilities_tenure_months)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(stringResource(R.string.liabilities_simulation_paid_off), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun formatPayoffDate(dateString: String): String {
    try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = formatter.parse(dateString)
        if (date != null) {
            val outFormatter = SimpleDateFormat("MMM yyyy", Locale("id", "ID"))
            return outFormatter.format(date)
        }
    } catch (e: Exception) {
        // Fallback
    }
    return dateString
}
