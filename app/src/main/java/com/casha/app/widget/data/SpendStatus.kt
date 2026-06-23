package com.casha.app.widget.data

import androidx.compose.ui.graphics.Color

enum class SpendStatus(val label: String, val color: Color) {
    COMFORTABLE("Aman", Color(0xFF2E7D32)),
    CAUTION("Hati-hati", Color(0xFFFF9800)),
    OVER_BUDGET("Over Budget", Color(0xFFF44336)),
    NO_INCOME("Belum ada income", Color(0xFF9E9E9E)),
    UNKNOWN("-", Color(0xFF9E9E9E));

    companion object {
        fun fromString(value: String?): SpendStatus = when (value) {
            "comfortable" -> COMFORTABLE
            "caution" -> CAUTION
            "over_budget" -> OVER_BUDGET
            "no_income" -> NO_INCOME
            else -> UNKNOWN
        }
    }
}
