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
        val response = apiService.getAllCategories()
        val entities = response.data?.map { it.toEntity() } ?: emptyList()
        if (entities.isNotEmpty()) {
            categoryDao.insertCategories(entities)
        }
        return entities.map { it.toDomain() }
    }

    override suspend fun createCategory(name: String, isActive: Boolean): CategoryCasha {
        // Remote-first: API must succeed before saving locally
        val response = apiService.createCategory(CreateCategoryRequest(name, isActive))
        val entity = response.data?.toEntity()
            ?: throw IllegalStateException("Failed to create category")
        categoryDao.insertCategory(entity)
        return entity.toDomain()
    }

    override suspend fun updateCategory(id: String, name: String, isActive: Boolean): CategoryCasha {
        // Remote-first: API must succeed before updating locally
        val response = apiService.updateCategory(id, UpdateCategoryRequest(name, isActive))
        val entity = response.data?.toEntity()
            ?: throw IllegalStateException("Failed to update category")
        categoryDao.insertCategory(entity) // upsert
        return entity.toDomain()
    }

    override suspend fun deleteCategory(id: String) {
        // Remote-first: API must succeed before removing locally
        apiService.deleteCategory(id)
        // Note: CategoryDao doesn't have deleteById, so we skip local delete
        // The next sync will refresh the list
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
