package com.casha.app.ui.feature.goaltracker

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalTrackerDetailScreen(
    goalId: String,
    viewModel: GoalTrackerViewModel,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val goal = uiState.selectedGoal
    
    var isEditMode by remember { mutableStateOf(false) }
    var showingAddContribution by remember { mutableStateOf(false) }
    var showingDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Edit fields
    var editName by remember { mutableStateOf("") }
    var editTargetAmount by remember { mutableStateOf("") }
    var editCategory by remember { mutableStateOf<GoalCategory?>(null) }
    var editDeadline by remember { mutableStateOf(Date().time) }
    var editUseDeadline by remember { mutableStateOf(false) }
    var editNote by remember { mutableStateOf("") }
    var editIcon by remember { mutableStateOf("🎯") }
    var editColorHex by remember { mutableStateOf("#4CAF50") }
    var editAssetId by remember { mutableStateOf<String?>(null) }

    var showingCategoryPicker by remember { mutableStateOf(false) }

    LaunchedEffect(goalId) {
        viewModel.fetchGoalDetails(goalId)
    }

    LaunchedEffect(goal) {
        goal?.let {
            editName = it.name
            editTargetAmount = it.targetAmount.toLong().toString()
            editCategory = it.category
            editDeadline = it.deadline?.time ?: (Date().time + 86400000L * 30)
            editUseDeadline = it.deadline != null
            editNote = it.note ?: ""
            editIcon = it.icon ?: "🎯"
            editColorHex = it.color ?: "#4CAF50"
            editAssetId = it.assetId
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (goal != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp)
                ) {
                    // Custom Header
                    DetailHeader(
                        title = if (isEditMode) "Edit Goal" else goal.name,
                        onBack = onNavigateBack,
                        onAction = {
                            if (isEditMode) {
                                // Save
                                editCategory?.let { cat ->
                                    viewModel.updateGoal(
                                        id = goalId,
                                        name = editName,
                                        targetAmount = editTargetAmount.toDoubleOrNull() ?: 0.0,
                                        category = cat,
                                        deadline = if (editUseDeadline) Date(editDeadline) else null,
                                        assetId = editAssetId,
                                        icon = editIcon,
                                        color = editColorHex,
                                        note = editNote
                                    )
                                    isEditMode = false
                                }
                            } else {
                                isEditMode = true
                            }
                        },
                        actionText = if (isEditMode) "Save" else "Edit",
                        isActionEnabled = !isEditMode || (editName.isNotBlank() && editTargetAmount.isNotBlank())
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Goal Balance Card
                        GoalBalanceCard(
                            goal = goal,
                            isEditMode = isEditMode,
                            editColor = Color(android.graphics.Color.parseColor(editColorHex)),
                            editIcon = editIcon
                        )

                        if (isEditMode) {
                            EditGoalSection(
                                name = editName,
                                onNameChange = { editName = it },
                                targetAmount = editTargetAmount,
                                onTargetAmountChange = { editTargetAmount = it },
                                category = editCategory ?: goal.category,
                                onCategoryClick = { showingCategoryPicker = true },
                                useDeadline = editUseDeadline,
                                onUseDeadlineChange = { editUseDeadline = it },
                                deadline = editDeadline,
                                onDeadlineChange = { editDeadline = it },
                                note = editNote,
                                onNoteChange = { editNote = it },
                                icon = editIcon,
                                onIconChange = { editIcon = it },
                                colorHex = editColorHex,
                                onColorChange = { editColorHex = it },
                                icons = listOf("🎯", "💰", "🏠", "🚗", "✈️", "🎓", "💍", "🏖️", "🏥", "🎁", "💻", "⛪", "🕋"),
                                colors = listOf("#4CAF50", "#2196F3", "#FF9800", "#E91E63", "#9C27B0", "#00BCD4", "#FFC107", "#795548"),
                                selectedColor = Color(android.graphics.Color.parseColor(editColorHex))
                            )
                        } else {
                            // View Mode
                            QuickActionsSection(onTopUpClick = { showingAddContribution = true }, goalColor = Color(android.graphics.Color.parseColor(goal.color ?: "#4CAF50")))
                            
                            GoalProgressSection(goal = goal)
                            
                            GoalDetailsSection(goal = goal)
                            
                            ContributionHistorySection(contributions = uiState.contributions)

                            // Delete Button
                            TextButton(
                                onClick = { showingDeleteConfirmation = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete Goal", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Goal not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showingAddContribution && goal != null) {
        AddContributionSheet(
            goal = goal,
            onDismiss = { showingAddContribution = false },
            onConfirm = { amount, note ->
                scope.launch {
                    viewModel.addContribution(goalId, amount, note)
                    showingAddContribution = false
                }
            }
        )
    }

    if (showingDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showingDeleteConfirmation = false },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete \"${goal?.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            if (viewModel.deleteGoal(goalId)) {
                                onNavigateBack()
                            }
                            showingDeleteConfirmation = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showingDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showingCategoryPicker) {
        GoalCategoryPickerBottomSheet(
            categories = uiState.categories,
            onDismiss = { showingCategoryPicker = false },
            onCategorySelected = {
                editCategory = it
                editIcon = it.icon
                editColorHex = it.color
                showingCategoryPicker = false
            }
        )
    }
}

@Composable
fun DetailHeader(
    title: String,
    onBack: () -> Unit,
    onAction: () -> Unit,
    actionText: String,
    isActionEnabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .background(Color.White, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 1,
            modifier = Modifier.weight(1.0f)
        )

        TextButton(
            onClick = onAction,
            enabled = isActionEnabled
        ) {
            Text(
                text = actionText,
                fontWeight = FontWeight.Bold,
                color = if (isActionEnabled) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}

@Composable
fun GoalBalanceCard(
    goal: Goal,
    isEditMode: Boolean,
    editColor: Color,
    editIcon: String
) {
    val color = if (isEditMode) editColor else Color(android.graphics.Color.parseColor(goal.color ?: "#4CAF50"))
    val icon = if (isEditMode) editIcon else (goal.icon ?: "🎯")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), spotColor = color.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(color, color.copy(alpha = 0.7f))
                )
            )
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Saved Amount",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = CurrencyFormatter.format(goal.currentAmount, goal.currency),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Target: ${CurrencyFormatter.format(goal.targetAmount, goal.currency)}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 32.sp)
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(onTopUpClick: () -> Unit, goalColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Button(
            onClick = onTopUpClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = goalColor),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(Icons.Default.AddCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Top Up Savings", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GoalProgressSection(goal: Goal) {
    val goalColor = Color(android.graphics.Color.parseColor(goal.color ?: "#4CAF50"))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = goalColor, modifier = Modifier.size(20.dp))
                    Text("Progress", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Surface(
                    color = goalColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "${goal.progress.percentage.toInt()}% Complete",
                        color = goalColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(goalColor.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(minOf(goal.progress.percentage.toFloat() / 100f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(goalColor, goalColor.copy(alpha = 0.7f))
                            )
                        )
                )
            }

            // Info Chips
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val remaining = maxOf(goal.targetAmount - goal.currentAmount, 0.0)
                InfoChip(
                    icon = Icons.Default.Info, // Placeholder icon
                    label = "Still Needed",
                    value = CurrencyFormatter.format(remaining, goal.currency),
                    color = Color(0xFFF44336), // Accent/Warning red
                    modifier = Modifier.weight(1f)
                )

                if (goal.progress.monthlySavingsNeeded != null) {
                    InfoChip(
                        icon = Icons.Default.DateRange,
                        label = "Per Month",
                        value = CurrencyFormatter.format(goal.progress.monthlySavingsNeeded, goal.currency),
                        color = goalColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            }

            Column {
                Text(text = label, fontSize = 10.sp, color = Color.Gray)
                Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
fun GoalDetailsSection(goal: Goal) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(label = "Category", value = goal.category.name)
                
                goal.deadline?.let {
                    DetailRow(label = "Deadline", value = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it))
                    goal.progress.daysRemaining?.let { days ->
                        DetailRow(label = "Time Left", value = "$days Days")
                    }
                }
                
                DetailRow(label = "Status", value = goal.status.name.lowercase().replaceFirstChar { it.uppercase() })
                
                goal.assetName?.let {
                    DetailRow(label = "Linked Asset", value = it)
                }
                
                if (!goal.note.isNullOrBlank()) {
                    DetailRow(label = "Note", value = goal.note)
                }
                
                DetailRow(label = "Last Updated", value = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(goal.updatedAt), isLast = true)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isLast: Boolean = false) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = Color.Gray, fontSize = 14.sp)
            Text(text = value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
        if (!isLast) {
            HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
        }
    }
}

@Composable
fun ContributionHistorySection(contributions: List<GoalContribution>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (contributions.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Text(
                    "No contributions yet",
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    contributions.forEachIndexed { index, contribution ->
                        ContributionRow(
                            contribution = contribution,
                            isLast = index == contributions.size - 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContributionRow(contribution: GoalContribution, isLast: Boolean) {
    val successColor = Color(0xFF4CAF50)
    
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        // Timeline Dot and Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(successColor)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(successColor.copy(alpha = 0.2f))
                )
            }
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 10.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = contribution.note?.ifBlank { "Contribution" } ?: "Contribution",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(contribution.datetime),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                
                Text(
                    text = "+${CurrencyFormatter.format(contribution.amount, CurrencyFormatter.defaultCurrency)}",
                    fontWeight = FontWeight.Bold,
                    color = successColor,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun EditGoalSection(
    name: String,
    onNameChange: (String) -> Unit,
    targetAmount: String,
    onTargetAmountChange: (String) -> Unit,
    category: GoalCategory,
    onCategoryClick: () -> Unit,
    useDeadline: Boolean,
    onUseDeadlineChange: (Boolean) -> Unit,
    deadline: Long,
    onDeadlineChange: (Long) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    icon: String,
    onIconChange: (String) -> Unit,
    colorHex: String,
    onColorChange: (String) -> Unit,
    icons: List<String>,
    colors: List<String>,
    selectedColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Name
        InputCard(title = "Goal Name") {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("e.g. Vacation Fund") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )
        }

        // Target Amount
        InputCard(title = "Target Amount") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CurrencyFormatter.symbol(CurrencyFormatter.defaultCurrency),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = onTargetAmountChange,
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }
        }

        // Category (Reuse simple clickable card)
        Surface(
            onClick = onCategoryClick,
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(selectedColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = category.icon, fontSize = 20.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Category", fontSize = 12.sp, color = Color.Gray)
                    Text(category.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
            }
        }

        // Timeline
        TimelineSection(
            useDeadline = useDeadline,
            onUseDeadlineChange = onUseDeadlineChange,
            deadline = deadline,
            onDeadlineChange = onDeadlineChange,
            selectedColor = selectedColor
        )

        // Note
        InputCard(title = "Note") {
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                placeholder = { Text("Optional notes...") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )
        }

        // Appearance
        OptionalFieldsSection(
            note = note,
            onNoteChange = onNoteChange,
            icon = icon,
            onIconChange = onIconChange,
            colorHex = colorHex,
            onColorChange = onColorChange,
            icons = icons,
            colors = colors,
            selectedColor = selectedColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContributionSheet(
    goal: Goal,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val goalColor = Color(android.graphics.Color.parseColor(goal.color ?: "#4CAF50"))
    val isValid = (amount.toDoubleOrNull() ?: 0.0) > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFF8F9FA),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Identity
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(goalColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(goal.icon ?: "🎯", fontSize = 30.sp)
                }
                Text(goal.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Add contribution to your goal", color = Color.Gray, fontSize = 14.sp)
            }

            // Amount Input
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Text("Amount", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                }
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (amount.isEmpty()) Color.Transparent else goalColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            CurrencyFormatter.symbol(goal.currency),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = goalColor
                        )
                        BasicTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Note Input
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Text("Note", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                }
                
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("What's this for?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }

            // Confirm Button
            Button(
                onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0, note) },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = goalColor,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(18.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// Reusing helper components from AddGoalScreen with compatible signatures or internal defines
@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    textStyle: androidx.compose.ui.text.TextStyle,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        modifier = modifier
    )
}
