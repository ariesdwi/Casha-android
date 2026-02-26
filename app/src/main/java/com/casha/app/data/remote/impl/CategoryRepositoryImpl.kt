package com.casha.app.data.remote.impl

import com.casha.app.data.local.dao.CategoryDao
import com.casha.app.data.local.entity.CategoryEntity
import com.casha.app.data.remote.api.CategoryApiService
import com.casha.app.data.remote.dto.CategoryDto
import com.casha.app.data.remote.dto.CreateCategoryRequest
import com.casha.app.data.remote.dto.UpdateCategoryRequest
import com.casha.app.domain.model.CategoryCasha
import com.casha.app.domain.repository.CategoryRepository
import java.text.SimpleDateFormat
import java.util.*
import com.casha.app.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val apiService: CategoryApiService,
    private val categoryDao: CategoryDao
) : CategoryRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override suspend fun getAllCategories(): List<CategoryCasha> {
        val result = safeApiCall { apiService.getAllCategories() }
        return result.fold(
            onSuccess = { response -> 
                val entities = response.data?.map { it.toEntity() } ?: emptyList()
                if (entities.isNotEmpty()) {
                    categoryDao.insertCategories(entities)
                }
                entities.map { it.toDomain() }
            },
            onFailure = { 
                // Fallback to local data on error
                categoryDao.getAllCategoriesOnce().map { it.toDomain() }
            }
        )
    }

    override suspend fun createCategory(name: String, isActive: Boolean): CategoryCasha {
        val result = safeApiCall { apiService.createCategory(CreateCategoryRequest(name, isActive)) }
        return result.fold(
            onSuccess = { response ->
                val entity = response.data?.toEntity() ?: throw IllegalStateException("Failed to create category")
                categoryDao.insertCategory(entity)
                entity.toDomain()
            },
            onFailure = { throw it }
        )
    }

    override suspend fun updateCategory(id: String, name: String, isActive: Boolean): CategoryCasha {
        val result = safeApiCall { apiService.updateCategory(id, UpdateCategoryRequest(name, isActive)) }
        return result.fold(
            onSuccess = { response ->
                val entity = response.data?.toEntity() ?: throw IllegalStateException("Failed to update category")
                categoryDao.insertCategory(entity)
                entity.toDomain()
            },
            onFailure = { throw it }
        )
    }

    override suspend fun deleteCategory(id: String) {
        val result = safeApiCall { apiService.deleteCategory(id) }
        result.onSuccess {
            categoryDao.deleteById(id)
        }.onFailure { throw it }
    }

    // ── Mapper ──

    private fun CategoryDto.toEntity() = CategoryEntity(
        id = id,
        name = name,
        isActive = isActive,
        userId = userId,
        createdAt = try { createdAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() },
        updatedAt = try { updatedAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() }
    )

    private fun CategoryEntity.toDomain() = CategoryCasha(
        id = id,
        name = name,
        isActive = isActive,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
