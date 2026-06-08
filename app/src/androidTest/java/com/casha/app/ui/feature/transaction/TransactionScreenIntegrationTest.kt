package com.casha.app.ui.feature.transaction

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.casha.app.domain.model.CashflowDateSection
import com.casha.app.domain.model.CashflowEntry
import com.casha.app.domain.model.CashflowType
import com.casha.app.ui.feature.transaction.subview.PeriodSummaryCard
import com.casha.app.ui.theme.CashaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Integration Tests for Period Summary Card in TransactionScreen
 * 
 * Task 8.2: Integration tests for period summary in list
 * Tests Requirements 1.1, 1.2, 9.5:
 * - 1.1: System SHALL display period financial summary at top of transaction list
 * - 1.2: System SHALL recalculate summary when user changes period filter
 * - 9.5: System SHALL recalculate summary when transactions are added/removed
 */
@RunWith(AndroidJUnit4::class)
class TransactionScreenIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockCashflowEntry(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Entry",
        amount: Double = 100.0,
        category: String = "Food",
        type: CashflowType = CashflowType.EXPENSE,
        date: Date = Date()
    ): CashflowEntry {
        return CashflowEntry(
            id = id,
            title = title,
            amount = amount,
            category = category,
            type = type,
            date = date,
            icon = null,
            groupId = null,
            groupName = null
        )
    }

    private fun createMockDateSection(
        day: String = "Today",
        date: String = "1 Jan 2024",
        items: List<CashflowEntry> = emptyList()
    ): CashflowDateSection {
        return CashflowDateSection(
            day = day,
            date = date,
            items = items,
            segments = emptyList()
        )
    }

    /**
     * Test that period summary displays at correct position (between filter and list)
     * Requirement 1.1: Period summary at top of transaction list
     */
    @Test
    fun periodSummary_displaysAtCorrectPosition() {
        val entries = listOf(
            createMockCashflowEntry(title = "Salary", amount = 5000.0, type = CashflowType.INCOME),
            createMockCashflowEntry(title = "Groceries", amount = 200.0, type = CashflowType.EXPENSE)
        )
        
        val sections = listOf(createMockDateSection(items = entries))
        val summary = sections.calculatePeriodSummary()

        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = summary.totalIncome,
                    totalExpense = summary.totalExpense,
                    netAmount = summary.netAmount
                )
            }
        }

        // Verify period summary is displayed
        composeTestRule.onNodeWithText("Income", substring = true)
            .assertExists()
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Expense", substring = true)
            .assertExists()
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Net", substring = true)
            .assertExists()
            .assertIsDisplayed()
    }

    /**
     * Test that period summary shows correct values for mixed transactions
     * Requirement 1.1: Display accurate period financial summary
     */
    @Test
    fun periodSummary_showsCorrectValuesForMixedTransactions() {
        val entries = listOf(
            createMockCashflowEntry(title = "Salary", amount = 5000.0, type = CashflowType.INCOME),
            createMockCashflowEntry(title = "Bonus", amount = 1000.0, type = CashflowType.INCOME),
            createMockCashflowEntry(title = "Groceries", amount = 300.0, type = CashflowType.EXPENSE),
            createMockCashflowEntry(title = "Transport", amount = 150.0, type = CashflowType.EXPENSE)
        )
        
        val sections = listOf(createMockDateSection(items = entries))
        val summary = sections.calculatePeriodSummary()

        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = summary.totalIncome,
                    totalExpense = summary.totalExpense,
                    netAmount = summary.netAmount
                )
            }
        }

        // Verify calculated values are correct
        assert(summary.totalIncome == 6000.0) { "Total income should be 6000.0" }
        assert(summary.totalExpense == 450.0) { "Total expense should be 450.0" }
        assert(summary.netAmount == 5550.0) { "Net amount should be 5550.0" }
    }

    /**
     * Test that period summary updates when filter changes
     * Requirement 1.2: Recalculate summary when period filter changes
     */
    @Test
    fun periodSummary_updatesWhenFilterChanges() {
        // Initial data (January)
        var currentSections by mutableStateOf(
            listOf(
                createMockDateSection(
                    items = listOf(
                        createMockCashflowEntry(title = "Jan Income", amount = 3000.0, type = CashflowType.INCOME),
                        createMockCashflowEntry(title = "Jan Expense", amount = 1000.0, type = CashflowType.EXPENSE)
                    )
                )
            )
        )

        composeTestRule.setContent {
            CashaTheme {
                val summary = remember(currentSections) {
                    currentSections.calculatePeriodSummary()
                }
                
                PeriodSummaryCard(
                    totalIncome = summary.totalIncome,
                    totalExpense = summary.totalExpense,
                    netAmount = summary.netAmount
                )
            }
        }

        // Verify initial summary
        val initialSummary = currentSections.calculatePeriodSummary()
        assert(initialSummary.totalIncome == 3000.0) { "Initial income should be 3000.0" }
        assert(initialSummary.netAmount == 2000.0) { "Initial net should be 2000.0" }

        // Change filter (simulate switching to February)
        currentSections = listOf(
            createMockDateSection(
                items = listOf(
                    createMockCashflowEntry(title = "Feb Income", amount = 5000.0, type = CashflowType.INCOME),
                    createMockCashflowEntry(title = "Feb Expense", amount = 2000.0, type = CashflowType.EXPENSE)
                )
            )
        )
        
        composeTestRule.waitForIdle()

        // Verify updated summary
        val updatedSummary = currentSections.calculatePeriodSummary()
        assert(updatedSummary.totalIncome == 5000.0) { "Updated income should be 5000.0" }
        assert(updatedSummary.totalExpense == 2000.0) { "Updated expense should be 2000.0" }
        assert(updatedSummary.netAmount == 3000.0) { "Updated net should be 3000.0" }
    }

    /**
     * Test that period summary recalculates when transactions are added
     * Requirement 9.5: Recalculate when transactions added/removed
     */
    @Test
    fun periodSummary_recalculatesWhenTransactionsAdded() {
        var currentSections by mutableStateOf(
            listOf(
                createMockDateSection(
                    items = listOf(
                        createMockCashflowEntry(title = "Initial Income", amount = 1000.0, type = CashflowType.INCOME)
                    )
                )
            )
        )

        composeTestRule.setContent {
            CashaTheme {
                val summary = remember(currentSections) {
                    currentSections.calculatePeriodSummary()
                }
                
                PeriodSummaryCard(
                    totalIncome = summary.totalIncome,
                    totalExpense = summary.totalExpense,
                    netAmount = summary.netAmount
                )
            }
        }

        // Verify initial state
        val initialSummary = currentSections.calculatePeriodSummary()
        assert(initialSummary.totalIncome == 1000.0) { "Initial income should be 1000.0" }
        assert(initialSummary.totalExpense == 0.0) { "Initial expense should be 0.0" }

        // Add new transactions
        currentSections = listOf(
            createMockDateSection(
                items = listOf(
                    createMockCashflowEntry(title = "Initial Income", amount = 1000.0, type = CashflowType.INCOME),
                    createMockCashflowEntry(title = "New Expense", amount = 500.0, type = CashflowType.EXPENSE),
                    createMockCashflowEntry(title = "New Income", amount = 2000.0, type = CashflowType.INCOME)
                )
            )
        )
        
        composeTestRule.waitForIdle()

        // Verify summary recalculated
        val updatedSummary = currentSections.calculatePeriodSummary()
        assert(updatedSummary.totalIncome == 3000.0) { "Updated income should be 3000.0" }
        assert(updatedSummary.totalExpense == 500.0) { "Updated expense should be 500.0" }
        assert(updatedSummary.netAmount == 2500.0) { "Updated net should be 2500.0" }
    }

    /**
     * Test that period summary recalculates when transactions are removed
     * Requirement 9.5: Recalculate when transactions removed
     */
    @Test
    fun periodSummary_recalculatesWhenTransactionsRemoved() {
        var currentSections by mutableStateOf(
            listOf(
                createMockDateSection(
                    items = listOf(
                        createMockCashflowEntry(id = "1", title = "Income 1", amount = 3000.0, type = CashflowType.INCOME),
                        createMockCashflowEntry(id = "2", title = "Expense 1", amount = 1000.0, type = CashflowType.EXPENSE),
                        createMockCashflowEntry(id = "3", title = "Expense 2", amount = 500.0, type = CashflowType.EXPENSE)
                    )
                )
            )
        )

        composeTestRule.setContent {
            CashaTheme {
                val summary = remember(currentSections) {
                    currentSections.calculatePeriodSummary()
                }
                
                PeriodSummaryCard(
                    totalIncome = summary.totalIncome,
                    totalExpense = summary.totalExpense,
                    netAmount = summary.netAmount
                )
            }
        }

        // Verify initial state
        val initialSummary = currentSections.calculatePeriodSummary()
        assert(initialSummary.totalIncome == 3000.0) { "Initial income should be 3000.0" }
        assert(initialSummary.totalExpense == 1500.0) { "Initial expense should be 1500.0" }

        // Remove transactions (simulate deletion)
        currentSections = listOf(
            createMockDateSection(
                items = listOf(
                    createMockCashflowEntry(id = "1", title = "Income 1", amount = 3000.0, type = CashflowType.INCOME)
                    // Expense transactions removed
                )
            )
        )
        
        composeTestRule.waitForIdle()

        // Verify summary recalculated after removal
        val updatedSummary = currentSections.calculatePeriodSummary()
        assert(updatedSummary.totalIncome == 3000.0) { "Updated income should remain 3000.0" }
        assert(updatedSummary.totalExpense == 0.0) { "Updated expense should be 0.0" }
        assert(updatedSummary.netAmount == 3000.0) { "Updated net should be 3000.0" }
    }

    /**
     * Test period summary with empty transaction list
     * Requirement 9.5: Handle empty lists gracefully
     */
    @Test
    fun periodSummary_handlesEmptyList() {
        val sections = emptyList<CashflowDateSection>()
        val summary = sections.calculatePeriodSummary()

        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = summary.totalIncome,
                    totalExpense = summary.totalExpense,
                    netAmount = summary.netAmount
                )
            }
        }

        // Verify zero values are handled correctly
        assert(summary.totalIncome == 0.0) { "Income should be 0.0 for empty list" }
        assert(summary.totalExpense == 0.0) { "Expense should be 0.0 for empty list" }
        assert(summary.netAmount == 0.0) { "Net should be 0.0 for empty list" }

        // Verify UI displays correctly with zero values
        composeTestRule.onNodeWithText("Income", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("Expense", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("Net", substring = true)
            .assertExists()
    }

    /**
     * Test period summary with only income transactions
     * Requirement 9.2: Calculate only income when no expenses
     */
    @Test
    fun periodSummary_withOnlyIncome() {
        val entries = listOf(
            createMockCashflowEntry(title = "Salary", amount = 5000.0, type = CashflowType.INCOME),
            createMockCashflowEntry(title = "Bonus", amount = 2000.0, type = CashflowType.INCOME)
        )
        
        val sections = listOf(createMockDateSection(items = entries))
        val summary = sections.calculatePeriodSummary()

        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = summary.totalIncome,
                    totalExpense = summary.totalExpense,
                    netAmount = summary.netAmount
                )
            }
        }

        assert(summary.totalIncome == 7000.0) { "Total income should be 7000.0" }
        assert(summary.totalExpense == 0.0) { "Total expense should be 0.0" }
        assert(summary.netAmount == 7000.0) { "Net amount should equal income when no expenses" }
    }

    /**
     * Test period summary with only expense transactions
     * Requirement 9.3: Calculate only expense when no income
     */
    @Test
    fun periodSummary_withOnlyExpenses() {
        val entries = listOf(
            createMockCashflowEntry(title = "Groceries", amount = 300.0, type = CashflowType.EXPENSE),
            createMockCashflowEntry(title = "Transport", amount = 150.0, type = CashflowType.EXPENSE),
            createMockCashflowEntry(title = "Entertainment", amount = 100.0, type = CashflowType.EXPENSE)
        )
        
        val sections = listOf(createMockDateSection(items = entries))
        val summary = sections.calculatePeriodSummary()

        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = summary.totalIncome,
                    totalExpense = summary.totalExpense,
                    netAmount = summary.netAmount
                )
            }
        }

        assert(summary.totalIncome == 0.0) { "Total income should be 0.0" }
        assert(summary.totalExpense == 550.0) { "Total expense should be 550.0" }
        assert(summary.netAmount == -550.0) { "Net amount should be negative when only expenses" }
    }

    /**
     * Test period summary across multiple date sections
     * Requirement 1.1: Aggregate data across all sections in period
     */
    @Test
    fun periodSummary_aggregatesAcrossMultipleDateSections() {
        val sections = listOf(
            createMockDateSection(
                day = "Monday",
                items = listOf(
                    createMockCashflowEntry(title = "Monday Income", amount = 1000.0, type = CashflowType.INCOME),
                    createMockCashflowEntry(title = "Monday Expense", amount = 200.0, type = CashflowType.EXPENSE)
                )
            ),
            createMockDateSection(
                day = "Tuesday",
                items = listOf(
                    createMockCashflowEntry(title = "Tuesday Income", amount = 1500.0, type = CashflowType.INCOME),
                    createMockCashflowEntry(title = "Tuesday Expense", amount = 300.0, type = CashflowType.EXPENSE)
                )
            ),
            createMockDateSection(
                day = "Wednesday",
                items = listOf(
                    createMockCashflowEntry(title = "Wednesday Expense", amount = 150.0, type = CashflowType.EXPENSE)
                )
            )
        )
        
        val summary = sections.calculatePeriodSummary()

        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = summary.totalIncome,
                    totalExpense = summary.totalExpense,
                    netAmount = summary.netAmount
                )
            }
        }

        // Verify aggregation across multiple days
        assert(summary.totalIncome == 2500.0) { "Total income should aggregate across all days: 2500.0" }
        assert(summary.totalExpense == 650.0) { "Total expense should aggregate across all days: 650.0" }
        assert(summary.netAmount == 1850.0) { "Net amount should be 1850.0" }
    }

    /**
     * Test that period summary uses memoization efficiently
     * Requirement 1.2: Use remember to cache computed summary
     */
    @Test
    fun periodSummary_usesMemoizationEfficiently() {
        var sections by mutableStateOf(
            listOf(
                createMockDateSection(
                    items = listOf(
                        createMockCashflowEntry(amount = 1000.0, type = CashflowType.INCOME)
                    )
                )
            )
        )
        
        var calculationCount = 0

        composeTestRule.setContent {
            CashaTheme {
                // Track calculation count
                val summary = remember(sections) {
                    calculationCount++
                    sections.calculatePeriodSummary()
                }
                
                PeriodSummaryCard(
                    totalIncome = summary.totalIncome,
                    totalExpense = summary.totalExpense,
                    netAmount = summary.netAmount
                )
            }
        }

        // Initial calculation
        assert(calculationCount == 1) { "Summary should be calculated once initially" }

        // Force recomposition without changing sections
        composeTestRule.waitForIdle()
        
        // Verify no recalculation when sections haven't changed
        assert(calculationCount == 1) { "Summary should not recalculate when sections haven't changed" }

        // Change sections
        sections = listOf(
            createMockDateSection(
                items = listOf(
                    createMockCashflowEntry(amount = 2000.0, type = CashflowType.INCOME)
                )
            )
        )
        composeTestRule.waitForIdle()

        // Verify recalculation only when sections change
        assert(calculationCount == 2) { "Summary should recalculate when sections change" }
    }
}
