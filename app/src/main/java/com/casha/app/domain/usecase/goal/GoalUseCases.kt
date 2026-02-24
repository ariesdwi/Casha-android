package com.casha.app.domain.usecase.goal

import com.casha.app.domain.model.Goal
import com.casha.app.domain.model.GoalSummary
import com.casha.app.domain.repository.GoalRepository
import javax.inject.Inject

class GetGoalsUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend fun execute(): List<Goal> {
        return repository.getGoals()
    }
}

class GetGoalSummaryUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend fun execute(): GoalSummary {
        return repository.getSummary()
    }
}

class GetGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend fun execute(id: String): Goal? {
        return repository.getGoal(id)
    }
}

class UpdateGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend fun execute(id: String, request: com.casha.app.domain.model.CreateGoalRequest) {
        repository.updateGoal(id, request)
    }
}

class DeleteGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend fun execute(id: String) {
        repository.deleteGoal(id)
    }
}

class AddGoalContributionUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend fun execute(goalId: String, amount: Double, note: String?) {
        repository.addContribution(goalId, amount, note)
    }
}
