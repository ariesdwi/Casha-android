package com.casha.app.ui.feature.transaction.subview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.core.util.DateHelper
import com.casha.app.domain.model.CashflowEntry
import com.casha.app.domain.model.CashflowType
import com.casha.app.ui.feature.transaction.CashflowUiUtils
import com.casha.app.ui.feature.transaction.TransactionViewModel
import com.casha.app.ui.theme.CashaPurple
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Collect all items from all sections that share this groupId
    val allGroupItems = remember(uiState.cashflowSections, groupId) {
        uiState.cashflowSections
            .flatMap { it.items }
            .filter { it.groupId == groupId }
    }

    // Mutable local state for optimistic UI updates
    var currentGroupItems by remember(allGroupItems) { mutableStateOf(allGroupItems) }
    val groupName = currentGroupItems.firstOrNull()?.groupName ?: "Multi Expense"
    val totalAmount = currentGroupItems.sumOf { it.amount }

    var showingDeleteGroupAlert by remember { mutableStateOf(false) }
    var showingDeleteItemAlert by remember { mutableStateOf<String?>(null) }
    var showingEditGroupNameSheet by remember { mutableStateOf(false) }
    var showingEditItemSheet by remember { mutableStateOf<CashflowEntry?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Auto-dismiss when all items are deleted
    LaunchedEffect(currentGroupItems) {
        if (currentGroupItems.isEmpty() && allGroupItems.isNotEmpty()) {
            onNavigateBack()
        }
    }

    // Observe loading state for operations
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && isProcessing) {
            isProcessing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Group Detail",
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
                            text = { Text("Rename Group") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                showingEditGroupNameSheet = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Group", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                showingDeleteGroupAlert = true
                            }
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (currentGroupItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No items in this group")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) { Text("Go Back") }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header card
                item {
                    GroupHeaderCard(
                        groupName = groupName,
                        itemCount = currentGroupItems.size,
                        totalAmount = totalAmount
                    )
                }

                // Per-item list
                itemsIndexed(
                    items = currentGroupItems,
                    key = { _, item -> item.id }
                ) { _, item ->
                    AnimatedVisibility(
                        visible = true,
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        GroupItemCard(
                            entry = item,
                            onEdit = { showingEditItemSheet = item },
                            onDelete = { showingDeleteItemAlert = item.id }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Processing dialog
    if (uiState.isLoading && isProcessing) {
        Dialog(
            onDismissRequest = {},
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
                    CircularProgressIndicator(modifier = Modifier.size(48.dp), color = CashaPurple)
                    Text("Processing...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Delete entire group alert
    if (showingDeleteGroupAlert) {
        AlertDialog(
            onDismissRequest = { showingDeleteGroupAlert = false },
            title = { Text("Delete Group") },
            text = { Text("This will delete all ${currentGroupItems.size} items in this group. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showingDeleteGroupAlert = false
                        isProcessing = true
                        viewModel.deleteGroup(groupId) {
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showingDeleteGroupAlert = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete single item alert
    showingDeleteItemAlert?.let { itemId ->
        AlertDialog(
            onDismissRequest = { showingDeleteItemAlert = null },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete this item from the group?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showingDeleteItemAlert = null
                        // Optimistic: remove from local list immediately
                        currentGroupItems = currentGroupItems.filter { it.id != itemId }
                        isProcessing = true
                        viewModel.deleteTransaction(itemId)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showingDeleteItemAlert = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit group name sheet
    if (showingEditGroupNameSheet) {
        EditGroupNameSheet(
            currentName = groupName,
            onDismiss = { showingEditGroupNameSheet = false },
            onSave = { newName ->
                showingEditGroupNameSheet = false
                isProcessing = true
                viewModel.renameGroup(groupId, newName)
            }
        )
    }

    // Edit single item sheet
    showingEditItemSheet?.let { entry ->
        // Build a TransactionCasha from the CashflowEntry so we can reuse EditTransactionBottomSheet
        val transaction = remember(entry) {
            com.casha.app.domain.model.TransactionCasha(
                id = entry.id,
                name = entry.title,
                category = entry.category,
                amount = entry.amount,
                datetime = entry.date,
                isSynced = true,
                groupId = entry.groupId,
                groupName = entry.groupName
            )
        }
        EditTransactionBottomSheet(
            transaction = transaction,
            cashflowType = CashflowType.EXPENSE,
            categories = uiState.categories,
            onDismissRequest = { showingEditItemSheet = null },
            onSave = { request ->
                showingEditItemSheet = null
                // Optimistic: update local state directly
                currentGroupItems = currentGroupItems.map { item ->
                    if (item.id == entry.id) {
                        item.copy(
                            title = request.name,
                            amount = request.amount,
                            category = request.category
                        )
                    } else item
                }
                isProcessing = true
                viewModel.updateTransaction(entry.id, request)
            }
        )
    }
}

@Composable
private fun GroupHeaderCard(
    groupName: String,
    itemCount: Int,
    totalAmount: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Purple cart icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(CashaPurple.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = CashaPurple,
                modifier = Modifier.size(50.dp)
            )
        }

        Text(
            text = groupName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // MULTI EXPENSE badge
        Text(
            text = "MULTI EXPENSE",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier
                .background(CashaPurple, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Total amount
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CashaPurple.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Amount",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = CurrencyFormatter.format(totalAmount),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = CashaPurple
            )
            Text(
                text = "$itemCount items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GroupItemCard(
    entry: CashflowEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val tempEntry = entry // Already a CashflowEntry
    val icon = CashflowUiUtils.iconForEntry(tempEntry)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(CashaPurple.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CashaPurple,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + category
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.category.ifEmpty { "Other" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Text(
                text = CurrencyFormatter.format(entry.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = CashaPurple
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Action menu
            var menuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Actions",
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
