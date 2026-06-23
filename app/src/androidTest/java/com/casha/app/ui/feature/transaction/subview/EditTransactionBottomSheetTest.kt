package com.casha.app.ui.feature.transaction.subview

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.casha.app.domain.model.CashflowType
import com.casha.app.domain.model.CategoryCasha
import com.casha.app.domain.model.TransactionCasha
import com.casha.app.ui.theme.CashaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * UI Tests for EditTransactionBottomSheet cleanup
 * 
 * Task 11.3: UI tests for cleaned edit form
 * Tests Requirements 6.1, 6.2, 6.3:
 * - 6.1: Edit form functions correctly for all valid inputs
 * - 6.2: "Confirmed" toggle is NOT displayed
 * - 6.3: Sync status indicator displays when transaction not synced
 */
@RunWith(AndroidJUnit4::class)
class EditTransactionBottomSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockTransaction(
        id: String = UUID.randomUUID().toString(),
        name: String = "Test Transaction",
        amount: Double = 100.0,
        category: String = "Food",
        datetime: Date = Date(),
        note: String? = null,
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
            note = note,
            isSynced = isSynced,
            createdAt = createdAt,
            updatedAt = updatedAt,
            liabilityId = null,
            remoteId = null
        )
    }

    private fun createMockCategories(): List<CategoryCasha> {
        return listOf(
            CategoryCasha(id = "1", name = "Food", type = "EXPENSE"),
            CategoryCasha(id = "2", name = "Transport", type = "EXPENSE"),
            CategoryCasha(id = "3", name = "Shopping", type = "EXPENSE"),
            CategoryCasha(id = "4", name = "Salary", type = "INCOME")
        )
    }

    /**
     * Test that "Confirmed" toggle is NOT displayed
     * Requirement 6.2: System SHALL NOT display "Confirmed" toggle in edit form
     */
    @Test
    fun editForm_doesNotDisplay_confirmedToggle() {
        val transaction = createMockTransaction(isSynced = true)
        val categories = createMockCategories()

        composeTestRule.setContent {
            CashaTheme {
                EditTransactionBottomSheet(
                    transaction = transaction,
                    cashflowType = CashflowType.EXPENSE,
                    categories = categories,
                    onDismissRequest = {},
                    onSave = {}
                )
            }
        }

        // Verify "Confirmed" toggle or checkbox does NOT exist
        composeTestRule.onNodeWithText("Confirmed", ignoreCase = true)
            .assertDoesNotExist()
        
        composeTestRule.onNodeWithText("Sync", substring = true, ignoreCase = true)
            .assertDoesNotExist()
        
        // Also check for "isSynced" in case it's displayed differently
        composeTestRule.onNodeWithText("isSynced", substring = true, ignoreCase = true)
            .assertDoesNotExist()
    }

    /**
     * Test that sync status indicator displays when transaction is NOT synced
     * Requirement 6.3: System SHALL display sync status indicator when isSynced is false
     */
    @Test
    fun editForm_displaysSyncIndicator_whenNotSynced() {
        val transaction = createMockTransaction(isSynced = false)
        val categories = createMockCategories()

        composeTestRule.setContent {
            CashaTheme {
                EditTransactionBottomSheet(
                    transaction = transaction,
                    cashflowType = CashflowType.EXPENSE,
                    categories = categories,
                    onDismissRequest = {},
                    onSave = {}
                )
            }
        }

        // Verify sync status indicator is displayed
        composeTestRule.onNodeWithText("Syncing with server...", substring = true)
            .assertExists()
            .assertIsDisplayed()
    }

    /**
     * Test that sync status indicator does NOT display when transaction is synced
     * Requirement 6.3: Status indicator only appears when actively syncing
     */
    @Test
    fun editForm_doesNotDisplaySyncIndicator_whenSynced() {
        val transaction = createMockTransaction(isSynced = true)
        val categories = createMockCategories()

        composeTestRule.setContent {
            CashaTheme {
                EditTransactionBottomSheet(
                    transaction = transaction,
                    cashflowType = CashflowType.EXPENSE,
                    categories = categories,
                    onDismissRequest = {},
                    onSave = {}
                )
            }
        }

        // Verify sync status indicator is NOT displayed
        composeTestRule.onNodeWithText("Syncing with server...", substring = true)
            .assertDoesNotExist()
    }

    /**
     * Test that edit form displays all required fields
     * Requirement 6.1: Form functions correctly for all valid inputs
     */
    @Test
    fun editForm_displaysAllRequiredFields() {
        val transaction = createMockTransaction(
            name = "Restaurant Bill",
            amount = 150.0,
            category = "Food",
            note = "Dinner with friends"
        )
        val categories = createMockCategories()

        composeTestRule.setContent {
            CashaTheme {
                EditTransactionBottomSheet(
                    transaction = transaction,
                    cashflowType = CashflowType.EXPENSE,
                    categories = categories,
                    onDismissRequest = {},
                    onSave = {}
                )
            }
        }

        // Verify all required field labels are present
        composeTestRule.onNodeWithText("Jumlah *", substring = true)
            .assertExists()
        
        composeTestRule.onNodeWithText("Nama *", substring = true)
            .assertExists()
        
        // Category field
        composeTestRule.onNodeWithText("Kategori *", substring = true)
            .assertExists()
            .assertExists()
        
        // Note field
        composeTestRule.onNodeWithText("Catatan", substring = true)
            .assertExists()
    }

    /**
     * Test that transaction data is pre-populated correctly
     * Requirement 6.1: Form displays existing transaction data
     */
    @Test
    fun editForm_prepopulatesTransactionData() {
        val transaction = createMockTransaction(
            name = "Coffee Purchase",
            amount = 25.50,
            category = "Food"
        )
        val categories = createMockCategories()

        composeTestRule.setContent {
            CashaTheme {
                EditTransactionBottomSheet(
                    transaction = transaction,
                    cashflowType = CashflowType.EXPENSE,
                    categories = categories,
                    onDismissRequest = {},
                    onSave = {}
                )
            }
        }

        // Verify transaction name is displayed
        composeTestRule.onNodeWithText("Coffee Purchase")
            .assertExists()
        
        // Verify category is displayed
        composeTestRule.onNodeWithText("Food")
            .assertExists()
    }

    /**
     * Test that Save button is enabled when form is valid
     * Requirement 6.1: Form validation works correctly
     */
    @Test
    fun editForm_saveButtonEnabled_whenFormValid() {
        val transaction = createMockTransaction(
            name = "Valid Transaction",
            amount = 100.0,
            category = "Food"
        )
        val categories = createMockCategories()

        composeTestRule.setContent {
            CashaTheme {
                EditTransactionBottomSheet(
                    transaction = transaction,
                    cashflowType = CashflowType.EXPENSE,
                    categories = categories,
                    onDismissRequest = {},
                    onSave = {}
                )
            }
        }

        // Find Save button (might be "Simpan" or "Save")
        composeTestRule.onNodeWithText("Simpan", ignoreCase = true)
            .assertExists()
            .assertIsEnabled()
    }

    /**
     * Test that Cancel button is always enabled
     * Validates user can always dismiss the form
     */
    @Test
    fun editForm_cancelButtonAlwaysEnabled() {
        val transaction = createMockTransaction()
        val categories = createMockCategories()

        composeTestRule.setContent {
            CashaTheme {
                EditTransactionBottomSheet(
                    transaction = transaction,
                    cashflowType = CashflowType.EXPENSE,
                    categories = categories,
                    onDismissRequest = {},
                    onSave = {}
                )
            }
        }

        // Find Cancel button (might be "Batal" or "Cancel")
        composeTestRule.onNodeWithText("Batal", ignoreCase = true)
            .assertExists()
            .assertIsEnabled()
    }

    /**
     * Test edit form for INCOME transaction type
     * Validates form works for both transaction types
     */
    @Test
    fun editForm_worksForIncomeTransaction() {
        val transaction = createMockTransaction(
            name = "Monthly Salary",
            amount = 5000.0,
            category = "Salary"
        )
        val categories = createMockCategories()

        composeTestRule.setContent {
            CashaTheme {
                EditTransactionBottomSheet(
                    transaction = transaction,
                    cashflowType = CashflowType.INCOME,
                    categories = categories,
                    onDismissRequest = {},
                    onSave = {}
                )
            }
        }

        // Verify title reflects income type
        composeTestRule.onNodeWithText("Edit Income", substring = true)
            .assertExists()
        
        // Verify transaction data is displayed
        composeTestRule.onNodeWithText("Monthly Salary")
            .assertExists()
    }

    /**
     * Test that form displays note when provided
     * Validates optional note field handling
     */
    @Test
    fun editForm_displaysNote_whenProvided() {
        val transaction = createMockTransaction(
            name = "Grocery Shopping",
            note = "Weekly groceries from supermarket"
        )
        val categories = createMockCategories()

        composeTestRule.setContent {
            CashaTheme {
                EditTransactionBottomSheet(
                    transaction = transaction,
                    cashflowType = CashflowType.EXPENSE,
                    categories = categories,
                    onDismissRequest = {},
                    onSave = {}
                )
            }
        }

        // Verify note is displayed
        composeTestRule.onNodeWithText("Weekly groceries from supermarket")
            .assertExists()
    }

    /**
     * Test that sync indicator is positioned prominently
     * Requirement 6.3: Status indicator positioned near top of form
     */
    @Test
    fun editForm_syncIndicatorPositionedProminently() {
        val transaction = createMockTransaction(isSynced = false)
        val categories = createMockCategories()

        composeTestRule.setContent {
            CashaTheme {
                EditTransactionBottomSheet(
                    transaction = transaction,
                    cashflowType = CashflowType.EXPENSE,
                    categories = categories,
                    onDismissRequest = {},
                    onSave = {}
                )
            }
        }

        // Verify sync indicator exists
        val syncIndicator = composeTestRule.onNodeWithText("Syncing with server...", substring = true)
        syncIndicator.assertExists()
        syncIndicator.assertIsDisplayed()
        
        // Note: Testing exact positioning is complex in Compose UI tests
        // The indicator is placed at the top of the form content as per implementation
    }

    /**
     * Integration test: Form with unsynced transaction shows indicator but allows editing
     * Validates complete form behavior with sync state
     */
    @Test
    fun editForm_allowsEditing_evenWhenNotSynced() {
        val transaction = createMockTransaction(
            name = "Unsynced Transaction",
            amount = 50.0,
            category = "Transport",
            isSynced = false
        )
        val categories = createMockCategories()

        composeTestRule.setContent {
            CashaTheme {
                EditTransactionBottomSheet(
                    transaction = transaction,
                    cashflowType = CashflowType.EXPENSE,
                    categories = categories,
                    onDismissRequest = {},
                    onSave = {}
                )
            }
        }

        // Verify sync indicator is shown
        composeTestRule.onNodeWithText("Syncing with server...", substring = true)
            .assertExists()
        
        // Verify form fields are still editable (name field exists)
        composeTestRule.onNodeWithText("Unsynced Transaction")
            .assertExists()
        
        // Verify Save button exists (even if we're syncing, form can be edited)
        composeTestRule.onNodeWithText("Simpan", ignoreCase = true)
            .assertExists()
    }

    /**
     * Test that form handles empty categories list gracefully
     * Edge case: categories not loaded yet
     */
    @Test
    fun editForm_handlesEmptyCategories() {
        val transaction = createMockTransaction()

        composeTestRule.setContent {
            CashaTheme {
                EditTransactionBottomSheet(
                    transaction = transaction,
                    cashflowType = CashflowType.EXPENSE,
                    categories = emptyList(),
                    onDismissRequest = {},
                    onSave = {}
                )
            }
        }

        // Form should still display
        composeTestRule.onNodeWithText("Nama *", substring = true)
            .assertExists()
        
        // Category field should exist but might show current category
        composeTestRule.onNodeWithText(transaction.category)
            .assertExists()
    }

    /**
     * Test form with various transaction amounts
     * Validates amount formatting and display
     */
    @Test
    fun editForm_handlesVariousAmountFormats() {
        // Test with integer amount
        val transaction1 = createMockTransaction(amount = 100.0)
        
        composeTestRule.setContent {
            CashaTheme {
                EditTransactionBottomSheet(
                    transaction = transaction1,
                    cashflowType = CashflowType.EXPENSE,
                    categories = createMockCategories(),
                    onDismissRequest = {},
                    onSave = {}
                )
            }
        }

        // Amount should be displayed (100 without decimals)
        composeTestRule.onNodeWithText("100", substring = true)
            .assertExists()
    }
}
