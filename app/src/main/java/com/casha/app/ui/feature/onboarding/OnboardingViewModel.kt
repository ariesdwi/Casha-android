package com.casha.app.ui.feature.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import com.casha.app.ui.feature.onboarding.model.OnboardingPage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_HAS_SEEN = "has_seen_onboarding"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentPage = MutableStateFlow(OnboardingPage.HOOK)
    val currentPage: StateFlow<OnboardingPage> = _currentPage.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    val hasSeenOnboarding: Boolean
        get() = prefs.getBoolean(KEY_HAS_SEEN, false)

    fun setPage(page: OnboardingPage) {
        _currentPage.value = page
    }

    fun nextPage() {
        val next = _currentPage.value.next
        if (next != null) {
            _currentPage.value = next
        } else {
            completeOnboarding()
        }
    }

    fun skip() {
        completeOnboarding()
    }

    private fun completeOnboarding() {
        prefs.edit().putBoolean(KEY_HAS_SEEN, true).apply()
        _isCompleted.value = true
    }
}
