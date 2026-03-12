package com.casha.app.ui.feature.budget.subview
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.*
import com.casha.app.ui.feature.budget.BudgetViewModel
import com.casha.app.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.casha.app.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetAIAdvisorSheet(
    onDismiss: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var incomeText by remember { mutableStateOf("") }
    val incomeValue = incomeText.toDoubleOrNull() ?: 0.0
    var showingApplySuccess by remember { mutableStateOf(false) }

    var fixedCategory by remember { mutableStateOf("") }
    var fixedAmountText by remember { mutableStateOf("") }
    val fixedAmountValue = fixedAmountText.toDoubleOrNull() ?: 0.0

    val fixedExpenses = remember { mutableStateMapOf<String, Double>() }
    var showCategoryMenu by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
modifier = Modifier.fillMaxSize(),
onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            modifier = Modifier.fillMaxHeight(0.9f),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.budget_advisor_title),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.budget_advisor_done), color = CashaSuccess)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    windowInsets = WindowInsets(0.dp)
                )
            },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            HeaderSection()

            // Income Input
            IncomeInputSection(
                incomeText = incomeText,
                incomeValue = incomeValue,
                isLoading = uiState.isLoading,
                onIncomeChange = { incomeText = it },
                onAnalyze = { viewModel.fetchRecommendations(incomeValue, fixedExpenses.toMap()) }
            )

            // Fixed Expenses
            FixedExpensesSection(
                categories = uiState.categories.map { it.name },
                fixedCategory = fixedCategory,
                fixedAmountText = fixedAmountText,
                fixedAmountValue = fixedAmountValue,
                fixedExpenses = fixedExpenses,
                showCategoryMenu = showCategoryMenu,
                onCategorySelect = { fixedCategory = it },
                onCategoryMenuToggle = { showCategoryMenu = it },
                onAmountChange = { fixedAmountText = it },
                onAddExpense = {
                    if (fixedCategory.isNotEmpty() && fixedAmountValue > 0) {
                        fixedExpenses[fixedCategory] = fixedAmountValue
                        fixedCategory = ""
                        fixedAmountText = ""
                    }
                },
                onRemoveExpense = { category ->
                    fixedExpenses.remove(category)
                }
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = CashaSuccess)
                        Text(stringResource(R.string.budget_advisor_analyzing), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            uiState.financialRecommendationResponse?.let { response ->
                StrategySummarySection(response.summary)
                FinancialSummarySection(response.financialSummary)
                
                if (response.insights.isNotEmpty()) {
                    InsightsSection(response.insights)
                }

                if (response.budgetMilestones.isNotEmpty()) {
                    BudgetMilestonesSection(response.budgetMilestones)
                }

                if (response.recommendations.isNotEmpty()) {
                    RecommendationsList(response.recommendations)
                    
                    ApplyAllButton(
                        isLoading = uiState.isLoading,
                        onApply = {
                            if (viewModel.applyRecommendedBudgets()) {
                                showingApplySuccess = true
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp)) // Extra padding for bottom sheet
        }

        if (showingApplySuccess) {
            AlertDialog(
                onDismissRequest = { showingApplySuccess = false },
                title = { Text(stringResource(R.string.budget_advisor_success_title)) },
                text = { Text(stringResource(R.string.budget_advisor_success_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        showingApplySuccess = false
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.budget_advisor_success_button))
                    }
                }
            )
        }
    }
}
}

// ── Sections ──

@Composable
private fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(elevation = 8.dp, shape = CircleShape, ambientColor = MaterialTheme.colorScheme.primary, spotColor = MaterialTheme.colorScheme.primary)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.budget_advisor_header_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                stringResource(R.string.budget_advisor_header_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun IncomeInputSection(
    incomeText: String,
    incomeValue: Double,
    isLoading: Boolean,
    onIncomeChange: (String) -> Unit,
    onAnalyze: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val isTyping = incomeText.isNotEmpty()

    val buttonColor by animateColorAsState(
        targetValue = if (isTyping && !isLoading) CashaSuccess else CashaSuccess.copy(alpha = 0.4f),
        animationSpec = tween(durationMillis = 300),
        label = "sendButtonColor"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.budget_advisor_income_title), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(CurrencyFormatter.symbol(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = if (isFocused) incomeText else if (incomeText.isNotEmpty()) CurrencyFormatter.formatInput(incomeText) else "",
                onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) onIncomeChange(it) },
                placeholder = { Text(stringResource(R.string.budget_advisor_income_placeholder)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).onFocusChanged { isFocused = it.isFocused },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            
            Button(
                onClick = onAnalyze,
                enabled = incomeValue > 0 && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    disabledContainerColor = CashaSuccess.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    stringResource(R.string.budget_advisor_income_analyze),
                    color = Color.White
                )
            }
        }
    }
}


