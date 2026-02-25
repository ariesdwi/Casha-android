package com.casha.app.ui.feature.liability.forminput

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.Liability
import com.casha.app.domain.model.CategoryCasha
import com.casha.app.ui.feature.liability.LiabilityState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLiabilityTransactionFormView(
    liability: Liability,
    liabilityState: LiabilityState,
    userCurrency: String,
    categories: List<CategoryCasha>,
    onDismissRequest: () -> Unit,
    onSubmit: (name: String, amount: Double, categoryId: String, description: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var amountFocused by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryCasha?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    val amountValue = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val isFormValid = name.isNotBlank() && amountValue > 0 && selectedCategory != null
    val currencySymbol = CurrencyFormatter.symbol(userCurrency)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color(0xFFF8F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            // Header
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Catat Transaksi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${liability.name} · ${liability.category.displayName}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name
                InputCard(title = "Nama Transaksi *") {
                    CashaFormTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "e.g., Belanja Tokopedia"
                    )
                }

                // Amount
                InputCard(title = "Jumlah *") {
                    OutlinedTextField(
                        value = if (amountFocused) amountText else if (amountText.isNotEmpty()) CurrencyFormatter.formatInput(amountText) else "",
                        onValueChange = { amountText = it.replace(",", ".") },
                        placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().onFocusChanged { amountFocused = it.isFocused },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = cashaBorderlessTextFieldColors(),
                        leadingIcon = {
                            Text(currencySymbol, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        },
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    )
                }

                // Category
                InputCard(title = "Kategori *") {
                    Box {
                        Surface(
                            onClick = { showCategoryDropdown = true },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCategory?.name ?: "Pilih Kategori",
                                    fontSize = 15.sp,
                                    fontWeight = if (selectedCategory != null) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedCategory != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Description
                InputCard(title = "Deskripsi") {
                    CashaFormTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Tambah catatan (Opsional)",
                        singleLine = false
                    )
                }

                // Available Credit Info
                liability.availableCredit?.let { credit ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Info Kartu:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Sisa limit: ${CurrencyFormatter.format(credit, userCurrency)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            if (amountValue > 0) {
                                val newCredit = credit - amountValue
                                Text(
                                    "Setelah transaksi: ${CurrencyFormatter.format(maxOf(newCredit, 0.0), userCurrency)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (newCredit < 0) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Submit
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FA))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = {
                        onSubmit(
                            name,
                            amountValue,
                            selectedCategory!!.id,
                            description.takeIf { it.isNotBlank() }
                        )
                    },
                    enabled = isFormValid && !liabilityState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009033),
                        disabledContainerColor = Color(0xFF009033).copy(alpha = 0.4f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
                ) {
                    if (liabilityState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                            Text("Catat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
