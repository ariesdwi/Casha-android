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
