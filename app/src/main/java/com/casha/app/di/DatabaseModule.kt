package com.casha.app.di

import android.content.Context
import androidx.room.Room
import com.casha.app.data.local.database.CashaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides database-related dependencies: Room database, DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CashaDatabase {
        return Room.databaseBuilder(
            context,
            CashaDatabase::class.java,
            "casha_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTransactionDao(db: CashaDatabase) = db.transactionDao()

    @Provides
    fun provideBudgetDao(db: CashaDatabase) = db.budgetDao()

    @Provides
    fun provideCategoryDao(db: CashaDatabase) = db.categoryDao()

    @Provides
    fun provideIncomeDao(db: CashaDatabase) = db.incomeDao()
}
