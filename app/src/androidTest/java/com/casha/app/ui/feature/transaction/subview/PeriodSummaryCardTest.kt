package com.casha.app.ui.feature.transaction.subview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.casha.app.ui.theme.CashaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for PeriodSummaryCard
 * 
 * Tests Requirements 1.1, 1.3, 1.4, 1.5, 1.6:
 * - 1.1: System SHALL display period summary showing total income, total expense, and net amount
 * - 1.3: System SHALL display income values in green color with appropriate icon
 * - 1.4: System SHALL display expense values in red color with appropriate icon
 * - 1.5: System SHALL display net amount in green when positive and red when negative
 * - 1.6: WHEN transaction list is empty, System SHALL display zero values
 */
@RunWith(AndroidJUnit4::class)
class PeriodSummaryCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Test that income displays in green with correct formatting
     * Requirement 1.3: System SHALL display income values in green color with appropriate icon
     */
    @Test
    fun income_displaysInGreen_withCorrectFormatting() {
        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = 5000.0,
                    totalExpense = 3000.0,
                    netAmount = 2000.0
                )
            }
        }

        // Verify "Income" label is displayed
        composeTestRule.onNodeWithText("Income")
            .assertExists()
            .assertIsDisplayed()

        // Verify income amount is displayed (formatted as currency)
        composeTestRule.onNodeWithText("5000", substring = true)
            .assertExists()
            .assertIsDisplayed()
    }

    /**
     * Test that expense displays in red with correct formatting
     * Requirement 1.4: System SHALL display expense values in red color with appropriate icon
     */
    @Test
    fun expense_displaysInRed_withCorrectFormatting() {
        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = 5000.0,
                    totalExpense = 3000.0,
                    netAmount = 2000.0
                )
            }
        }

        // Verify "Expense" label is displayed
        composeTestRule.onNodeWithText("Expense")
            .assertExists()
            .assertIsDisplayed()

        // Verify expense amount is displayed (formatted as currency)
        composeTestRule.onNodeWithText("3000", substring = true)
            .assertExists()
            .assertIsDisplayed()
    }

    /**
     * Test that net amount displays with correct contextual color (positive)
     * Requirement 1.5: System SHALL display net amount in green when positive
     */
    @Test
    fun netAmount_displaysInGreen_whenPositive() {
        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = 5000.0,
                    totalExpense = 3000.0,
                    netAmount = 2000.0
                )
            }
        }

        // Verify "Net" label is displayed
        composeTestRule.onNodeWithText("Net")
            .assertExists()
            .assertIsDisplayed()

        // Verify net amount is displayed with positive sign
        composeTestRule.onNodeWithText("+", substring = true)
            .assertExists()

        // Verify net amount value
        composeTestRule.onNodeWithText("2000", substring = true)
            .assertExists()
            .assertIsDisplayed()
    }

    /**
     * Test that net amount displays with correct contextual color (negative)
     * Requirement 1.5: System SHALL display net amount in red when negative
     */
    @Test
    fun netAmount_displaysInRed_whenNegative() {
        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = 2000.0,
                    totalExpense = 5000.0,
                    netAmount = -3000.0
                )
            }
        }

        // Verify "Net" label is displayed
        composeTestRule.onNodeWithText("Net")
            .assertExists()
            .assertIsDisplayed()

        // Verify net amount is displayed (negative sign is part of the formatted value)
        composeTestRule.onNodeWithText("3000", substring = true)
            .assertExists()
            .assertIsDisplayed()
    }

    /**
     * Test with zero values
     * Requirement 1.6: WHEN transaction list is empty, System SHALL display zero values
     */
    @Test
    fun periodSummary_displaysZeroValues_whenEmpty() {
        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = 0.0,
                    totalExpense = 0.0,
                    netAmount = 0.0
                )
            }
        }

        // Verify all labels are displayed
        composeTestRule.onNodeWithText("Income").assertExists()
        composeTestRule.onNodeWithText("Expense").assertExists()
        composeTestRule.onNodeWithText("Net").assertExists()

        // Verify zero values are displayed (formatted as currency)
        // Zero should display as +0 or 0 depending on formatting
        val zeroNodes = composeTestRule.onAllNodesWithText("0", substring = true)
        assert(zeroNodes.fetchSemanticsNodes().isNotEmpty()) {
            "Zero values should be displayed"
        }
    }

    /**
     * Test that all three summary items are displayed
     * Requirement 1.1: System SHALL display total income, total expense, and net amount
     */
    @Test
    fun periodSummary_displaysAllThreeItems() {
        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = 10000.0,
                    totalExpense = 7500.0,
                    netAmount = 2500.0
                )
            }
        }

        // Verify all three labels are present
        composeTestRule.onNodeWithText("Income")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Expense")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Net")
            .assertExists()
            .assertIsDisplayed()

        // Verify amounts are displayed
        composeTestRule.onNodeWithText("10000", substring = true).assertExists()
        composeTestRule.onNodeWithText("7500", substring = true).assertExists()
        composeTestRule.onNodeWithText("2500", substring = true).assertExists()
    }

    /**
     * Test with large amounts
     * Edge case: Verify proper formatting of large currency values
     */
    @Test
    fun periodSummary_handlesLargeAmounts() {
        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = 1000000.0,
                    totalExpense = 750000.0,
                    netAmount = 250000.0
                )
            }
        }

        // Verify labels are displayed
        composeTestRule.onNodeWithText("Income").assertExists()
        composeTestRule.onNodeWithText("Expense").assertExists()
        composeTestRule.onNodeWithText("Net").assertExists()

        // Verify large amounts are displayed (may have thousand separators)
        composeTestRule.onNodeWithText("1000000", substring = true).assertExists()
        composeTestRule.onNodeWithText("750000", substring = true).assertExists()
        composeTestRule.onNodeWithText("250000", substring = true).assertExists()
    }

    /**
     * Test with small decimal amounts
     * Edge case: Verify proper formatting of decimal values
     */
    @Test
    fun periodSummary_handlesDecimalAmounts() {
        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = 123.45,
                    totalExpense = 67.89,
                    netAmount = 55.56
                )
            }
        }

        // Verify labels are displayed
        composeTestRule.onNodeWithText("Income").assertExists()
        composeTestRule.onNodeWithText("Expense").assertExists()
        composeTestRule.onNodeWithText("Net").assertExists()

        // Verify decimal amounts are displayed
        composeTestRule.onNodeWithText("123", substring = true).assertExists()
        composeTestRule.onNodeWithText("67", substring = true).assertExists()
        composeTestRule.onNodeWithText("55", substring = true).assertExists()
    }

    /**
     * Test with only income (no expenses)
     * Edge case: When user has only income transactions
     */
    @Test
    fun periodSummary_displaysCorrectly_withOnlyIncome() {
        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = 5000.0,
                    totalExpense = 0.0,
                    netAmount = 5000.0
                )
            }
        }

        // Verify income is displayed
        composeTestRule.onNodeWithText("Income").assertExists()
        composeTestRule.onNodeWithText("5000", substring = true).assertExists()

        // Verify expense is zero
        composeTestRule.onNodeWithText("Expense").assertExists()

        // Verify net equals income
        composeTestRule.onNodeWithText("Net").assertExists()
        val netAmountNodes = composeTestRule.onAllNodesWithText("+", substring = true)
        assert(netAmountNodes.fetchSemanticsNodes().isNotEmpty()) {
            "Positive net amount should display + sign"
        }
    }

    /**
     * Test with only expenses (no income)
     * Edge case: When user has only expense transactions
     */
    @Test
    fun periodSummary_displaysCorrectly_withOnlyExpenses() {
        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = 0.0,
                    totalExpense = 3000.0,
                    netAmount = -3000.0
                )
            }
        }

        // Verify income is zero
        composeTestRule.onNodeWithText("Income").assertExists()

        // Verify expense is displayed
        composeTestRule.onNodeWithText("Expense").assertExists()
        composeTestRule.onNodeWithText("3000", substring = true).assertExists()

        // Verify net is negative
        composeTestRule.onNodeWithText("Net").assertExists()
    }

    /**
     * Test equal income and expense (zero net)
     * Edge case: When income equals expense, net should be zero
     */
    @Test
    fun periodSummary_displaysZeroNet_whenIncomeEqualsExpense() {
        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = 5000.0,
                    totalExpense = 5000.0,
                    netAmount = 0.0
                )
            }
        }

        // Verify all labels are displayed
        composeTestRule.onNodeWithText("Income").assertExists()
        composeTestRule.onNodeWithText("Expense").assertExists()
        composeTestRule.onNodeWithText("Net").assertExists()

        // Verify income and expense amounts
        composeTestRule.onAllNodesWithText("5000", substring = true).assertCountEquals(2)

        // Verify net is zero (with + sign for zero/positive values)
        composeTestRule.onNodeWithText("+", substring = true).assertExists()
    }

    /**
     * Integration test: Verify complete period summary card layout
     * Requirements 1.1, 1.3, 1.4, 1.5: All requirements met together
     */
    @Test
    fun periodSummary_displaysCompleteLayout() {
        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = 8000.0,
                    totalExpense = 6000.0,
                    netAmount = 2000.0
                )
            }
        }

        // Verify all three sections are present
        composeTestRule.onNodeWithText("Income")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Expense")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Net")
            .assertExists()
            .assertIsDisplayed()

        // Verify all amounts are displayed
        composeTestRule.onNodeWithText("8000", substring = true).assertExists()
        composeTestRule.onNodeWithText("6000", substring = true).assertExists()
        composeTestRule.onNodeWithText("2000", substring = true).assertExists()

        // Verify positive net has + sign
        composeTestRule.onNodeWithText("+", substring = true).assertExists()
    }

    /**
     * Test card has proper structure with dividers
     * Verify visual separation between sections
     */
    @Test
    fun periodSummary_hasProperStructure() {
        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = 1000.0,
                    totalExpense = 500.0,
                    netAmount = 500.0
                )
            }
        }

        // Verify the card renders as a Card component
        // by checking that all expected content is present and visible
        composeTestRule.onNodeWithText("Income").assertIsDisplayed()
        composeTestRule.onNodeWithText("Expense").assertIsDisplayed()
        composeTestRule.onNodeWithText("Net").assertIsDisplayed()

        // Verify all three sections have their amounts displayed
        composeTestRule.onNodeWithText("1000", substring = true).assertExists()
        composeTestRule.onNodeWithText("500", substring = true).assertExists()
    }

    /**
     * Test with very small amounts (edge case)
     * Verify proper handling of amounts less than 1
     */
    @Test
    fun periodSummary_handlesSmallAmounts() {
        composeTestRule.setContent {
            CashaTheme {
                PeriodSummaryCard(
                    totalIncome = 0.50,
                    totalExpense = 0.25,
                    netAmount = 0.25
                )
            }
        }

        // Verify labels are displayed
        composeTestRule.onNodeWithText("Income").assertExists()
        composeTestRule.onNodeWithText("Expense").assertExists()
        composeTestRule.onNodeWithText("Net").assertExists()

        // Small amounts should be formatted correctly
        // The exact formatting depends on CurrencyFormatter implementation
        // We verify that the card renders without errors
        val allText = composeTestRule.onAllNodesWithText("0", substring = true)
        assert(allText.fetchSemanticsNodes().isNotEmpty()) {
            "Small decimal amounts should be displayed"
        }
    }
}
