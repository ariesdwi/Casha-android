package com.casha.app.ui.feature.goaltracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.domain.model.Goal
import com.casha.app.domain.model.GoalSummary
import com.casha.app.domain.usecase.goal.GetGoalSummaryUseCase
import com.casha.app.domain.usecase.goal.GetGoalsUseCase
import com.casha.app.domain.usecase.goal.GetGoalUseCase
import com.casha.app.domain.usecase.goal.UpdateGoalUseCase
import com.casha.app.domain.usecase.goal.DeleteGoalUseCase
import com.casha.app.domain.usecase.goal.AddGoalContributionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import com.casha.app.domain.model.GoalCategory
import com.casha.app.domain.model.GoalContribution
import com.casha.app.domain.model.CreateGoalRequest
import com.casha.app.domain.usecase.goal.CreateGoalUseCase
import com.casha.app.domain.usecase.goal.GetGoalCategoriesUseCase

data class GoalTrackerUiState(
    val goals: List<Goal> = emptyList(),
    val goalSummary: GoalSummary? = null,
    val categories: List<GoalCategory> = emptyList(),
    val selectedGoal: Goal? = null,
    val contributions: List<GoalContribution> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class GoalTrackerViewModel @Inject constructor(
    private val getGoalsUseCase: GetGoalsUseCase,
    private val getGoalSummaryUseCase: GetGoalSummaryUseCase,
    private val getGoalCategoriesUseCase: GetGoalCategoriesUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val getGoalUseCase: GetGoalUseCase,
    private val updateGoalUseCase: UpdateGoalUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase,
    private val addGoalContributionUseCase: AddGoalContributionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalTrackerUiState())
    val uiState: StateFlow<GoalTrackerUiState> = _uiState.asStateFlow()

    init {
        fetchAllData()
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val cats = getGoalCategoriesUseCase.execute()
                _uiState.update { it.copy(categories = cats) }
            } catch (e: Exception) {
                // Ignore failure for mockup
            }
        }
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

    fun fetchGoalDetails(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val goal = getGoalUseCase.execute(id)
                _uiState.update { it.copy(
                    selectedGoal = goal,
                    contributions = goal?.recentContributions ?: emptyList(),
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to fetch goal details"
                ) }
            }
        }
    }

    fun createGoal(
        name: String,
        targetAmount: Double,
        category: GoalCategory,
        deadline: Date?,
        assetId: String?,
        icon: String,
        color: String,
        note: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                createGoalUseCase.execute(
                    CreateGoalRequest(
                        name = name,
                        targetAmount = targetAmount,
                        category = category,
                        deadline = deadline,
                        assetId = assetId,
                        icon = icon,
                        color = color,
                        note = note
                    )
                )
                // Refresh list on success
                fetchAllData()
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateGoal(
        id: String,
        name: String,
        targetAmount: Double,
        category: GoalCategory,
        deadline: Date?,
        assetId: String?,
        icon: String,
        color: String,
        note: String
    ): kotlinx.coroutines.Deferred<Boolean> = viewModelScope.async {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            updateGoalUseCase.execute(
                id,
                CreateGoalRequest(
                    name = name,
                    targetAmount = targetAmount,
                    category = category,
                    deadline = deadline,
                    assetId = assetId,
                    icon = icon,
                    color = color,
                    note = note
                )
            )
            fetchAllData()
            true
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            false
        }
    }

    suspend fun deleteGoal(id: String): Boolean {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        return try {
            deleteGoalUseCase.execute(id)
            fetchAllData()
            true
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            false
        }
    }

    suspend fun addContribution(goalId: String, amount: Double, note: String?) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            addGoalContributionUseCase.execute(goalId, amount, note)
            fetchGoalDetails(goalId)
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
        }
    }
}
