package com.casha.app.widget.util

/**
 * Short currency formatter for widget compact spaces.
 * IDR: "rb" (ribu), "jt" (juta), "M" (miliar)
 * USD/EUR/etc: "k", "M", "B"
 */
object WidgetCurrencyFormatter {

    fun formatShort(amount: Double, currency: String = "IDR"): String {
        val symbol = currencySymbol(currency)
        val absAmount = kotlin.math.abs(amount)

        return when (currency) {
            "IDR" -> when {
                absAmount >= 1_000_000_000 -> "${symbol}${formatNum(absAmount / 1_000_000_000)}M"
                absAmount >= 1_000_000 -> "${symbol}${formatNum(absAmount / 1_000_000)}jt"
                absAmount >= 1_000 -> "${symbol}${formatNum(absAmount / 1_000)}rb"
                else -> "${symbol}${absAmount.toLong()}"
            }
            else -> when {
                absAmount >= 1_000_000_000 -> "${symbol}${formatNum(absAmount / 1_000_000_000)}B"
                absAmount >= 1_000_000 -> "${symbol}${formatNum(absAmount / 1_000_000)}M"
                absAmount >= 1_000 -> "${symbol}${formatNum(absAmount / 1_000)}k"
                else -> "${symbol}${absAmount.toLong()}"
            }
        }
    }

    fun formatFull(amount: Double, currency: String = "IDR"): String {
        val symbol = currencySymbol(currency)
        val separator = if (currency == "IDR") '.' else ','
        val wholeAmount = kotlin.math.abs(amount).toLong()
        val formatted = wholeAmount.toString().reversed().chunked(3).joinToString("$separator").reversed()
        return "$symbol$formatted"
    }

    private fun formatNum(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.1f", value).trimEnd('0').trimEnd('.')
        }
    }

    private fun currencySymbol(currency: String): String = when (currency) {
        "IDR" -> "Rp"
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        "SGD" -> "S$"
        "MYR" -> "RM"
        "THB" -> "฿"
        "PHP" -> "₱"
        "VND" -> "₫"
        "AUD" -> "A$"
        "KRW" -> "₩"
        "INR" -> "₹"
        else -> "$currency "
    }
}
