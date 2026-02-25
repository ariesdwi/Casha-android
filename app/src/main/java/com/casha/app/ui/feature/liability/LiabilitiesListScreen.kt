package com.casha.app.ui.feature.liability

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casha.app.domain.model.Liability

@Composable
fun LiabilitiesListScreen(
    viewModel: LiabilityViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchAllLiabilities()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Custom Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(elevation = 2.dp, shape = CircleShape)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Liabilitas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    IconButton(
                        onClick = onNavigateToCreate,
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(elevation = 2.dp, shape = CircleShape)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Liability",
                            tint = Color(0xFF009033),
                            modifier = Modifier.size(45.dp)
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when {
                        uiState.isLoading && uiState.activeLiabilities.isEmpty() && uiState.paidOffLiabilities.isEmpty() -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        uiState.activeLiabilities.isEmpty() && uiState.paidOffLiabilities.isEmpty() -> {
                            EmptyStateView(onCreateClick = onNavigateToCreate)
                        }
                        else -> {
                            LiabilitiesList(
                                uiState = uiState,
                                onLiabilityClick = onNavigateToDetail,
                                onSummaryClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiabilitiesList(
    uiState: LiabilityState,
    onLiabilityClick: (String) -> Unit,
    onSummaryClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary card
        item {
            LiabilitySummaryCard(
                totalBalance = uiState.totalBalance,
                totalMonthlyInstallment = uiState.totalMonthlyInstallment,
                activeCount = uiState.activeCount,
                paidOffCount = uiState.paidOffCount,
                isLoading = uiState.isLoading,
                onTap = onSummaryClick
            )
        }

        // Overdue section
        if (uiState.overdueLiabilities.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Default.Error,
                    title = "Menunggak",
                    iconTint = Color(0xFFE53935)
                )
            }

            items(uiState.overdueLiabilities, key = { "overdue-${it.id}" }) { liability ->
                LiabilityRow(
                    liability = liability,
                    onClick = { onLiabilityClick(liability.id) }
                )
            }
        }

        // Active (non-overdue) section
        if (uiState.nonOverdueLiabilities.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Default.CreditCard,
                    title = "Aktif",
                    iconTint = Color(0xFF009033)
                )
            }

            items(uiState.nonOverdueLiabilities, key = { "active-${it.id}" }) { liability ->
                LiabilityRow(
                    liability = liability,
                    onClick = { onLiabilityClick(liability.id) }
                )
            }
        }

        // Paid-off section
        if (uiState.paidOffLiabilities.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Default.CheckCircle,
                    title = "Lunas",
                    iconTint = Color(0xFF4CAF50)
                )
            }

            items(uiState.paidOffLiabilities, key = { "paidoff-${it.id}" }) { liability ->
                LiabilityRow(
                    liability = liability,
                    onClick = { onLiabilityClick(liability.id) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    iconTint: Color
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconTint
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun EmptyStateView(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CreditCard,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "Belum Ada Liabilitas",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tambahkan kartu kredit, pinjaman, atau KPR untuk mulai melacak utang Anda.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onCreateClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009033)),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(text = "Tambah Liabilitas", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
