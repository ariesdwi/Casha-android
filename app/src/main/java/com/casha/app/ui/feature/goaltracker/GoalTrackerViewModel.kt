package com.casha.app.ui.feature.goaltracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.domain.model.Goal
import com.casha.app.domain.model.GoalSummary
import com.casha.app.domain.usecase.goal.GetGoalSummaryUseCase
import com.casha.app.domain.usecase.goal.GetGoalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalTrackerUiState(
    val goals: List<Goal> = emptyList(),
    val goalSummary: GoalSummary? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class GoalTrackerViewModel @Inject constructor(
    private val getGoalsUseCase: GetGoalsUseCase,
    private val getGoalSummaryUseCase: GetGoalSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalTrackerUiState())
    val uiState: StateFlow<GoalTrackerUiState> = _uiState.asStateFlow()

    init {
        fetchAllData()
    }

    fun fetchAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val goals = getGoalsUseCase.execute()
                val summary = getGoalSummaryUseCase.execute()
                _uiState.update { it.copy(
                    goals = goals,
                    goalSummary = summary,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to fetch goals"
                ) }
            }
        }
    }
}
