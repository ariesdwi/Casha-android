package com.casha.app.ui.feature.portfolio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.Asset
import com.casha.app.domain.model.UpdateAssetRequest
import com.casha.app.ui.feature.portfolio.subviews.detail.*
import com.casha.app.ui.theme.CashaPrimaryLight
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailScreen(
    assetId: String,
    initialAsset: Asset,
    viewModel: PortfolioViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Current asset from state or fallback
    val asset = uiState.assets.firstOrNull { it.id == assetId } ?: initialAsset
    
    // Screen states
    var showEditModal by remember { mutableStateOf(false) }
    var showingDeleteConfirmation by remember { mutableStateOf(false) }
    
    val userCurrency = CurrencyFormatter.defaultCurrency

    // Load transactions on launch
    LaunchedEffect(assetId) {
        viewModel.fetchAssetTransactions(assetId)
    }

    if (showEditModal) {
        EditAssetScreen(
            asset = asset,
            viewModel = viewModel,
            onNavigateBack = { showEditModal = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = asset.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditModal = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Asset Balance Card
            AssetBalanceCardView(asset = asset, userCurrency = userCurrency)

            AssetQuickActionsView(
                asset = asset,
                onBuyTapped = { /* TODO: Show AddAssetTransactionView (Saving) */ },
                onSellTapped = { /* TODO: Show AddAssetTransactionView (Withdraw) */ }
            )
            
            AssetInfoDetailsView(asset = asset)
            
            AssetTransactionHistoryView(
                asset = asset,
                transactions = uiState.assetTransactions
            )

            // Delete Button
            Button(
                onClick = { showingDeleteConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.error
                ),
                elevation = null
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Hapus Aset", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showingDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showingDeleteConfirmation = false },
            title = { Text("Hapus Aset") },
            text = { Text("Apakah Anda yakin ingin menghapus ${asset.name}? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAsset(asset.id) {
                            onNavigateBack()
                        }
                        showingDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showingDeleteConfirmation = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
