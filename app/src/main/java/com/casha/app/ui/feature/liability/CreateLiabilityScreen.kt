package com.casha.app.ui.feature.liability

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.CreateLiabilityRequest
import com.casha.app.domain.model.InterestType
import com.casha.app.domain.model.LiabilityCategory
import com.casha.app.ui.feature.liability.forminput.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLiabilityScreen(
    selectedCategory: LiabilityCategory = LiabilityCategory.MORTGAGE,
    viewModel: LiabilityViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userCurrency = CurrencyFormatter.defaultCurrency

    // Core details
    var name by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }

    // Amounts
    var creditLimit by remember { mutableStateOf("") }
    var principal by remember { mutableStateOf("") }
    var currentBalance by remember { mutableStateOf("") }
    var tenor by remember { mutableStateOf("") }
    var monthlyInstallment by remember { mutableStateOf("") }
    var gracePeriodMonths by remember { mutableStateOf("") }

    // Credit Card Specific
    var billingDay by remember { mutableStateOf("1") }
    var dueDay by remember { mutableStateOf("25") }
    var minPaymentPercentage by remember { mutableStateOf("10") }
    var lateFee by remember { mutableStateOf("") }

    // Common Configs
    var interestRate by remember { mutableStateOf("") }
    var interestType by remember { mutableStateOf(InterestType.MONTHLY) }
    var description by remember { mutableStateOf("") }

    // Dates
    val calendar = Calendar.getInstance()
    var startDate by remember { mutableStateOf(calendar.time) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isCreditCard = selectedCategory == LiabilityCategory.CREDIT_CARD
    val isPayLater = selectedCategory == LiabilityCategory.PAY_LATER
    val isCreditBased = isCreditCard || isPayLater
    val isoFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Title based on category
    val screenTitle = when (selectedCategory) {
        LiabilityCategory.CREDIT_CARD -> "Tambah Kartu Kredit"
        LiabilityCategory.PAY_LATER -> "Tambah Pay Later"
        LiabilityCategory.MORTGAGE -> "Tambah KPR"
        LiabilityCategory.AUTO_LOAN -> "Tambah Kredit Kendaraan"
        LiabilityCategory.STUDENT_LOAN -> "Tambah Pinjaman Pendidikan"
        LiabilityCategory.BUSINESS_LOAN -> "Tambah Pinjaman Usaha"
        LiabilityCategory.PERSONAL_LOAN -> "Tambah Pinjaman"
        LiabilityCategory.OTHER -> "Tambah Hutang"
    }

    // Name placeholder based on category
    val namePlaceholder = when (selectedCategory) {
        LiabilityCategory.CREDIT_CARD -> "e.g., BCA Platinum"
        LiabilityCategory.PAY_LATER -> "e.g., Shopee PayLater"
        LiabilityCategory.MORTGAGE -> "e.g., KPR Rumah"
        LiabilityCategory.AUTO_LOAN -> "e.g., Kredit Mobil Brio"
        LiabilityCategory.STUDENT_LOAN -> "e.g., Pinjaman Kuliah"
        LiabilityCategory.PERSONAL_LOAN -> "e.g., Pinjaman KTA"
        LiabilityCategory.BUSINESS_LOAN -> "e.g., Modal Kerja"
        LiabilityCategory.OTHER -> "e.g., Hutang Lainnya"
    }

    val notesPlaceholder = when (selectedCategory) {
        LiabilityCategory.CREDIT_CARD -> "e.g., Kartu utama belanja"
        LiabilityCategory.PAY_LATER -> "e.g., Saldo e-commerce"
        LiabilityCategory.MORTGAGE -> "e.g., Rumah Serpong Blok A"
        LiabilityCategory.AUTO_LOAN -> "e.g., Cicilan mobil Brio"
        LiabilityCategory.STUDENT_LOAN -> "e.g., Biaya semester 3"
        LiabilityCategory.PERSONAL_LOAN -> "e.g., Modal renovasi"
        LiabilityCategory.BUSINESS_LOAN -> "e.g., Modal kerja"
        LiabilityCategory.OTHER -> "e.g., Catatan pinjaman"
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onNavigateBack,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color(0xFFF8F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            // ── Header ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = screenTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Mohon lengkapi detail informasi berikut",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // ── Error Message ───────────────────────────────────
            errorMessage?.let { errorMsg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Basic Info Section ──────────────────────────────
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Name
                    InputCard(
                        title = if (isCreditCard) "Nama Kartu *" else if (isPayLater) "Nama Layanan *" else "Nama Hutang *"
                    ) {
                        CashaFormTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = namePlaceholder
                        )
                    }

                    // Bank Name (not for PayLater)
                    if (!isPayLater) {
                        InputCard(title = "Bank *") {
                            CashaFormTextField(
                                value = bankName,
                                onValueChange = { bankName = it },
                                placeholder = "e.g., BCA, Mandiri"
                            )
                        }
                    }
                }

                // ── Category-Specific Fields ────────────────────────
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedCategory) {
                        LiabilityCategory.CREDIT_CARD -> {
                            CreditCardFormView(
                                creditLimit = creditLimit,
                                onCreditLimitChange = { creditLimit = it },
                                currentBalance = currentBalance,
                                onCurrentBalanceChange = { currentBalance = it },
                                interestRate = interestRate,
                                onInterestRateChange = { interestRate = it },
                                interestType = interestType,
                                onInterestTypeChange = { interestType = it },
                                billingDay = billingDay,
                                onBillingDayChange = { billingDay = it },
                                dueDay = dueDay,
                                onDueDayChange = { dueDay = it },
                                minPaymentPercentage = minPaymentPercentage,
                                onMinPaymentPercentageChange = { minPaymentPercentage = it },
                                lateFee = lateFee,
                                onLateFeeChange = { lateFee = it },
                                userCurrency = userCurrency
                            )
                        }
                        LiabilityCategory.PAY_LATER -> {
                            PayLaterFormView(
                                creditLimit = creditLimit,
                                onCreditLimitChange = { creditLimit = it },
                                currentBalance = currentBalance,
                                onCurrentBalanceChange = { currentBalance = it },
                                interestRate = interestRate,
                                onInterestRateChange = { interestRate = it },
                                interestType = interestType,
                                onInterestTypeChange = { interestType = it },
                                billingDay = billingDay,
                                onBillingDayChange = { billingDay = it },
                                dueDay = dueDay,
                                onDueDayChange = { dueDay = it },
                                userCurrency = userCurrency
                            )
                        }
                        LiabilityCategory.STUDENT_LOAN -> {
                            StudentLoanFormView(
                                principal = principal,
                                onPrincipalChange = { principal = it },
                                currentBalance = currentBalance,
                                onCurrentBalanceChange = { currentBalance = it },
                                tenor = tenor,
                                onTenorChange = { tenor = it },
                                gracePeriodMonths = gracePeriodMonths,
                                onGracePeriodMonthsChange = { gracePeriodMonths = it },
                                interestRate = interestRate,
                                onInterestRateChange = { interestRate = it },
                                interestType = interestType,
                                onInterestTypeChange = { interestType = it },
                                startDate = startDate,
                                onStartDateChange = { startDate = it },
                                dueDay = dueDay,
                                onDueDayChange = { dueDay = it },
                                monthlyInstallment = monthlyInstallment,
                                onMonthlyInstallmentChange = { monthlyInstallment = it },
                                userCurrency = userCurrency
                            )
                        }
                        else -> {
                            RegularLoanFormView(
                                principal = principal,
                                onPrincipalChange = { principal = it },
                                currentBalance = currentBalance,
                                onCurrentBalanceChange = { currentBalance = it },
                                tenor = tenor,
                                onTenorChange = { tenor = it },
                                interestRate = interestRate,
                                onInterestRateChange = { interestRate = it },
                                interestType = interestType,
                                onInterestTypeChange = { interestType = it },
                                startDate = startDate,
                                onStartDateChange = { startDate = it },
                                dueDay = dueDay,
                                onDueDayChange = { dueDay = it },
                                monthlyInstallment = monthlyInstallment,
                                onMonthlyInstallmentChange = { monthlyInstallment = it },
                                userCurrency = userCurrency
                            )
                        }
                    }
                }

                // ── Notes ───────────────────────────────────────────
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    InputCard(title = "Catatan") {
                        CashaFormTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = notesPlaceholder,
                            singleLine = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Submit Section ──────────────────────────────────
            Surface(
                color = Color(0xFFF8F9FA),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = {
                        errorMessage = null
                        val interestVal = interestRate.toDoubleOrNull()
                        if (name.isBlank()) {
                            errorMessage = "Masukkan nama hutang"
                            return@Button
                        }
                        if (interestVal == null || interestVal < 0) {
                            errorMessage = "Masukkan rate bunga yang valid"
                            return@Button
                        }

                        if (isCreditBased) {
                            val limitVal = creditLimit.toDoubleOrNull()
                            if (limitVal == null || limitVal <= 0) {
                                errorMessage = "Masukkan limit kredit yang valid"
                                return@Button
                            }
                        } else {
                            val principalVal = principal.toDoubleOrNull()
                            if (principalVal == null || principalVal <= 0) {
                                errorMessage = "Masukkan jumlah pinjaman yang valid"
                                return@Button
                            }
                        }

                        // Only send endDate for loans without tenor (backend calculates from tenor)
                        val tenorMonths = tenor.toIntOrNull()
                        val endDateStr: String? = null

                        val request = CreateLiabilityRequest(
                            name = name,
                            bankName = if (isPayLater) null else bankName.takeIf { it.isNotBlank() },
                            category = selectedCategory,
                            creditLimit = if (isCreditBased) creditLimit.toDoubleOrNull() else null,
                            billingDay = if (isCreditBased) billingDay.toIntOrNull() else null,
                            dueDay = dueDay.toIntOrNull(),
                            minPaymentPercentage = if (isCreditCard) minPaymentPercentage.toDoubleOrNull() else null,
                            interestRate = interestVal,
                            interestType = interestType,
                            lateFee = if (isCreditCard) lateFee.toDoubleOrNull() else null,
                            principal = if (isCreditBased) 0.0 else (principal.toDoubleOrNull() ?: 0.0),
                            currentBalance = if (isCreditBased) currentBalance.toDoubleOrNull() else currentBalance.toDoubleOrNull(),
                            startDate = if (isCreditBased) null else isoFormatter.format(startDate),
                            endDate = endDateStr,
                            description = description.takeIf { it.isNotBlank() },
                            tenor = tenorMonths,
                            monthlyInstallment = monthlyInstallment.toDoubleOrNull(),
                            gracePeriodMonths = if (selectedCategory == LiabilityCategory.STUDENT_LOAN) gracePeriodMonths.toIntOrNull() else null
                        )

                        viewModel.createLiability(request, onSuccess = onNavigateBack)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009033),
                        disabledContainerColor = Color(0xFF009033).copy(alpha = 0.4f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Text(
                                "Simpan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
