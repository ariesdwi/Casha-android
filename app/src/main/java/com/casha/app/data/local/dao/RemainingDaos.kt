package com.casha.app.data.local.dao

import androidx.room.*
import com.casha.app.data.local.entity.BudgetEntity
import com.casha.app.data.local.entity.CategoryEntity
import com.casha.app.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY updatedAt DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Query("DELETE FROM budgets")
    suspend fun clearAll()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun clearAll()
}

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes ORDER BY datetime DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncomes(incomes: List<IncomeEntity>)

    @Query("DELETE FROM incomes")
    suspend fun clearAll()
}
