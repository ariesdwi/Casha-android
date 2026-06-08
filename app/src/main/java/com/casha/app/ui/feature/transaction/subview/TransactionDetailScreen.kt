package com.casha.app.ui.feature.transaction.subview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.R
import com.casha.app.domain.model.CashflowType
import com.casha.app.domain.model.TransactionCasha
import com.casha.app.ui.feature.transaction.TransactionViewModel
import com.casha.app.ui.feature.transaction.CashflowUiUtils

/**
 * Enum to track the type of operation being performed
 */
private enum class OperationType {
    DELETING,
    EDITING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    cashflowType: CashflowType,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Find the specific transaction based on type
    // Also check remoteId to handle locally-added transactions whose Room id is a local UUID
    // but whose cashflow history id matches the server-assigned remoteId.
    val transaction = if (cashflowType == CashflowType.INCOME) {
        uiState.rawIncomes.find { it.id == transactionId || it.remoteId == transactionId }?.let { 
            with(CashflowUiUtils) { it.toTransaction() }
        }
    } else {
        uiState.rawTransactions.find { it.id == transactionId || it.remoteId == transactionId }
    }

    // Cache the most recent valid transaction state so the UI doesn't blank out 
    // instantly when it's deleted from the backing view model or list.
    val activeTransaction = remember(transaction) { transaction } ?: remember { transaction }

    // Simplified dialog state management
    var showingDeleteAlert by remember { mutableStateOf(false) }
    var showingSyncAlert by remember { mutableStateOf(false) }
    var showingEditSheet by remember { mutableStateOf(false) }
    
    // Single operation state instead of separate flags
    var operationType by remember { mutableStateOf<OperationType?>(null) }
    
    // Track if we're in an operation
    val isInOperation = operationType != null && uiState.isLoading
    
    // Task 12.2: Error handling state
    var showDeleteError by remember { mutableStateOf(false) }
    var deleteErrorMessage by remember { mutableStateOf("") }



    if (activeTransaction == null) {
        // Fallback or full loading state
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Transaction not found")
                Button(onClick = onNavigateBack, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (cashflowType == CashflowType.INCOME) "Income Detail" else stringResource(R.string.transactions_detail_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Use UnifiedTransactionDetailCard instead of multiple card sections
                UnifiedTransactionDetailCard(
                    transaction = activeTransaction,
                    cashflowType = cashflowType,
                    onEdit = {
                        if (activeTransaction.isSynced) {
                            showingEditSheet = true
                        } else {
                            showingSyncAlert = true
                        }
                    },
                    onDelete = {
                        if (activeTransaction.isSynced) {
                            showingDeleteAlert = true
                        } else {
                            showingSyncAlert = true
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    // Processing Dialog - Shows during delete or edit operations
    if (isInOperation) {
        Dialog(
            onDismissRequest = { /* No cancel */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp), 
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (operationType) {
                            OperationType.DELETING -> "Deleting..."
                            OperationType.EDITING -> "Saving..."
                            null -> ""
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showingDeleteAlert) {
        AlertDialog(
            onDismissRequest = { showingDeleteAlert = false },
            title = { Text(stringResource(R.string.transactions_detail_delete_confirm_title)) },
            text = { 
                // Task 12.1: Display transaction name and amount
                Text(
                    if (activeTransaction != null) {
                        "Are you sure you want to delete \"${activeTransaction.name}\" (${com.casha.app.core.util.CurrencyFormatter.format(activeTransaction.amount)})?"
                    } else {
                        stringResource(R.string.transactions_detail_delete_confirm_message)
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showingDeleteAlert = false
                        operationType = OperationType.DELETING
                        // Task 12.2: Delete with error handling
                        try {
                            if (cashflowType == CashflowType.INCOME) {
                                viewModel.deleteIncome(activeTransaction.id) {
                                    onNavigateBack()
                                }
                            } else {
                                viewModel.deleteTransaction(activeTransaction.id) {
                                    onNavigateBack()
                                }
                            }
                        } catch (e: Exception) {
                            // Task 12.2: Show error if delete fails
                            operationType = null
                            deleteErrorMessage = e.message ?: "Failed to delete transaction"
                            showDeleteError = true
                        }
                    }
                ) {
                    Text(
                        stringResource(R.string.transactions_detail_delete_confirm_button), 
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showingDeleteAlert = false }) {
                    Text(stringResource(R.string.transactions_detail_cancel))
                }
            }
        )
    }

    // Sync Required Alert Dialog
    if (showingSyncAlert) {
        AlertDialog(
            onDismissRequest = { showingSyncAlert = false },
            title = { Text(stringResource(R.string.transactions_detail_sync_required_title)) },
            text = { 
                Text(
                    stringResource(R.string.transactions_detail_sync_required_message)
                )
            },
            confirmButton = {
                TextButton(onClick = { showingSyncAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
    // Edit Bottom Sheet
    if (showingEditSheet) {
        EditTransactionBottomSheet(
            transaction = activeTransaction,
            cashflowType = cashflowType,
            categories = uiState.categories,
            onDismissRequest = { showingEditSheet = false },
            onSave = { request ->
                operationType = OperationType.EDITING
                if (cashflowType == CashflowType.INCOME) {
                    // Convert TransactionRequest to CreateIncomeRequest
                    val incomeRequest = com.casha.app.domain.model.CreateIncomeRequest(
                        name = request.name,
                        amount = request.amount,
                        datetime = request.datetime,
                        type = try { 
                            com.casha.app.domain.model.IncomeType.valueOf(request.category.uppercase()) 
                        } catch(e: Exception) { 
                            com.casha.app.domain.model.IncomeType.OTHER 
                        },
                        note = request.note
                    )
                    viewModel.updateIncome(activeTransaction.id, incomeRequest)
                } else {
                    viewModel.updateTransaction(activeTransaction.id, request)
                }
                showingEditSheet = false
            }
        )
    }

    // Reset operation type when loading completes
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && operationType != null) {
            operationType = null
        }
    }
    
    // Task 12.2: Delete Error Dialog
    if (showDeleteError) {
        AlertDialog(
            onDismissRequest = { showDeleteError = false },
            title = { 
                Text("Delete Failed")
            },
            text = { 
                Text(
                    "Unable to delete the transaction. ${deleteErrorMessage}\n\nThe transaction has been retained. Please try again."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteError = false }
                ) {
                    Text("OK")
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}

