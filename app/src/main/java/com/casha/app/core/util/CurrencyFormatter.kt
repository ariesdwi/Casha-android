package com.casha.app.core.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Utility for formatting monetary amounts with proper currency symbols.
 */
object CurrencyFormatter {

    /**
     * Formats [amount] with the given [currencyCode] (ISO 4217, e.g. "SGD", "IDR", "USD").
     * Falls back to the device's default locale if the currency is unknown.
     */
    fun format(amount: Double, currencyCode: String): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            val locale = getLocaleForCurrency(currencyCode)
            val formatter = NumberFormat.getCurrencyInstance(locale)
            formatter.currency = currency
            formatter.format(amount)
        } catch (e: IllegalArgumentException) {
            // Fallback: plain number with currency code
            "$currencyCode ${String.format("%.2f", amount)}"
        }
    }

    /**
     * Formats [amount] as a compact string (e.g. "1.2K", "3.5M").
     */
    fun formatCompact(amount: Double, currencyCode: String): String {
        val symbol = try {
            Currency.getInstance(currencyCode).symbol
        } catch (e: Exception) {
            currencyCode
        }

        return when {
            amount >= 1_000_000_000 -> "$symbol${String.format("%.1fB", amount / 1_000_000_000)}"
            amount >= 1_000_000 -> "$symbol${String.format("%.1fM", amount / 1_000_000)}"
            amount >= 1_000 -> "$symbol${String.format("%.1fK", amount / 1_000)}"
            else -> "$symbol${String.format("%.0f", amount)}"
        }
    }

    private fun getLocaleForCurrency(currencyCode: String): Locale {
        return when (currencyCode.uppercase()) {
            "IDR" -> Locale("id", "ID")
            "SGD" -> Locale("en", "SG")
            "USD" -> Locale.US
            "EUR" -> Locale.GERMANY
            "GBP" -> Locale.UK
            "JPY" -> Locale.JAPAN
            "KRW" -> Locale.KOREA
            "CNY" -> Locale.CHINA
            else -> Locale.getDefault()
        }
    }
}
