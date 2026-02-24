package com.casha.app.ui.feature.liability

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.domain.model.Liability
import com.casha.app.domain.model.LiabilityCategory
import com.casha.app.domain.model.LiabilityStatement
import com.casha.app.domain.model.PaymentType
import com.casha.app.ui.feature.liability.subviews.LiabilityBalanceCardView
import com.casha.app.ui.feature.liability.subviews.LiabilityCreditCardSectionsView
import com.casha.app.ui.feature.liability.subviews.LiabilityInfoDetailsView
import com.casha.app.ui.feature.liability.subviews.LiabilityQuickActionsView
import com.casha.app.core.util.CurrencyFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiabilityDetailView(
    initialLiability: Liability,
    liabilityState: LiabilityState,
    onBack: () -> Unit,
    onRecordPayment: (Double, PaymentType) -> Unit,
    onAddTransaction: () -> Unit,
    onStatementClick: (LiabilityStatement) -> Unit
) {
    val liability = liabilityState.liabilities.firstOrNull { it.id == initialLiability.id } ?: initialLiability
    val userCurrency = CurrencyFormatter.defaultCurrency
    val scrollState = rememberScrollState()

    var showingRecordPayment by remember { mutableStateOf(false) }
    var paymentAmountValue by remember { mutableDoubleStateOf(0.0) }
    var paymentType by remember { mutableStateOf(PaymentType.PARTIAL) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA) // Premium off-white app background
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
                        imageVector = Icons.Default.ArrowBack,
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
            // Balance Card
            LiabilityBalanceCardView(
                liability = liability,
                latestStatement = liabilityState.latestStatement,
                userCurrency = userCurrency
            )

            // Quick Actions
            LiabilityQuickActionsView(
                onRecordPayment = { showingRecordPayment = true },
                onAddTransaction = onAddTransaction
            )

            // Liability Details
            LiabilityInfoDetailsView(
                liability = liability,
                userCurrency = userCurrency
            )

            // Payment History (only for non-credit card liabilities)
            if (liability.category != LiabilityCategory.CREDIT_CARD) {
                // TODO: Payment History Section for non-credit cards
            }

            // Credit Card Specific Sections
            if (liability.category == LiabilityCategory.CREDIT_CARD) {
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
            }
            
            Spacer(modifier = Modifier.height(84.dp))
        }

        // Action Sheets can be implemented using Modals/Dialogs here based on showingRecordPayment state
        if (showingRecordPayment) {
            // Note: In real Android implementation, BottomSheet or Dialog is used here
            // onRecordPayment(paymentAmountValue, paymentType)
        }
    }
}
