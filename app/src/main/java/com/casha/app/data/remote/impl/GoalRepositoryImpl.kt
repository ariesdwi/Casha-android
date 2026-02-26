package com.casha.app.data.remote.impl

import com.casha.app.data.remote.api.GoalApiService
import com.casha.app.data.remote.dto.*
import com.casha.app.domain.model.*
import com.casha.app.domain.repository.GoalRepository
import java.text.SimpleDateFormat
import java.util.*
import com.casha.app.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val apiService: GoalApiService
) : GoalRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override suspend fun getGoals(): List<Goal> {
        val result = safeApiCall { apiService.getGoals() }
        return result.fold(
            onSuccess = { response -> response.data?.map { it.toDomain() } ?: emptyList() },
            onFailure = { emptyList() }
        )
    }

    override suspend fun getSummary(): GoalSummary {
        val result = safeApiCall { apiService.getSummary() }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() ?: GoalSummary(0, 0, 0, 0.0, 0.0, 0.0) },
            onFailure = { GoalSummary(0, 0, 0, 0.0, 0.0, 0.0) }
        )
    }

    override suspend fun createGoal(request: CreateGoalRequest) {
        val mappedRequest = CreateGoalApiRequest(
            name = request.name,
            targetAmount = request.targetAmount,
            category = request.category.id, // the backend trace expects the category UUID string
            deadline = request.deadline?.let { dateFormat.format(it) },
            assetId = request.assetId,
            icon = request.icon,
            color = request.color,
            note = request.note
        )
        safeApiCall { apiService.createGoal(mappedRequest) }
    }

    override suspend fun fetchGoalCategories(): List<GoalCategory> {
        val result = safeApiCall { apiService.getGoalsCategories() }
        return result.fold(
            onSuccess = { response ->
                response.data?.map { dto ->
                    GoalCategory(
                        id = dto.id,
                        name = dto.name.replace("_", " ").lowercase().replaceFirstChar { char -> char.uppercase() },
                        icon = dto.icon,
                        color = dto.color,
                        isActive = dto.isActive,
                        userId = dto.userId
                    )
                } ?: emptyList()
            },
            onFailure = { emptyList() }
        )
    }

    override suspend fun getGoal(id: String): Goal? {
        val result = safeApiCall { apiService.getGoal(id) }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() },
            onFailure = { null }
        )
    }

    override suspend fun updateGoal(id: String, request: CreateGoalRequest) {
        val mappedRequest = CreateGoalApiRequest(
            name = request.name,
            targetAmount = request.targetAmount,
            category = request.category.id,
            deadline = request.deadline?.let { dateFormat.format(it) },
            assetId = request.assetId,
            icon = request.icon,
            color = request.color,
            note = request.note
        )
        safeApiCall { apiService.updateGoal(id, mappedRequest) }
    }

    override suspend fun deleteGoal(id: String) {
        safeApiCall { apiService.deleteGoal(id) }
    }

    override suspend fun addContribution(goalId: String, amount: Double, note: String?) {
        val request = AddContributionApiRequest(amount, note)
        safeApiCall { apiService.addContribution(goalId, request) }
    }

    private fun GoalDto.toDomain() = Goal(
        id = id,
        name = name,
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        currency = currency,
        category = categoryDetail?.let {
            GoalCategory(
                id = it.id,
                name = it.name.replace("_", " ").lowercase().replaceFirstChar { char -> char.uppercase() },
                icon = it.icon,
                color = it.color,
                isActive = it.isActive,
                userId = it.userId
            )
        } ?: GoalCategory(
            id = categoryId ?: category ?: "CUSTOM",
            name = (category ?: "Custom").replace("_", " ").lowercase().replaceFirstChar { char -> char.uppercase() },
            icon = icon ?: "🎯",
            color = color ?: "#4CAF50"
        ),
        icon = icon,
        color = color,
        deadline = try { deadline?.let { dateFormat.parse(it) } } catch (e: Exception) { null },
        status = try { GoalStatus.valueOf(status.uppercase()) } catch (e: Exception) { GoalStatus.ACTIVE },
        assetId = assetId,
        assetName = assetName,
        note = note,
        progress = progress?.toDomain() ?: GoalProgress(0.0),
        recentContributions = recentContributions.map { it.toDomain() },
        createdAt = try { createdAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() },
        updatedAt = try { updatedAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() }
    )

    private fun GoalProgressDto.toDomain() = GoalProgress(
        percentage = percentage,
        daysRemaining = daysRemaining,
        monthlySavingsNeeded = monthlySavingsNeeded
    )

    private fun GoalContributionDto.toDomain() = GoalContribution(
        id = id,
        goalId = goalId,
        amount = amount,
        note = note,
        datetime = try { dateFormat.parse(datetime) ?: Date() } catch (e: Exception) { Date() }
    )

    private fun GoalSummaryDto.toDomain() = GoalSummary(
        totalGoals = totalGoals,
        activeGoals = activeGoals,
        completedGoals = completedGoals,
        totalTarget = totalTarget,
        totalCurrent = totalCurrent,
        overallProgress = overallProgress,
        nearestDeadline = nearestDeadline?.toDomain()
    )

    private fun NearestDeadlineDto.toDomain() = NearestDeadline(
        name = name,
        deadline = try { dateFormat.parse(deadline) ?: Date() } catch (e: Exception) { Date() }
    )
}
