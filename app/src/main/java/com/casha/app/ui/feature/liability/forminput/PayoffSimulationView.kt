package com.casha.app.ui.feature.liability.forminput

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8F9FA) // Casha Background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                TopAppBar(
                    title = { Text("Simulasi Pelunasan", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Strategy Picker
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Strategi Pembayaran",
                            fontSize = 14.sp,
                            color = Color(0xFF666666) // Casha Text Secondary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SimulationStrategy.entries.forEach { strategy ->
                                SelectableChip(
                                    text = strategy.displayName,
                                    selected = selectedStrategy == strategy,
                                    onClick = { selectedStrategy = strategy },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Additional Payment Input
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Budget Tambahan per Bulan",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = CurrencyFormatter.symbol(userCurrency),
                                fontWeight = FontWeight.Bold,
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
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                decorationBox = { innerTextField ->
                                    if (additionalPayment.isEmpty() && !isAdditionalPaymentFocused) {
                                        Text("0", color = Color.Gray.copy(alpha = 0.5f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                            .height(56.dp),
                        enabled = additionalPaymentValue > 0 && !liabilityState.isLoading,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("Hitung Simulasi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Results
                    if (liabilityState.isLoading) {
                        Box(modifier = Modifier.padding(vertical = 32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Text("Menghitung simulasi...", color = Color(0xFF666666))
                            }
                        }
                    } else if (liabilityState.simulationResult != null) {
                        ResultSection(
                            result = liabilityState.simulationResult,
                            userCurrency = userCurrency
                        )
                    } else if (hasSimulated) {
                        Text(
                            text = "Tidak ada data simulasi",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultSection(result: SimulatePayoffResponse, userCurrency: String) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.height(1.dp).weight(1f).background(Color(0xFF666666)))
            Text("Hasil", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF666666))
            Box(modifier = Modifier.height(1.dp).weight(1f).background(Color(0xFF666666)))
        }

        // Top Summary Cards
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                icon = Icons.Default.CalendarToday,
                iconColor = Color.Blue,
                title = "Estimasi Lunas",
                mainValue = formatPayoffDate(result.estimatedPayoffDate),
                subtitle = "(${result.totalMonthsToPayOff} bulan dari sekarang)"
            )

            SummaryCard(
                icon = Icons.Default.AttachMoney,
                iconColor = Color.Green,
                title = "Bunga Dihemat",
                mainValue = CurrencyFormatter.format(result.interestSaved, userCurrency),
                subtitle = null
            )

            SummaryCard(
                icon = Icons.Default.Timer,
                iconColor = Color(0xFFFFA500), // Orange
                title = "Waktu Dihemat",
                mainValue = "${result.monthsSaved} bulan lebih cepat",
                subtitle = null
            )
        }

        // Recommendation
        if (result.recommendation.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFA500).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFFA500), modifier = Modifier.size(16.dp))
                    Text("Rekomendasi", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA500))
                }
                Text(
                    text = result.recommendation,
                    fontSize = 12.sp,
                    color = Color(0xFF666666) // Casha Text Secondary
                )
            }
        }

        // Per Liability Breakdown
        if (result.liabilityBreakdown.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Per Liability:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

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
    subtitle: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 12.sp, color = Color(0xFF666666))
            Text(mainValue, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(subtitle, fontSize = 10.sp, color = Color(0xFF666666))
            }
        }
    }
}

@Composable
private fun BreakdownRow(index: Int, item: LiabilityBreakdown, userCurrency: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$index.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(24.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "→ ${item.monthsToPayOff} bln",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                "Bayar: ${CurrencyFormatter.format(item.monthlyPayment, userCurrency)}/bln",
                fontSize = 12.sp,
                color = Color(0xFF666666) // Casha Text Secondary
            )
        }
    }
}

private fun formatPayoffDate(dateString: String): String {
    try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = formatter.parse(dateString)
        if (date != null) {
            val outFormatter = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
            return outFormatter.format(date)
        }
    } catch (e: Exception) {
        // Fallback to raw string
    }
    return dateString
}
