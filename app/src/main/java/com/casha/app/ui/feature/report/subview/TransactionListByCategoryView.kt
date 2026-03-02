package com.casha.app.ui.feature.report.subview

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.ui.feature.report.ReportViewModel
import com.casha.app.ui.feature.transaction.CashflowUiUtils
import com.casha.app.ui.feature.transaction.CashflowUiUtils.toCashflowEntry
import com.casha.app.ui.feature.transaction.subview.TransactionList
import androidx.compose.ui.res.stringResource
import com.casha.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListByCategoryView(
    category: String,
    onBackClick: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(category) {
        viewModel.loadTransactionsByCategory(category)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(R.string.budget_action_cancel))
                    }
                },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        val sections = remember(uiState.transactionsByCategory) {
            val entries = uiState.transactionsByCategory.map { it.toCashflowEntry() }
            CashflowUiUtils.groupTransactionsByDate(entries)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TransactionList(
                sections = sections,
                isLoading = uiState.isLoading,
                onClick = { _, _ -> /* TODO: Navigate to Detail */ }
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
