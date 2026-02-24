package com.casha.app.domain.usecase.goal

import com.casha.app.domain.model.GoalCategory
import com.casha.app.domain.repository.GoalRepository
import javax.inject.Inject

class GetGoalCategoriesUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend fun execute(): List<GoalCategory> {
        return repository.fetchGoalCategories()
    }
}
