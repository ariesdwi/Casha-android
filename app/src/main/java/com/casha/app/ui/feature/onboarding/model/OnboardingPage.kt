package com.casha.app.ui.feature.onboarding.model

import com.casha.app.R

/**
 * Represents each page in the onboarding flow.
 * Mirrors iOS OnboardingPage enum.
 */
enum class OnboardingPage(val index: Int) {
    HOOK(0),
    CHAT_INPUT(1),
    REVELATION(2),
    TRANSACTION_HISTORY(3),
    BUDGET_TRACKER(4),
    PORTFOLIO(5),
    LIABILITIES(6),
    GOAL_TRACKER(7),
    GLOBAL_POWER(8),
    OFFLINE_SYNC(9);

    val isLastPage: Boolean get() = this == OFFLINE_SYNC

    val next: OnboardingPage?
        get() = entries.getOrNull(index + 1)

    val ctaTextResId: Int
        get() = when (this) {
            HOOK -> R.string.onboarding_cta_hook
            CHAT_INPUT -> R.string.onboarding_cta_chat_input
            REVELATION -> R.string.onboarding_cta_revelation
            TRANSACTION_HISTORY -> R.string.onboarding_cta_transaction_history
            BUDGET_TRACKER -> R.string.onboarding_cta_budget_tracker
            PORTFOLIO -> R.string.onboarding_cta_portfolio
            LIABILITIES -> R.string.onboarding_cta_liabilities
            GOAL_TRACKER -> R.string.onboarding_cta_goal_tracker
            GLOBAL_POWER -> R.string.onboarding_cta_global_power
            OFFLINE_SYNC -> R.string.onboarding_cta_offline_sync
        }

    companion object {
        fun fromIndex(index: Int): OnboardingPage =
            entries.firstOrNull { it.index == index } ?: HOOK
    }
}
