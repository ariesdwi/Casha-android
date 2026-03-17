package com.casha.app.ui.feature.transaction.subview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.R
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.core.util.DateHelper
import com.casha.app.domain.model.CashflowType
import com.casha.app.domain.model.TransactionCasha
import com.casha.app.ui.feature.transaction.TransactionViewModel
import com.casha.app.ui.feature.transaction.CashflowUiUtils
import com.casha.app.ui.theme.*

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
    val transaction = if (cashflowType == CashflowType.INCOME) {
        uiState.rawIncomes.find { it.id == transactionId }?.let { 
            with(CashflowUiUtils) { it.toTransaction() }
        }
    } else {
        uiState.rawTransactions.find { it.id == transactionId }
    }

    // Cache the most recent valid transaction state so the UI doesn't blank out 
    // instantly when it's deleted from the backing view model or list.
    val activeTransaction = remember(transaction) { transaction } ?: remember { transaction }

    var showingDeleteAlert by remember { mutableStateOf(false) }
    var showingSyncAlert by remember { mutableStateOf(false) }
    var showingTenorSheet by remember { mutableStateOf(false) }
    var showingEditSheet by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    // Observe when deleting completes to gracefully pop navigation
    LaunchedEffect(uiState.isLoading, isDeleting) {
        if (isDeleting && !uiState.isLoading) {
            onNavigateBack()
        }
    }

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
                },
                actions = {
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.transactions_detail_edit)) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                if (activeTransaction.isSynced) {
                                    showingEditSheet = true
                                } else {
                                    showingSyncAlert = true
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.transactions_detail_delete_confirm_button), color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                if (activeTransaction.isSynced) {
                                    showingDeleteAlert = true
                                } else {
                                    showingSyncAlert = true
                                }
                            }
                        )
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
                HeaderSection(activeTransaction, cashflowType)
                AmountStatusSection(activeTransaction, cashflowType)
                CategorySection(activeTransaction)
                DetailsSection(activeTransaction, onConvertInstallmentClick = { showingTenorSheet = true })
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    // Professional Processing Dialog Modal
    if (uiState.isLoading && (isDeleting || isEditing)) {
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
                    CircularProgressIndicator(modifier = Modifier.size(48.dp), color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = if (isDeleting) "Deleting..." else "Saving...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (showingDeleteAlert) {
        AlertDialog(
            onDismissRequest = { showingDeleteAlert = false },
            title = { Text(stringResource(R.string.transactions_detail_delete_confirm_title)) },
            text = { Text(stringResource(R.string.transactions_detail_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showingDeleteAlert = false
                        isDeleting = true
                        if (cashflowType == CashflowType.INCOME) {
                            viewModel.deleteIncome(activeTransaction.id)
                        } else {
                            viewModel.deleteTransaction(activeTransaction.id)
                        }
                    }
                ) {
                    Text(stringResource(R.string.transactions_detail_delete_confirm_button), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showingDeleteAlert = false }) {
                    Text(stringResource(R.string.transactions_detail_cancel))
                }
            }
        )
    }

    if (showingSyncAlert) {
        AlertDialog(
            onDismissRequest = { showingSyncAlert = false },
            title = { Text(stringResource(R.string.transactions_detail_sync_required_title)) },
            text = { Text(stringResource(R.string.transactions_detail_sync_required_message)) },
            confirmButton = {
                TextButton(onClick = { showingSyncAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    if (showingEditSheet) {
        EditTransactionBottomSheet(
            transaction = activeTransaction,
            cashflowType = cashflowType,
            onDismissRequest = { showingEditSheet = false },
            onSave = { request ->
                isEditing = true
                if (cashflowType == CashflowType.INCOME) {
                    // Convert TransactionRequest to CreateIncomeRequest
                    val incomeRequest = com.casha.app.domain.model.CreateIncomeRequest(
                        name = request.name,
                        amount = request.amount,
                        datetime = request.datetime,
                        type = try { com.casha.app.domain.model.IncomeType.valueOf(request.category.uppercase()) } catch(e: Exception) { com.casha.app.domain.model.IncomeType.OTHER },
                        note = request.note
                    )
                    viewModel.updateIncome(activeTransaction.id, incomeRequest)
                } else {
                    viewModel.updateTransaction(activeTransaction.id, request)
                }
                showingEditSheet = false
                
                // Reset isEditing smoothly after successful save block finishes
                // This simulates finishing to unlock the UI.
                // It relies on LaunchedEffect or the viewModel terminating isloading properly.
                // We'll manage resetting isEditing outside or leave it since it hides when !uiState.isLoading
            }
        )
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && isEditing) {
            isEditing = false
        }
    }
    
    // Convert Installment sheet would go here
}

@Composable
private fun HeaderSection(transaction: TransactionCasha, type: CashflowType) {
    val tempEntry = with(CashflowUiUtils) { transaction.toCashflowEntry() }
    val icon = CashflowUiUtils.iconForEntry(tempEntry)
    val color = if (type == CashflowType.INCOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val onColor = if (type == CashflowType.INCOME) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError
    val typeName = if (type == CashflowType.INCOME) "INCOME" else "EXPENSE"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(50.dp)
            )
        }

        Text(
            text = transaction.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = typeName,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = onColor,
            modifier = Modifier
                .background(color, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun AmountStatusSection(transaction: TransactionCasha, type: CashflowType) {
    val color = if (type == CashflowType.INCOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val titleText = if (type == CashflowType.INCOME) stringResource(R.string.transactions_detail_amount_received) else stringResource(R.string.transactions_detail_amount_spent)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = CurrencyFormatter.format(transaction.amount, CurrencyFormatter.defaultCurrency).replace("-",""), // Remove negative sign for display
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun CategorySection(transaction: TransactionCasha) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.transactions_detail_category),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Label, // Tag-like icon
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp).size(20.dp)
            )
            Text(
                text = transaction.category.ifEmpty { "Uncategorized" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DetailsSection(transaction: TransactionCasha, onConvertInstallmentClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = if (transaction.category.isEmpty() && transaction.amount > 0) "Income Details" else stringResource(R.string.transactions_detail_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        DetailRow(title = stringResource(R.string.transactions_detail_date), value = DateHelper.formatDisplayWithTime(transaction.datetime))
        DetailRow(title = stringResource(R.string.transactions_detail_updated), value = DateHelper.formatDisplayWithTime(transaction.updatedAt))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = stringResource(R.string.transactions_detail_sync_status),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(100.dp)
            )
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (transaction.isSynced) stringResource(R.string.transactions_detail_status_synced) else stringResource(R.string.transactions_detail_status_pending),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (transaction.isSynced) MaterialTheme.colorScheme.primary else Color(0xFFFF9800),
                    textAlign = TextAlign.End
                )
                if (transaction.isSynced) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp).size(16.dp)
                    )
                }
            }
        }

        if (transaction.liabilityId != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onConvertInstallmentClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.transactions_detail_convert_installment))
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}