@Composable
private fun FixedExpensesSection(
    categories: List<String>,
    fixedCategory: String,
    fixedAmountText: String,
    fixedAmountValue: Double,
    fixedExpenses: Map<String, Double>,
    showCategoryMenu: Boolean,
    onCategorySelect: (String) -> Unit,
    onCategoryMenuToggle: (Boolean) -> Unit,
    onAmountChange: (String) -> Unit,
    onAddExpense: () -> Unit,
    onRemoveExpense: (String) -> Unit
) {
    var isAmountFocused by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.budget_advisor_fixed_title), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
        ) {
            // Input Row
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                        .clickable { onCategoryMenuToggle(true) }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = fixedCategory.ifEmpty { stringResource(R.string.budget_advisor_fixed_category) },
                            fontSize = 12.sp,
                            color = if (fixedCategory.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    
                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { onCategoryMenuToggle(false) }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    onCategorySelect(category)
                                    onCategoryMenuToggle(false)
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = if (isAmountFocused) fixedAmountText else if (fixedAmountText.isNotEmpty()) CurrencyFormatter.formatInput(fixedAmountText) else "",
                    onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) onAmountChange(it) },
                    placeholder = { Text(stringResource(R.string.budget_advisor_fixed_amount), fontSize = 12.sp) },
                    modifier = Modifier.weight(1f).onFocusChanged { isAmountFocused = it.isFocused },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                IconButton(
                    onClick = onAddExpense,
                    enabled = fixedCategory.isNotEmpty() && fixedAmountValue > 0
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = CashaSuccess, modifier = Modifier.size(32.dp))
                }
            }

            // List of Expenses
            if (fixedExpenses.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    fixedExpenses.entries.sortedBy { it.key }.forEachIndexed { index, entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(entry.key, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = CurrencyFormatter.format(entry.value),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { onRemoveExpense(entry.key) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Cancel, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        if (index < fixedExpenses.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }

        Text(
            stringResource(R.string.budget_advisor_fixed_locked),
            fontStyle = FontStyle.Italic,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StrategySummarySection(summary: RecommendationSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .border(1.dp, CashaSuccess.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stringResource(R.string.budget_advisor_strategy_title, summary.strategy.replaceFirstChar { it.uppercase() }),
                fontWeight = FontWeight.Bold,
                color = CashaSuccess,
                fontSize = 16.sp
            )
            Icon(Icons.Default.Shield, contentDescription = null, tint = CashaSuccess)
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AllocationCircle(title = stringResource(R.string.budget_advisor_strategy_needs), percentage = summary.needsPercentage, color = CashaBlue, modifier = Modifier.weight(1f))
            AllocationCircle(title = stringResource(R.string.budget_advisor_strategy_wants), percentage = summary.wantsPercentage, color = CashaWarning, modifier = Modifier.weight(1f))
            AllocationCircle(title = stringResource(R.string.budget_advisor_strategy_savings), percentage = summary.savingsPercentage, color = CashaSuccess, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AllocationCircle(title: String, percentage: Int, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = color.copy(alpha = 0.1f), style = Stroke(width = 6.dp.toPx()))
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * (percentage / 100f),
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text("$percentage%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BudgetMilestonesSection(milestones: List<BudgetMilestone>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.budget_advisor_milestones_title), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        milestones.forEach { milestone ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(CashaSuccess.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = CashaSuccess, modifier = Modifier.size(20.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(milestone.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.budget_advisor_milestones_target, milestone.target),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = CashaSuccess,
                            modifier = Modifier
                                .background(CashaSuccess.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        milestone.action, 
                        fontSize = 13.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FinancialSummarySection(summary: FinancialSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.budget_advisor_summary_title), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(stringResource(R.string.budget_advisor_summary_debts), summary.debts, CashaDanger, Modifier.weight(1f))
            SummaryCard(stringResource(R.string.budget_advisor_summary_assets), summary.assets, CashaSuccess, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: Double, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier.size(8.dp).background(color, CircleShape)
            )
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(CurrencyFormatter.format(value), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun InsightsSection(insights: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.budget_advisor_insights_title), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .border(1.dp, CashaWarning.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            insights.forEach { insight ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Lightbulb, 
                        contentDescription = null, 
                        tint = CashaWarning, 
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Text(insight, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun RecommendationsList(recommendations: List<BudgetAIRecommendation>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.budget_advisor_recommendations_title), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        recommendations.filter { it.suggestedAmount > 0 }.forEach { rec ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CashaSuccess.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = CashaSuccess, modifier = Modifier.size(18.dp))
                        }
                        Text(rec.category, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    }
                    Text(CurrencyFormatter.format(rec.suggestedAmount), fontWeight = FontWeight.Bold, color = CashaSuccess, fontSize = 16.sp)
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                
                Text(
                    rec.reasoning, 
                    fontSize = 13.sp, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun ApplyAllButton(isLoading: Boolean, onApply: () -> Unit) {
    Button(
        onClick = onApply,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .height(56.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = CashaSuccess),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(R.string.budget_advisor_recommendations_apply_all), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
