package com.casha.app.domain.usecase.goal

import com.casha.app.domain.model.CreateGoalRequest
import com.casha.app.domain.repository.GoalRepository
import javax.inject.Inject

class CreateGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend fun execute(request: CreateGoalRequest) {
        repository.createGoal(request)
    }
}
