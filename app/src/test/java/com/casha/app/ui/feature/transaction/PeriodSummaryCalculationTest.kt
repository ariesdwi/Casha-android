package com.casha.app.ui.feature.transaction

import com.casha.app.domain.model.CashflowDateSection
import com.casha.app.domain.model.CashflowEntry
import com.casha.app.domain.model.CashflowType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

/**
 * Unit tests for period summary calculation functionality.
 * Tests Requirements: 9.1, 9.2, 9.3, 9.4, 9.5
 */
class PeriodSummaryCalculationTest {

    private fun createCashflowEntry(
        id: String,
        title: String,
        amount: Double,
        type: CashflowType,
        category: String = "Test Category"
    ): CashflowEntry {
        return CashflowEntry(
            id = id,
            title = title,
            amount = amount,
            category = category,
            type = type,
            date = Date()
        )
    }

    @Test
    fun calculatePeriodSummary_withEmptyList_returnsZeroValues() {
        // Given: Empty list of CashflowDateSections
        val sections = emptyList<CashflowDateSection>()

        // When: Calculate period summary
        val summary = sections.calculatePeriodSummary()

        // Then: All values should be zero
        assertEquals("Total income should be 0.0", 0.0, summary.totalIncome, 0.001)
        assertEquals("Total expense should be 0.0", 0.0, summary.totalExpense, 0.001)
        assertEquals("Net amount should be 0.0", 0.0, summary.netAmount, 0.001)
    }

    @Test
    fun calculatePeriodSummary_withOnlyIncome_calculatesCorrectly() {
        // Given: Sections with only income transactions
        val incomeEntry1 = createCashflowEntry("1", "Salary", 5000.0, CashflowType.INCOME)
        val incomeEntry2 = createCashflowEntry("2", "Bonus", 1000.0, CashflowType.INCOME)
        
        val section = CashflowDateSection(
            day = "Today",
            date = "1 Jan 2024",
            items = listOf(incomeEntry1, incomeEntry2)
        )
        val sections = listOf(section)

        // When: Calculate period summary
        val summary = sections.calculatePeriodSummary()

        // Then: Total income should be sum of all income, expense should be 0
        assertEquals("Total income should be 6000.0", 6000.0, summary.totalIncome, 0.001)
        assertEquals("Total expense should be 0.0", 0.0, summary.totalExpense, 0.001)
        assertEquals("Net amount should be 6000.0", 6000.0, summary.netAmount, 0.001)
    }

    @Test
    fun calculatePeriodSummary_withOnlyExpense_calculatesCorrectly() {
        // Given: Sections with only expense transactions
        val expenseEntry1 = createCashflowEntry("1", "Groceries", 150.0, CashflowType.EXPENSE)
        val expenseEntry2 = createCashflowEntry("2", "Restaurant", 75.0, CashflowType.EXPENSE)
        
        val section = CashflowDateSection(
            day = "Today",
            date = "1 Jan 2024",
            items = listOf(expenseEntry1, expenseEntry2)
        )
        val sections = listOf(section)

        // When: Calculate period summary
        val summary = sections.calculatePeriodSummary()

        // Then: Total expense should be sum of all expenses, income should be 0
        assertEquals("Total income should be 0.0", 0.0, summary.totalIncome, 0.001)
        assertEquals("Total expense should be 225.0", 225.0, summary.totalExpense, 0.001)
        assertEquals("Net amount should be -225.0", -225.0, summary.netAmount, 0.001)
    }

    @Test
    fun calculatePeriodSummary_withMixedTransactions_calculatesCorrectly() {
        // Given: Sections with mixed income and expense transactions
        val incomeEntry1 = createCashflowEntry("1", "Salary", 5000.0, CashflowType.INCOME)
        val incomeEntry2 = createCashflowEntry("2", "Freelance", 800.0, CashflowType.INCOME)
        val expenseEntry1 = createCashflowEntry("3", "Rent", 1500.0, CashflowType.EXPENSE)
        val expenseEntry2 = createCashflowEntry("4", "Groceries", 300.0, CashflowType.EXPENSE)
        val expenseEntry3 = createCashflowEntry("5", "Transport", 100.0, CashflowType.EXPENSE)
        
        val section = CashflowDateSection(
            day = "Today",
            date = "1 Jan 2024",
            items = listOf(incomeEntry1, expenseEntry1, incomeEntry2, expenseEntry2, expenseEntry3)
        )
        val sections = listOf(section)

        // When: Calculate period summary
        val summary = sections.calculatePeriodSummary()

        // Then: Verify all calculations
        assertEquals("Total income should be 5800.0", 5800.0, summary.totalIncome, 0.001)
        assertEquals("Total expense should be 1900.0", 1900.0, summary.totalExpense, 0.001)
        assertEquals("Net amount should be 3900.0", 3900.0, summary.netAmount, 0.001)
    }

