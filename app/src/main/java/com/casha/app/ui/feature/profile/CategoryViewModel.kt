package com.casha.app.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.domain.model.CategoryCasha
import com.casha.app.domain.usecase.category.CategorySyncUseCase
import com.casha.app.domain.usecase.category.CreateCategoryUseCase
import com.casha.app.domain.usecase.category.DeleteCategoryUseCase
import com.casha.app.domain.usecase.category.UpdateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val categories: List<CategoryCasha> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val syncUseCase: CategorySyncUseCase,
    private val createUseCase: CreateCategoryUseCase,
    private val updateUseCase: UpdateCategoryUseCase,
    private val deleteUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val categories = syncUseCase.fetchCategories()
                _uiState.update { it.copy(categories = categories, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Failed to fetch categories: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun addCategory(name: String, isActive: Boolean, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val newCategory = createUseCase.execute(name, isActive)
                _uiState.update { 
                    it.copy(
                        categories = it.categories + newCategory,
                        isLoading = false
                    ) 
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Failed to add category: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun editCategory(id: String, name: String, isActive: Boolean, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val updated = updateUseCase.execute(id, name, isActive)
                _uiState.update { state ->
                    val newList = state.categories.map { if (it.id == id) updated else it }
                    state.copy(categories = newList, isLoading = false)
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Failed to update category: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun removeCategory(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                deleteUseCase.execute(id)
                _uiState.update { state ->
                    val newList = state.categories.filter { it.id != id }
                    state.copy(categories = newList, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Failed to delete category: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
