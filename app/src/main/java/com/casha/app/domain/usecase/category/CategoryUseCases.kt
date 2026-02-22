package com.casha.app.domain.usecase.category

import com.casha.app.data.local.dao.CategoryDao
import com.casha.app.domain.model.CategoryCasha
import com.casha.app.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Local-first read + background API sync.
 * Returns cached categories instantly, then syncs from API in background.
 */
class CategorySyncUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val categoryDao: CategoryDao
) {
    suspend fun fetchCategories(): List<CategoryCasha> {
        // 1. Read local first (instant UI)
        val localEntities = categoryDao.getAllCategories().firstOrNull() ?: emptyList()
        val localCategories = localEntities.map { it.toDomain() }

        // 2. Background sync with remote (non-blocking)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                categoryRepository.getAllCategories() // fetches API → saves to Room
            } catch (_: Exception) {
                // Silently fail — local data is sufficient
            }
        }

        return localCategories
    }

    private fun com.casha.app.data.local.entity.CategoryEntity.toDomain() = CategoryCasha(
        id = id,
        name = name,
        isActive = isActive,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Remote-first create. API must succeed before saving locally.
 */
class CreateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend fun execute(name: String, isActive: Boolean = true): CategoryCasha {
        return categoryRepository.createCategory(name, isActive)
    }
}

/**
 * Remote-first update. API must succeed before updating locally.
 */
class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend fun execute(id: String, name: String, isActive: Boolean): CategoryCasha {
        return categoryRepository.updateCategory(id, name, isActive)
    }
}

/**
 * Remote-first delete. API must succeed before removing from state.
 */
class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend fun execute(id: String) {
        categoryRepository.deleteCategory(id)
    }
}
