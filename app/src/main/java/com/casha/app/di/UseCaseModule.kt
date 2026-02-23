package com.casha.app.di

import com.casha.app.domain.usecase.liability.GetLiabilityDetailsUseCase
import com.casha.app.domain.usecase.liability.GetLiabilityDetailsUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    @Singleton
    abstract fun bindGetLiabilityDetailsUseCase(impl: GetLiabilityDetailsUseCaseImpl): GetLiabilityDetailsUseCase
}
