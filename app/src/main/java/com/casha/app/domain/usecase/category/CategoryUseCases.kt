package com.casha.app.domain.usecase.category

import com.casha.app.data.local.dao.CategoryDao
import com.casha.app.data.local.entity.CategoryEntity
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
    suspend fun fetchCategories(): List<CategoryEntity> {
        // 1. Read local first (instant UI)
        val localCategories = categoryDao.getAllCategories().firstOrNull() ?: emptyList()

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
}

/**
 * Remote-first create. API must succeed before saving locally.
 */
class CreateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend fun execute(name: String, isActive: Boolean = true): CategoryEntity {
        return categoryRepository.createCategory(name, isActive)
    }
}

/**
 * Remote-first update. API must succeed before updating locally.
 */
class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend fun execute(id: String, name: String, isActive: Boolean): CategoryEntity {
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
