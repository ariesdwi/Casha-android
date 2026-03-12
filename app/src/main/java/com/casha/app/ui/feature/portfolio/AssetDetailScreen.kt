package com.casha.app.ui.feature.portfolio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.casha.app.R
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.Asset
import com.casha.app.domain.model.AssetTransactionType
import com.casha.app.domain.model.UpdateAssetRequest
import com.casha.app.ui.feature.portfolio.subviews.detail.*
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
    val asset = uiState.assets.firstOrNull { it.id == assetId } ?: initialAsset
    
    var isEditMode by remember { mutableStateOf(false) }
    var showingDeleteConfirmation by remember { mutableStateOf(false) }
    var showingAddTransaction by remember { mutableStateOf(false) }
    var selectedTransactionType by remember { mutableStateOf(AssetTransactionType.SAVING) }
    
    // Edit fields
    var editName by remember { mutableStateOf(asset.name) }
    var editAmount by remember { mutableStateOf(String.format("%.0f", asset.amount)) }
    var editQuantity by remember { mutableStateOf(asset.quantity?.let { String.format("%.2f", it) } ?: "") }
    var editPricePerUnit by remember { mutableStateOf(asset.pricePerUnit?.let { String.format("%.0f", it) } ?: "") }
    var editDescription by remember { mutableStateOf(asset.description ?: "") }
    var editAcquisitionDate by remember { mutableStateOf(asset.acquisitionDate ?: Date()) }
    var showAcquisitionDate by remember { mutableStateOf(asset.acquisitionDate != null) }
    var editLocation by remember { mutableStateOf(asset.location ?: "") }
    // Gold purity
    var editPurity by remember { mutableStateOf<Int?>(asset.purity) }
    
    // Reset function
    val resetEditFields = {
        editName = asset.name
        editAmount = String.format("%.0f", asset.amount)
        editQuantity = asset.quantity?.let { String.format("%.2f", it) } ?: ""
        editPricePerUnit = asset.pricePerUnit?.let { String.format("%.0f", it) } ?: ""
        editDescription = asset.description ?: ""
        editAcquisitionDate = asset.acquisitionDate ?: Date()
        showAcquisitionDate = asset.acquisitionDate != null
        editLocation = asset.location ?: ""
        editPurity = asset.purity
    }
    
    // Update fields when asset changes
    LaunchedEffect(asset) {
        if (!isEditMode) resetEditFields()
    }
    
    val userCurrency = CurrencyFormatter.defaultCurrency

    LaunchedEffect(assetId) {
        viewModel.fetchAssetTransactions(assetId)
    }

    if (showingAddTransaction) {
        AddAssetTransactionScreen(
            asset = asset,
            transactionType = selectedTransactionType,
            viewModel = viewModel,
            onNavigateBack = { showingAddTransaction = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isEditMode) stringResource(R.string.portfolio_action_edit) else asset.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (isEditMode) {
                        TextButton(onClick = {
                            resetEditFields()
                            isEditMode = false
                        }) {
                            Text(stringResource(R.string.portfolio_action_cancel), color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (isEditMode) {
                        TextButton(
                            onClick = {
                                val request = if (asset.type.isQuantityBased) {
                                    UpdateAssetRequest(
                                        name = editName,
                                        amount = null,
                                        quantity = editQuantity.toDoubleOrNull() ?: 0.0,
                                        unit = asset.unit,
                                        pricePerUnit = editPricePerUnit.toDoubleOrNull() ?: 0.0,
                                        acquisitionDate = if (showAcquisitionDate) editAcquisitionDate else null,
                                        location = editLocation.ifBlank { null },
                                        description = editDescription.ifBlank { null },
                                        purity = editPurity
                                    )
                                } else {
                                    val amt = editAmount.toDoubleOrNull() ?: 0.0
                                    UpdateAssetRequest(
                                        name = editName,
                                        amount = amt,
                                        quantity = 1.0,
                                        unit = null,
                                        pricePerUnit = amt,
                                        acquisitionDate = if (showAcquisitionDate) editAcquisitionDate else null,
                                        location = editLocation.ifBlank { null },
                                        description = editDescription.ifBlank { null }
                                    )
                                }
                                viewModel.updateAsset(asset.id, request) {
                                    isEditMode = false
                                }
                            },
                            enabled = !uiState.isLoading
                        ) {
                            Text(stringResource(R.string.portfolio_action_save), color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        TextButton(onClick = { isEditMode = true }) {
                            Text(stringResource(R.string.portfolio_action_edit), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                windowInsets = WindowInsets(0.dp),
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Asset Balance Card - full bleed with horizontal padding
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                AssetBalanceCardView(asset = asset, userCurrency = userCurrency)
            }

            if (isEditMode) {
                AssetEditSectionView(
                    asset = asset,
                    editName = editName, onNameChange = { editName = it },
                    editAmount = editAmount, onAmountChange = { editAmount = it },
                    editQuantity = editQuantity, onQuantityChange = { editQuantity = it },
                    editPricePerUnit = editPricePerUnit, onPricePerUnitChange = { editPricePerUnit = it },
                    editDescription = editDescription, onDescriptionChange = { editDescription = it },
                    editAcquisitionDate = editAcquisitionDate, onAcquisitionDateChange = { editAcquisitionDate = it },
                    showAcquisitionDate = showAcquisitionDate, onShowAcquisitionDateChange = { showAcquisitionDate = it },
                    editLocation = editLocation, onLocationChange = { editLocation = it },
                    editPurity = editPurity, onPurityChange = { editPurity = it },
                    userCurrency = userCurrency
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            } else {
                AssetQuickActionsView(
                    asset = asset,
                    onBuyTapped = {
                        selectedTransactionType = AssetTransactionType.SAVING
                        showingAddTransaction = true
                    },
                    onSellTapped = {
                        selectedTransactionType = AssetTransactionType.WITHDRAW
                        showingAddTransaction = true
                    }
                )
                
                AssetInfoDetailsView(asset = asset)
                
                AssetTransactionHistoryView(
                    asset = asset,
                    transactions = uiState.assetTransactions
                )

                // Delete Button - styled as a warning card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedButton(
                        onClick = { showingDeleteConfirmation = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(stringResource(R.string.portfolio_action_delete), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }


    if (showingDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showingDeleteConfirmation = false },
            title = { Text(stringResource(R.string.portfolio_action_delete)) },
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
                    Text(stringResource(R.string.portfolio_action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showingDeleteConfirmation = false }) {
                    Text(stringResource(R.string.portfolio_action_cancel))
                }
            }
        )
    }
}
