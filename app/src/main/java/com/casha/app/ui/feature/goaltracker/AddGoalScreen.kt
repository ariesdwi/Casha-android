package com.casha.app.ui.feature.goaltracker

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.GoalCategory
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    viewModel: GoalTrackerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var category by remember {
        mutableStateOf(
            GoalCategory(
                id = "CUSTOM",
                name = "Custom",
                icon = "🎯",
                color = "#4CAF50"
            )
        )
    }
    
    var useDeadline by remember { mutableStateOf(false) }
    // We use a simple Long timestamp for the Date picker state
    var deadline by remember { mutableStateOf(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }.timeInMillis) }
    
    var icon by remember { mutableStateOf("🎯") }
    var colorHex by remember { mutableStateOf("#4CAF50") }
    var note by remember { mutableStateOf("") }
    var assetId by remember { mutableStateOf<String?>(null) }
    
    // Default system currency
    val userCurrency = CurrencyFormatter.defaultCurrency

    val icons = listOf("🎯", "💰", "🏠", "🚗", "✈️", "🎓", "💍", "🏖️", "🏥", "🎁", "💻", "⛪", "🕋")
    val colors = listOf("#4CAF50", "#2196F3", "#FF9800", "#E91E63", "#9C27B0", "#00BCD4", "#FFC107", "#795548")

    val selectedColor = remember(colorHex) {
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFF4CAF50)
        }
    }

    val isValid = remember(name, targetAmount) {
        name.isNotBlank() && targetAmount.isNotBlank() && (targetAmount.toDoubleOrNull() ?: 0.0) > 0
    }

    var showingCategoryPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(bottom = paddingValues.calculateBottomPadding() + 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Custom Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "New Goal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Wrapping the rest of the content in a padded column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
            // Goal Preview Card
            GoalPreviewCard(
                name = name,
                targetAmount = targetAmount,
                icon = icon,
                color = selectedColor,
                userCurrency = userCurrency,
                categoryName = category.name
            )

            // Category Selection Card
            CategorySelectionCard(
                category = category,
                selectedColor = selectedColor,
                onClick = { showingCategoryPicker = true }
            )

            // Goal Name Input
            InputCard(title = "Goal Name") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Vacation Fund") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }

            // Target Amount Input
            InputCard(title = "Target Amount") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = CurrencyFormatter.symbol(userCurrency),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    OutlinedTextField(
                        value = targetAmount,
                        onValueChange = { targetAmount = it },
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

            // Timeline Section
            TimelineSection(
                useDeadline = useDeadline,
                onUseDeadlineChange = { useDeadline = it },
                deadline = deadline,
                onDeadlineChange = { deadline = it },
                selectedColor = selectedColor
            )

            // Optional Fields Section
            OptionalFieldsSection(
                note = note,
                onNoteChange = { note = it },
                icon = icon,
                onIconChange = { icon = it },
                colorHex = colorHex,
                onColorChange = { colorHex = it },
                icons = icons,
                colors = colors,
                selectedColor = selectedColor
            )

            // Create Button
            Button(
                onClick = {
                    viewModel.createGoal(
                        name = name,
                        targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                        category = category,
                        deadline = if (useDeadline) Date(deadline) else null,
                        assetId = assetId,
                        icon = icon,
                        color = colorHex,
                        note = note,
                        onSuccess = { onNavigateBack() }
                    )
                },
                enabled = isValid && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedColor,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Create Goal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
            } // End of inner padded column
            
            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    if (showingCategoryPicker) {
        GoalCategoryPickerBottomSheet(
            categories = uiState.categories,
            onDismiss = { showingCategoryPicker = false },
            onCategorySelected = { 
                category = it
                showingCategoryPicker = false 
            }
        )
    }
}
