package com.casha.app.di

import com.casha.app.data.remote.impl.AuthRepositoryImpl
import com.casha.app.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Application-level dependency injection module.
 * Binds repository interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}

