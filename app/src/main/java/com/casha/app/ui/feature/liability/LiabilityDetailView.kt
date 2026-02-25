package com.casha.app.ui.feature.liability

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.casha.app.domain.model.CategoryCasha
import com.casha.app.domain.model.Liability
import com.casha.app.domain.model.LiabilityStatement
import com.casha.app.domain.model.PaymentType
import com.casha.app.ui.feature.liability.subviews.LiabilityBalanceCardView
import com.casha.app.ui.feature.liability.subviews.LiabilityCreditCardSectionsView
import com.casha.app.ui.feature.liability.subviews.LiabilityInfoDetailsView
import com.casha.app.ui.feature.liability.subviews.LiabilityQuickActionsView
import com.casha.app.ui.feature.liability.subviews.detail.*
import com.casha.app.core.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiabilityDetailView(
    initialLiability: Liability,
    liabilityState: LiabilityState,
    onBack: () -> Unit,
    onRecordPayment: (Double, PaymentType, Double?, Double?, String?) -> Unit,
    onAddInstallment: (String, Double, Double, Int, Int, java.util.Date) -> Unit,
    onSimulatePayoff: (com.casha.app.domain.model.SimulationStrategy, Double) -> Unit,
    onAddTransaction: () -> Unit,
    onCreateTransaction: ((String, Double, String, String?) -> Unit)? = null,
    categories: List<CategoryCasha> = emptyList(),
    onStatementClick: (LiabilityStatement) -> Unit
) {
    val liability = liabilityState.liabilities.firstOrNull { it.id == initialLiability.id } ?: initialLiability
    val userCurrency = CurrencyFormatter.defaultCurrency
    val scrollState = rememberScrollState()

    var showingRecordPayment by remember { mutableStateOf(false) }
    var paymentAmountValue by remember { mutableDoubleStateOf(0.0) }
    var paymentType by remember { mutableStateOf(PaymentType.PARTIAL) }
    var showingPaymentHistory by remember { mutableStateOf(false) }
    var showingSimulation by remember { mutableStateOf(false) }
    var showingAddInstallment by remember { mutableStateOf(false) }
    var showingAddTransaction by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(bottom = paddingValues.calculateBottomPadding())
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Custom Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(elevation = 2.dp, shape = androidx.compose.foundation.shape.CircleShape)
                        .background(MaterialTheme.colorScheme.surface, androidx.compose.foundation.shape.CircleShape)
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
                    text = liability.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (liability.category.isRevolving) {
                // ── Revolving (Credit Card / Pay Later) ──
                
                // Balance Card
                LiabilityBalanceCardView(
                    liability = liability,
                    latestStatement = liabilityState.latestStatement,
                    userCurrency = userCurrency
                )

                // Quick Actions
                LiabilityQuickActionsView(
                    onRecordPayment = { showingRecordPayment = true },
                    onAddTransaction = {
                        if (onCreateTransaction != null) {
                            showingAddTransaction = true
                        } else {
                            onAddTransaction()
                        }
                    },
                    onAddInstallment = { showingAddInstallment = true }
                )

                // Liability Info Details
                LiabilityInfoDetailsView(
                    liability = liability,
                    userCurrency = userCurrency
                )

                // Payment History
                PaymentHistorySection(
                    payments = liabilityState.paymentHistory,
                    userCurrency = userCurrency,
                    isLoading = liabilityState.isLoading,
                    onViewAll = { showingPaymentHistory = true }
                )

                // Credit Card Specific Sections (Statements, Unbilled, Installments)
                LiabilityCreditCardSectionsView(
                    liabilityState = liabilityState,
                    liability = liability,
                    userCurrency = userCurrency,
                    onPaymentClick = { amount, type ->
                        paymentAmountValue = amount
                        paymentType = type
                        showingRecordPayment = true
                    },
                    onStatementClick = onStatementClick,
                    onTransactionClick = { /* Handle transaction click */ }
                )
            } else {
                // ── Fixed Loan (Mortgage, Personal, Auto, Student, Business, Other) ──
                
                // Loan Balance Card
                LiabilityLoanBalanceCardView(
                    liability = liability,
                    userCurrency = userCurrency
                )

                // Quick Actions (Pay + Simulate)
                LiabilityLoanQuickActionsView(
                    onRecordPayment = { showingRecordPayment = true },
                    onSimulatePayment = { showingSimulation = true }
                )

                // Loan Info Box
                LiabilityLoanInfoBoxView(
                    liability = liability,
                    userCurrency = userCurrency
                )

                // Loan Payment Summary
                LiabilityLoanPaymentSummaryView(
                    liability = liability,
                    userCurrency = userCurrency
                )

                // Payment History
                PaymentHistorySection(
                    payments = liabilityState.paymentHistory,
                    userCurrency = userCurrency,
                    isLoading = liabilityState.isLoading,
                    onViewAll = { showingPaymentHistory = true }
                )
            }

            Spacer(modifier = Modifier.height(84.dp))
        }

        // Record Payment bottom sheet
        if (showingRecordPayment) {
            com.casha.app.ui.feature.liability.forminput.RecordPaymentFormView(
                liability = liability,
                liabilityState = liabilityState,
                userCurrency = userCurrency,
                onDismissRequest = { showingRecordPayment = false },
                onSubmit = { amount, date, type, principal, interest, notes ->
                    onRecordPayment(amount, type, principal, interest, notes)
                    showingRecordPayment = false
                }
            )
        }

        // Add Installment bottom sheet
        if (showingAddInstallment) {
            com.casha.app.ui.feature.liability.forminput.AddInstallmentFormView(
                liability = liability,
                liabilityState = liabilityState,
                userCurrency = userCurrency,
                onDismissRequest = { showingAddInstallment = false },
                onSubmit = { name, total, monthly, tenor, current, start ->
                    onAddInstallment(name, total, monthly, tenor, current, start)
                    showingAddInstallment = false
                }
            )
        }

        // Add Liability Transaction bottom sheet
        if (showingAddTransaction && onCreateTransaction != null) {
            com.casha.app.ui.feature.liability.forminput.AddLiabilityTransactionFormView(
                liability = liability,
                liabilityState = liabilityState,
                userCurrency = userCurrency,
                categories = categories,
                onDismissRequest = { showingAddTransaction = false },
                onSubmit = { name, amount, categoryId, description ->
                    onCreateTransaction(name, amount, categoryId, description)
                    showingAddTransaction = false
                }
            )
        }

        // Payoff Simulation
        if (showingSimulation) {
            com.casha.app.ui.feature.liability.forminput.PayoffSimulationView(
                liabilityState = liabilityState,
                userCurrency = userCurrency,
                onDismissRequest = { showingSimulation = false },
                onSubmit = onSimulatePayoff
            )
        }

        // Payment History
        if (showingPaymentHistory) {
            com.casha.app.ui.feature.liability.subviews.detail.PaymentHistoryListView(
                payments = liabilityState.paymentHistory,
                liabilityName = liability.name,
                userCurrency = userCurrency,
                onDismissRequest = { showingPaymentHistory = false }
            )
        }
    }
}