    @Test
    fun calculatePeriodSummary_withMultipleSections_calculatesCorrectly() {
        // Given: Multiple date sections with various transactions
        val section1 = CashflowDateSection(
            day = "Monday",
            date = "1 Jan 2024",
            items = listOf(
                createCashflowEntry("1", "Salary", 5000.0, CashflowType.INCOME),
                createCashflowEntry("2", "Groceries", 150.0, CashflowType.EXPENSE)
            )
        )
        
        val section2 = CashflowDateSection(
            day = "Tuesday",
            date = "2 Jan 2024",
            items = listOf(
                createCashflowEntry("3", "Freelance", 1200.0, CashflowType.INCOME),
                createCashflowEntry("4", "Restaurant", 75.0, CashflowType.EXPENSE),
                createCashflowEntry("5", "Transport", 50.0, CashflowType.EXPENSE)
            )
        )
        
        val section3 = CashflowDateSection(
            day = "Wednesday",
            date = "3 Jan 2024",
            items = listOf(
                createCashflowEntry("6", "Utilities", 200.0, CashflowType.EXPENSE)
            )
        )
        
        val sections = listOf(section1, section2, section3)

        // When: Calculate period summary
        val summary = sections.calculatePeriodSummary()

        // Then: Verify aggregation across all sections
        assertEquals("Total income should be 6200.0", 6200.0, summary.totalIncome, 0.001)
        assertEquals("Total expense should be 475.0", 475.0, summary.totalExpense, 0.001)
        assertEquals("Net amount should be 5725.0", 5725.0, summary.netAmount, 0.001)
    }

    @Test
    fun calculatePeriodSummary_withNegativeNetAmount_calculatesCorrectly() {
        // Given: Sections where expenses exceed income
        val section = CashflowDateSection(
            day = "Today",
            date = "1 Jan 2024",
            items = listOf(
                createCashflowEntry("1", "Part-time", 1000.0, CashflowType.INCOME),
                createCashflowEntry("2", "Rent", 1500.0, CashflowType.EXPENSE),
                createCashflowEntry("3", "Shopping", 800.0, CashflowType.EXPENSE)
            )
        )
        val sections = listOf(section)

        // When: Calculate period summary
        val summary = sections.calculatePeriodSummary()

        // Then: Net amount should be negative
        assertEquals("Total income should be 1000.0", 1000.0, summary.totalIncome, 0.001)
        assertEquals("Total expense should be 2300.0", 2300.0, summary.totalExpense, 0.001)
        assertEquals("Net amount should be -1300.0", -1300.0, summary.netAmount, 0.001)
    }

    @Test
    fun calculatePeriodSummary_withLargeAmounts_calculatesAccurately() {
        // Given: Sections with large transaction amounts
        val section = CashflowDateSection(
            day = "Today",
            date = "1 Jan 2024",
            items = listOf(
                createCashflowEntry("1", "Investment Return", 150000.0, CashflowType.INCOME),
                createCashflowEntry("2", "Annual Salary", 60000.0, CashflowType.INCOME),
                createCashflowEntry("3", "Property Purchase", 200000.0, CashflowType.EXPENSE)
            )
        )
        val sections = listOf(section)

        // When: Calculate period summary
        val summary = sections.calculatePeriodSummary()

        // Then: Verify large amounts are calculated accurately
        assertEquals("Total income should be 210000.0", 210000.0, summary.totalIncome, 0.001)
        assertEquals("Total expense should be 200000.0", 200000.0, summary.totalExpense, 0.001)
        assertEquals("Net amount should be 10000.0", 10000.0, summary.netAmount, 0.001)
    }

    @Test
    fun calculatePeriodSummary_withDecimalAmounts_calculatesAccurately() {
        // Given: Sections with decimal transaction amounts
        val section = CashflowDateSection(
            day = "Today",
            date = "1 Jan 2024",
            items = listOf(
                createCashflowEntry("1", "Salary", 3250.75, CashflowType.INCOME),
                createCashflowEntry("2", "Groceries", 87.50, CashflowType.EXPENSE),
                createCashflowEntry("3", "Coffee", 4.25, CashflowType.EXPENSE),
                createCashflowEntry("4", "Bonus", 150.50, CashflowType.INCOME)
            )
        )
        val sections = listOf(section)

        // When: Calculate period summary
        val summary = sections.calculatePeriodSummary()

        // Then: Verify decimal precision is maintained
        assertEquals("Total income should be 3401.25", 3401.25, summary.totalIncome, 0.001)
        assertEquals("Total expense should be 91.75", 91.75, summary.totalExpense, 0.001)
        assertEquals("Net amount should be 3309.5", 3309.5, summary.netAmount, 0.001)
    }

    @Test
    fun calculatePeriodSummary_withSectionsContainingNoItems_returnsZeroValues() {
        // Given: Sections with empty items lists
        val section1 = CashflowDateSection(
            day = "Monday",
            date = "1 Jan 2024",
            items = emptyList()
        )
        val section2 = CashflowDateSection(
            day = "Tuesday",
            date = "2 Jan 2024",
            items = emptyList()
        )
        val sections = listOf(section1, section2)

        // When: Calculate period summary
        val summary = sections.calculatePeriodSummary()

        // Then: All values should be zero
        assertEquals("Total income should be 0.0", 0.0, summary.totalIncome, 0.001)
        assertEquals("Total expense should be 0.0", 0.0, summary.totalExpense, 0.001)
        assertEquals("Net amount should be 0.0", 0.0, summary.netAmount, 0.001)
    }
}
