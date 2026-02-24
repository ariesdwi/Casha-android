package com.casha.app.ui.feature.goaltracker

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.domain.model.GoalCategory
import com.casha.app.core.util.CurrencyFormatter

@Composable
fun InputCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineSection(
    useDeadline: Boolean,
    onUseDeadlineChange: (Boolean) -> Unit,
    deadline: Long,
    onDeadlineChange: (Long) -> Unit,
    selectedColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = selectedColor
                    )
                    Text("Set Deadline", fontSize = 14.sp)
                }
                Switch(
                    checked = useDeadline,
                    onCheckedChange = onUseDeadlineChange,
                    colors = SwitchDefaults.colors(checkedTrackColor = selectedColor)
                )
            }
        }

        AnimatedVisibility(
            visible = useDeadline,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = deadline)
            
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { onDeadlineChange(it) }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun OptionalFieldsSection(
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InputCard(title = "Notes") {
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                placeholder = { Text("Add any details...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Appearance",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                var iconMenuExpanded by remember { mutableStateOf(false) }
                var colorMenuExpanded by remember { mutableStateOf(false) }

                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(selectedColor.copy(alpha = 0.1f))
                            .clickable { iconMenuExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(icon)
                        Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(16.dp), tint = selectedColor)
                    }

                    DropdownMenu(expanded = iconMenuExpanded, onDismissRequest = { iconMenuExpanded = false }) {
                        icons.chunked(4).forEach { rowIcons ->
                            Row {
                                rowIcons.forEach { currentIcon ->
                                    DropdownMenuItem(
                                        text = { Text(currentIcon, fontSize = 20.sp) },
                                        onClick = {
                                            onIconChange(currentIcon)
                                            iconMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(selectedColor.copy(alpha = 0.1f))
                            .clickable { colorMenuExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(selectedColor)
                        )
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = selectedColor)
                    }

                    DropdownMenu(expanded = colorMenuExpanded, onDismissRequest = { colorMenuExpanded = false }) {
                        colors.chunked(4).forEach { rowColors ->
                            Row {
                                rowColors.forEach { currentColorHex ->
                                    val safeColor = try { Color(android.graphics.Color.parseColor(currentColorHex)) } catch (e: Exception) { Color.Gray }
                                    DropdownMenuItem(
                                        text = {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(safeColor)
                                            )
                                        },
                                        onClick = {
                                            onColorChange(currentColorHex)
                                            colorMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySelectionCard(
    category: GoalCategory,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(selectedColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(category.icon, fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Category",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCategoryPickerBottomSheet(
    categories: List<GoalCategory>,
    onDismiss: () -> Unit,
    onCategorySelected: (GoalCategory) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                "Select Category",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            categories.forEach { cat ->
                val safeColor = try { Color(android.graphics.Color.parseColor(cat.color)) } catch (e: Exception) { Color.Gray }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategorySelected(cat) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(safeColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(cat.icon, fontSize = 20.sp)
                    }

                    Text(
                        text = cat.name,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun GoalPreviewCard(
    name: String,
    targetAmount: String,
    icon: String,
    color: Color,
    userCurrency: String,
    categoryName: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 32.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.ifBlank { "Unset Goal Name" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Target: ${CurrencyFormatter.format(targetAmount.toDoubleOrNull() ?: 0.0, userCurrency)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
