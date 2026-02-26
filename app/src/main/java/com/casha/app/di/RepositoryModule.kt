package com.casha.app.di

import com.casha.app.data.remote.impl.TransactionRepositoryImpl
import com.casha.app.data.remote.impl.CashflowRepositoryImpl
import com.casha.app.data.remote.impl.IncomeRepositoryImpl
import com.casha.app.data.remote.impl.GoalRepositoryImpl
import com.casha.app.domain.repository.TransactionRepository
import com.casha.app.domain.repository.CashflowRepository
import com.casha.app.domain.repository.IncomeRepository
import com.casha.app.domain.repository.GoalRepository
import com.casha.app.data.remote.impl.ChatRepositoryImpl
import com.casha.app.domain.repository.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Application-level dependency injection module for Repositories.
 * Binds repository interfaces to their implementations.
 * (Stubs to be filled as modules are ported from iOS)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCashflowRepository(impl: CashflowRepositoryImpl): CashflowRepository

    @Binds
    @Singleton
    abstract fun bindIncomeRepository(impl: IncomeRepositoryImpl): IncomeRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: com.casha.app.data.remote.impl.CategoryRepositoryImpl): com.casha.app.domain.repository.CategoryRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: com.casha.app.data.remote.impl.BudgetRepositoryImpl): com.casha.app.domain.repository.BudgetRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindLiabilityRepository(impl: com.casha.app.data.remote.impl.LiabilityRepositoryImpl): com.casha.app.domain.repository.LiabilityRepository

    @Binds
    @Singleton
    abstract fun bindPortfolioRepository(impl: com.casha.app.data.remote.impl.PortfolioRepositoryImpl): com.casha.app.domain.repository.PortfolioRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: com.casha.app.data.remote.impl.NotificationRepositoryImpl): com.casha.app.domain.repository.NotificationRepository
}
