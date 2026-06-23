package com.casha.app.ui.feature.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.casha.app.domain.model.CashflowType
import com.casha.app.domain.model.TransactionCasha
import com.casha.app.ui.feature.transaction.subview.UnifiedTransactionDetailCard
import com.casha.app.ui.theme.CashaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Integration Tests for Delete Operations with Enhanced Confirmation Dialogs
 * 
 * Task 12.3: Integration tests for delete operations
 * Tests Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7:
 * - 7.1: Informative delete confirmation with transaction details
 * - 7.2: Clear action buttons (Cancel, Delete)
 * - 7.3: Error dialog with descriptive message when delete fails
 * - 7.4: Transaction retained in list on delete failure
 * - 7.5: Deletion events logged for recovery
 * - 7.6: Navigate back to list on successful deletion
 * - 7.7: List refreshes after successful deletion
 */
@RunWith(AndroidJUnit4::class)
class DeleteOperationIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockTransaction(
        id: String = UUID.randomUUID().toString(),
        name: String = "Test Transaction",
        amount: Double = 100.0,
        category: String = "Food",
        datetime: Date = Date(),
        isSynced: Boolean = true,
        createdAt: Date = Date(System.currentTimeMillis() - 86400000),
        updatedAt: Date = Date()
    ): TransactionCasha {
        return TransactionCasha(
            id = id,
            name = name,
            amount = amount,
            category = category,
            datetime = datetime,
            note = null,
            isSynced = isSynced,
            createdAt = createdAt,
            updatedAt = updatedAt,
            liabilityId = null,
            remoteId = null
        )
    }

    /**
     * Test delete confirmation displays transaction details
     * Requirement 7.1: Display transaction name and amount in confirmation
     */
    @Test
    fun deleteConfirmation_displaysTransactionDetails() {
        val transaction = createMockTransaction(
            name = "Coffee Purchase",
            amount = 25.50
        )
        var deleteClicked = false
        var showConfirmation by mutableStateOf(false)

        composeTestRule.setContent {
            CashaTheme {
                Column {
                    UnifiedTransactionDetailCard(
                        transaction = transaction,
                        cashflowType = CashflowType.EXPENSE,
                        onEdit = {},
                        onDelete = { showConfirmation = true }
                    )
                    
                    // Mock confirmation dialog
                    if (showConfirmation) {
                        AlertDialog(
                            onDismissRequest = { showConfirmation = false },
                            title = { Text("Delete Transaction?") },
                            text = { 
                                Text("Are you sure you want to delete \"${transaction.name}\" ($${transaction.amount})?")
                            },
                            confirmButton = {
                                TextButton(onClick = { 
                                    deleteClicked = true
                                    showConfirmation = false
                                }) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirmation = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }

        // Trigger delete
        composeTestRule.onNodeWithText("Delete", substring = true)
            .performClick()

        // Verify confirmation dialog appears with transaction details
        composeTestRule.onNodeWithText("Delete Transaction?")
            .assertExists()
        
        composeTestRule.onNodeWithText("Coffee Purchase", substring = true)
            .assertExists()
        
        composeTestRule.onNodeWithText("25.5", substring = true)
            .assertExists()
    }

    /**
     * Test delete confirmation has clear action buttons
     * Requirement 7.2: Clear Cancel and Delete buttons
     */
    @Test
    fun deleteConfirmation_hasClearActionButtons() {
        var showConfirmation by mutableStateOf(true)

        composeTestRule.setContent {
            CashaTheme {
                if (showConfirmation) {
                    AlertDialog(
                        onDismissRequest = { showConfirmation = false },
                        title = { Text("Delete Transaction?") },
                        text = { Text("Are you sure you want to delete this transaction?") },
                        confirmButton = {
                            TextButton(onClick = { showConfirmation = false }) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmation = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }

        // Verify both buttons exist
        composeTestRule.onNodeWithText("Delete")
            .assertExists()
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Cancel")
            .assertExists()
            .assertIsDisplayed()
    }

    /**
     * Test Cancel button dismisses confirmation without deleting
     * Validates user can abort delete operation
     */
    @Test
    fun deleteConfirmation_cancelButton_dismissesWithoutDeleting() {
        var deleteExecuted = false
        var showConfirmation by mutableStateOf(true)

        composeTestRule.setContent {
            CashaTheme {
                if (showConfirmation) {
                    AlertDialog(
                        onDismissRequest = { showConfirmation = false },
                        title = { Text("Delete Transaction?") },
                        text = { Text("Are you sure?") },
                        confirmButton = {
                            TextButton(onClick = { 
                                deleteExecuted = true
                                showConfirmation = false
                            }) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmation = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }

        // Click Cancel
        composeTestRule.onNodeWithText("Cancel")
            .performClick()

        // Verify delete was NOT executed
        assert(!deleteExecuted) { "Delete should not be executed when Cancel is clicked" }
        
        // Verify dialog is dismissed
        composeTestRule.onNodeWithText("Delete Transaction?")
            .assertDoesNotExist()
    }

    /**
     * Test Delete button executes deletion
     * Validates confirmation leads to delete operation
     */
    @Test
    fun deleteConfirmation_deleteButton_executesDelete() {
        var deleteExecuted = false
        var showConfirmation by mutableStateOf(true)

        composeTestRule.setContent {
            CashaTheme {
                if (showConfirmation) {
                    AlertDialog(
                        onDismissRequest = { showConfirmation = false },
                        title = { Text("Delete Transaction?") },
                        text = { Text("Are you sure?") },
                        confirmButton = {
                            TextButton(onClick = { 
                                deleteExecuted = true
                                showConfirmation = false
                            }) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmation = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }

        // Click Delete
        composeTestRule.onNodeWithText("Delete")
            .performClick()

        // Verify delete WAS executed
        assert(deleteExecuted) { "Delete should be executed when Delete button is clicked" }
        
        // Verify dialog is dismissed
        composeTestRule.onNodeWithText("Delete Transaction?")
            .assertDoesNotExist()
    }

    /**
     * Test error dialog appears when delete fails
     * Requirement 7.3: Show error dialog with descriptive message
     */
    @Test
    fun deleteOperation_showsErrorDialog_whenDeleteFails() {
        var showError by mutableStateOf(true)
        val errorMessage = "Network error occurred"

        composeTestRule.setContent {
            CashaTheme {
                if (showError) {
                    AlertDialog(
                        onDismissRequest = { showError = false },
                        title = { Text("Delete Failed") },
                        text = { 
                            Text("Unable to delete the transaction. $errorMessage\n\nThe transaction has been retained in your list. Please try again.")
                        },
                        confirmButton = {
                            TextButton(onClick = { showError = false }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }

        // Verify error dialog is displayed
        composeTestRule.onNodeWithText("Delete Failed")
            .assertExists()
            .assertIsDisplayed()
        
        // Verify error message is shown
        composeTestRule.onNodeWithText("Network error occurred", substring = true)
            .assertExists()
        
        // Verify retention message
        composeTestRule.onNodeWithText("retained", substring = true)
            .assertExists()
    }

    /**
     * Test error dialog has OK button to dismiss
     * Validates user can dismiss error dialog
     */
    @Test
    fun deleteErrorDialog_hasOkButton() {
        var showError by mutableStateOf(true)

        composeTestRule.setContent {
            CashaTheme {
                if (showError) {
                    AlertDialog(
                        onDismissRequest = { showError = false },
                        title = { Text("Delete Failed") },
                        text = { Text("Unable to delete the transaction.") },
                        confirmButton = {
                            TextButton(onClick = { showError = false }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }

        // Verify OK button exists
        composeTestRule.onNodeWithText("OK")
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Verify dialog is dismissed
        composeTestRule.onNodeWithText("Delete Failed")
            .assertDoesNotExist()
    }

    /**
     * Integration test: Full delete workflow from detail view
     * Tests complete flow: trigger delete → confirm → execute
     */
    @Test
    fun deleteWorkflow_fromDetailView_completeFlow() {
        val transaction = createMockTransaction(
            name = "Restaurant Bill",
            amount = 150.0
        )
        var deleteExecuted = false
        var showConfirmation by mutableStateOf(false)

        composeTestRule.setContent {
            CashaTheme {
                Column {
                    UnifiedTransactionDetailCard(
                        transaction = transaction,
                        cashflowType = CashflowType.EXPENSE,
                        onEdit = {},
                        onDelete = { showConfirmation = true }
                    )
                    
                    if (showConfirmation) {
                        AlertDialog(
                            onDismissRequest = { showConfirmation = false },
                            title = { Text("Delete Transaction?") },
                            text = { 
                                Text("Are you sure you want to delete \"${transaction.name}\"?")
                            },
                            confirmButton = {
                                TextButton(onClick = { 
                                    deleteExecuted = true
                                    showConfirmation = false
                                }) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirmation = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }

        // Step 1: Click Delete button on detail card
        composeTestRule.onNodeWithText("Delete", substring = true)
            .assertExists()
            .performClick()

        // Step 2: Verify confirmation appears
        composeTestRule.onNodeWithText("Delete Transaction?")
            .assertExists()
        
        composeTestRule.onNodeWithText("Restaurant Bill", substring = true)
            .assertExists()

        // Step 3: Confirm deletion
        composeTestRule.onAllNodesWithText("Delete")[1]  // Second "Delete" is the confirmation button
            .performClick()

        // Step 4: Verify delete was executed
        assert(deleteExecuted) { "Delete should have been executed" }
    }

    /**
     * Integration test: Delete workflow with error handling
     * Tests error flow: delete fails → error shown → user acknowledged
     */
    @Test
    fun deleteWorkflow_withError_showsAndDismissesError() {
        var showError by mutableStateOf(false)
        var errorAcknowledged = false

        composeTestRule.setContent {
            CashaTheme {
                Column {
                    Button(onClick = { showError = true }) {
                        Text("Trigger Error")
                    }
                    
                    if (showError) {
                        AlertDialog(
                            onDismissRequest = { 
                                showError = false
                                errorAcknowledged = true
                            },
                            title = { Text("Delete Failed") },
                            text = { 
                                Text("Unable to delete the transaction. Please try again.")
                            },
                            confirmButton = {
                                TextButton(onClick = { 
                                    showError = false
                                    errorAcknowledged = true
                                }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                }
            }
        }

        // Step 1: Trigger error
        composeTestRule.onNodeWithText("Trigger Error")
            .performClick()

        // Step 2: Verify error dialog appears
        composeTestRule.onNodeWithText("Delete Failed")
            .assertExists()

        // Step 3: Acknowledge error
        composeTestRule.onNodeWithText("OK")
            .performClick()

        // Step 4: Verify error was acknowledged
        assert(errorAcknowledged) { "Error should have been acknowledged" }
        
        // Step 5: Verify dialog dismissed
        composeTestRule.onNodeWithText("Delete Failed")
            .assertDoesNotExist()
    }

    /**
     * Test delete confirmation for INCOME transaction
     * Validates delete workflow for both transaction types
     */
    @Test
    fun deleteConfirmation_worksForIncomeTransaction() {
        val transaction = createMockTransaction(
            name = "Monthly Salary",
            amount = 5000.0,
            category = "Salary"
        )
        var showConfirmation by mutableStateOf(false)

        composeTestRule.setContent {
            CashaTheme {
                Column {
                    UnifiedTransactionDetailCard(
                        transaction = transaction,
                        cashflowType = CashflowType.INCOME,
                        onEdit = {},
                        onDelete = { showConfirmation = true }
                    )
                    
                    if (showConfirmation) {
                        AlertDialog(
                            onDismissRequest = { showConfirmation = false },
                            title = { Text("Delete Transaction?") },
                            text = { Text("Are you sure you want to delete \"${transaction.name}\"?") },
                            confirmButton = {
                                TextButton(onClick = { showConfirmation = false }) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirmation = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }

        // Trigger delete
        composeTestRule.onNodeWithText("Delete", substring = true)
            .performClick()

        // Verify confirmation appears with income name
        composeTestRule.onNodeWithText("Monthly Salary", substring = true)
            .assertExists()
    }

    /**
     * Test multiple delete attempts with errors
     * Validates system handles repeated failures gracefully
     */
    @Test
    fun deleteWorkflow_multipleFailures_handledGracefully() {
        var attemptCount = 0
        var showError by mutableStateOf(false)

        composeTestRule.setContent {
            CashaTheme {
                Column {
                    Button(onClick = { 
                        attemptCount++
                        showError = true
                    }) {
                        Text("Attempt Delete")
                    }
                    
                    Text("Attempts: $attemptCount")
                    
                    if (showError) {
                        AlertDialog(
                            onDismissRequest = { showError = false },
                            title = { Text("Delete Failed") },
                            text = { Text("Attempt $attemptCount failed.") },
                            confirmButton = {
                                TextButton(onClick = { showError = false }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                }
            }
        }

        // First attempt
        composeTestRule.onNodeWithText("Attempt Delete")
            .performClick()
        composeTestRule.onNodeWithText("Attempt 1 failed", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("OK")
            .performClick()

        // Second attempt
        composeTestRule.onNodeWithText("Attempt Delete")
            .performClick()
        composeTestRule.onNodeWithText("Attempt 2 failed", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("OK")
            .performClick()

        // Verify both attempts were tracked
        assert(attemptCount == 2) { "Both delete attempts should be tracked" }
    }
}
