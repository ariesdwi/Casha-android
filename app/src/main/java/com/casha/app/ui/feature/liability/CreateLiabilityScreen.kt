package com.casha.app.ui.feature.liability

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.focus.onFocusChanged
import com.casha.app.core.util.CurrencyFormatter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casha.app.domain.model.CreateLiabilityRequest
import com.casha.app.domain.model.InterestType
import com.casha.app.domain.model.LiabilityCategory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLiabilityScreen(
    viewModel: LiabilityViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Core details
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(LiabilityCategory.MORTGAGE) }
    var bankName by remember { mutableStateOf("") }
    
    // Amounts
    var creditLimit by remember { mutableStateOf("") }
    var principal by remember { mutableStateOf("") }
    var currentBalance by remember { mutableStateOf("") }
    
    // Credit Card Specific
    var billingDay by remember { mutableStateOf("1") }
    var dueDay by remember { mutableStateOf("25") }
    var minPaymentPercentage by remember { mutableStateOf("10") }
    
    // Common Configs
    var interestRate by remember { mutableStateOf("") }
    var interestType by remember { mutableStateOf(InterestType.MONTHLY) }
    var description by remember { mutableStateOf("") }
    
    // Dates
    val calendar = Calendar.getInstance()
    var startDate by remember { mutableStateOf(calendar.time) }
    calendar.add(Calendar.YEAR, 5)
    var endDate by remember { mutableStateOf(calendar.time) }
    
    var showingCategoryPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val isCreditCard = selectedCategory == LiabilityCategory.CREDIT_CARD
    val context = LocalContext.current
    val isoFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA) // Premium off-white app background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(bottom = paddingValues.calculateBottomPadding())
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Custom Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
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
                        imageVector = Icons.Default.ArrowBack, // Standardized casha apps generic back icon
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Add Liability",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            // Error Message
            errorMessage?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // Category Picker
            CategorySelectionCard(
                selectedCategory = selectedCategory,
                onClick = { showingCategoryPicker = true }
            )
            
            // Name
            InputCard(title = if (isCreditCard) "Card Name" else "Liability Name") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(if (isCreditCard) "e.g., Platinum Visa" else "e.g., KPR Rumah", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F4F6),
                        unfocusedContainerColor = Color(0xFFF3F4F6),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    )
                )
            }
            
            // Bank Name
            InputCard(title = "Bank Name") {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    placeholder = { Text("e.g., BCA, Mandiri", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F4F6),
                        unfocusedContainerColor = Color(0xFFF3F4F6),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    )
                )
            }
            
            // Amounts section
            if (isCreditCard) {
                var isCreditLimitFocused by remember { mutableStateOf(false) }
                InputCard(title = "Credit Limit") {
                    OutlinedTextField(
                        value = if (isCreditLimitFocused) creditLimit else if (creditLimit.isNotEmpty()) CurrencyFormatter.formatInput(creditLimit) else "",
                        onValueChange = { creditLimit = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().onFocusChanged { isCreditLimitFocused = it.isFocused },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3F4F6),
                            unfocusedContainerColor = Color(0xFFF3F4F6),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                        )
                    )
                }
                
                var isCurrentBalanceFocused by remember { mutableStateOf(false) }
                InputCard(title = "Current Balance") {
                    OutlinedTextField(
                        value = if (isCurrentBalanceFocused) currentBalance else if (currentBalance.isNotEmpty()) CurrencyFormatter.formatInput(currentBalance) else "",
                        onValueChange = { currentBalance = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().onFocusChanged { isCurrentBalanceFocused = it.isFocused },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3F4F6),
                            unfocusedContainerColor = Color(0xFFF3F4F6),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                        )
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
                    InputCard(title = "Billing Day", modifier = Modifier.weight(1f).padding(0.dp)) {
                        OutlinedTextField(
                            value = billingDay,
                            onValueChange = { billingDay = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF3F4F6),
                                unfocusedContainerColor = Color(0xFFF3F4F6),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                            )
                        )
                    }
                    InputCard(title = "Due Day", modifier = Modifier.weight(1f).padding(0.dp)) {
                        OutlinedTextField(
                            value = dueDay,
                            onValueChange = { dueDay = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF3F4F6),
                                unfocusedContainerColor = Color(0xFFF3F4F6),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                            )
                        )
                    }
                }
                
                InputCard(title = "Minimum Payment (%)") {
                    OutlinedTextField(
                        value = minPaymentPercentage,
                        onValueChange = { minPaymentPercentage = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3F4F6),
                            unfocusedContainerColor = Color(0xFFF3F4F6),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                        )
                    )
                }
            } else {
                var isPrincipalFocused by remember { mutableStateOf(false) }
                InputCard(title = "Principal Amount") {
                    OutlinedTextField(
                        value = if (isPrincipalFocused) principal else if (principal.isNotEmpty()) CurrencyFormatter.formatInput(principal) else "",
                        onValueChange = { principal = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().onFocusChanged { isPrincipalFocused = it.isFocused },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3F4F6),
                            unfocusedContainerColor = Color(0xFFF3F4F6),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                        )
                    )
                }
            }
            
            // Interest Configuration
            InputCard(title = "Interest Rate (%)") {
                OutlinedTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F4F6),
                        unfocusedContainerColor = Color(0xFFF3F4F6),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    )
                )
            }
            
            InputCard(title = "Interest Type") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectableChip(
                        text = "Monthly",
                        selected = interestType == InterestType.MONTHLY,
                        onClick = { interestType = InterestType.MONTHLY },
                        modifier = Modifier.weight(1f)
                    )
                    SelectableChip(
                        text = "Flat",
                        selected = interestType == InterestType.FLAT,
                        onClick = { interestType = InterestType.FLAT },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Dates Section
            val startCalendar = Calendar.getInstance().apply { time = startDate }
            val endCalendar = Calendar.getInstance().apply { time = endDate }
            
            val startDatePickerDialog = DatePickerDialog(
                context,
                { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                    startCalendar.set(year, month, dayOfMonth)
                    startDate = startCalendar.time
                },
                startCalendar.get(Calendar.YEAR),
                startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH)
            )

            val endDatePickerDialog = DatePickerDialog(
                context,
                { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                    endCalendar.set(year, month, dayOfMonth)
                    endDate = endCalendar.time
                },
                endCalendar.get(Calendar.YEAR),
                endCalendar.get(Calendar.MONTH),
                endCalendar.get(Calendar.DAY_OF_MONTH)
            )
            
            InputCard(title = "Start Date") {
                Button(
                    onClick = { startDatePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF3F4F6),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        isoFormatter.format(startDate), 
                        modifier = Modifier.fillMaxWidth(), 
                        color = MaterialTheme.colorScheme.onSurface 
                    )
                }
            }
            
            InputCard(title = "End Date") {
                Button(
                    onClick = { endDatePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF3F4F6),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        isoFormatter.format(endDate), 
                        modifier = Modifier.fillMaxWidth(), 
                        color = MaterialTheme.colorScheme.onSurface 
                    )
                }
            }
            
            // Description
            InputCard(title = "Description (Optional)") {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Notes about this liability", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F4F6),
                        unfocusedContainerColor = Color(0xFFF3F4F6),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Create Button
            Button(
                onClick = {
                    errorMessage = null
                    val interestVal = interestRate.toDoubleOrNull()
                    if (name.isBlank()) {
                        errorMessage = "Please enter liability name"
                        return@Button
                    }
                    if (interestVal == null || interestVal < 0) {
                        errorMessage = "Please enter a valid interest rate"
                        return@Button
                    }
                    
                    val request = CreateLiabilityRequest(
                        name = name,
                        bankName = bankName.takeIf { it.isNotBlank() },
                        category = selectedCategory,
                        creditLimit = if (isCreditCard) creditLimit.toDoubleOrNull() else null,
                        billingDay = if (isCreditCard) billingDay.toIntOrNull() else null,
                        dueDay = if (isCreditCard) dueDay.toIntOrNull() else null,
                        minPaymentPercentage = if (isCreditCard) minPaymentPercentage.toDoubleOrNull() else null,
                        interestRate = interestVal,
                        interestType = interestType,
                        lateFee = null,
                        principal = if (isCreditCard) 0.0 else (principal.toDoubleOrNull() ?: 0.0),
                        currentBalance = if (isCreditCard) currentBalance.toDoubleOrNull() else null,
                        startDate = isoFormatter.format(startDate),
                        endDate = isoFormatter.format(endDate),
                        description = description.takeIf { it.isNotBlank() }
                    )
                    
                    viewModel.createLiability(request, onSuccess = onNavigateBack)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Create Liability", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(120.dp))
        }

        // Category Picker Dialog
        if (showingCategoryPicker) {
            Dialog(onDismissRequest = { showingCategoryPicker = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select Category",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        LiabilityCategory.entries.forEach { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCategory = category
                                        showingCategoryPicker = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category.displayName,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selectedCategory == category) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
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
private fun CategorySelectionCard(
    selectedCategory: LiabilityCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.05f),
                ambientColor = Color.Transparent
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Here you would optimally map icon dynamically
                Text("🏦", fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Liability Category",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = selectedCategory.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InputCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.05f),
                ambientColor = Color.Transparent
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Wrapping content inside an unstyled/less bulky modifier context is handled by customizing OutlinedTextFields at the caller
            // or we could provide a CompositionLocal if we really scaled. For now we will update the implementations. 
            content()
        }
    }
}

@Composable
private fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFF3F4F6)
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = contentColor, fontWeight = fontWeight, fontSize = 14.sp)
    }
}
