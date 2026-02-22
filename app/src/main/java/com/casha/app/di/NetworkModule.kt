package com.casha.app.di

import com.casha.app.core.config.AppConfig
import com.casha.app.core.network.AuthInterceptor
import com.casha.app.core.network.ErrorInterceptor
import com.casha.app.data.remote.api.AuthApiService
import com.casha.app.data.remote.api.CashflowApiService
import com.casha.app.data.remote.api.GoalApiService
import com.casha.app.data.remote.api.IncomeApiService
import com.casha.app.data.remote.api.TransactionApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Provides network-related dependencies: OkHttpClient, Retrofit, Json, API services.
 * BASE_URL is driven by build variant via [AppConfig].
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        errorInterceptor: ErrorInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(AppConfig.apiTimeout, TimeUnit.SECONDS)
            .readTimeout(AppConfig.apiTimeout, TimeUnit.SECONDS)
            .writeTimeout(AppConfig.apiTimeout, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(errorInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (AppConfig.shouldLogNetworkRequests)
                        HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
                }
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConfig.baseUrl)
            .client(client)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    // ── API Services ──

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTransactionApiService(retrofit: Retrofit): TransactionApiService {
        return retrofit.create(TransactionApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCashflowApiService(retrofit: Retrofit): CashflowApiService {
        return retrofit.create(CashflowApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideIncomeApiService(retrofit: Retrofit): IncomeApiService {
        return retrofit.create(IncomeApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGoalApiService(retrofit: Retrofit): GoalApiService {
        return retrofit.create(GoalApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCategoryApiService(retrofit: Retrofit): com.casha.app.data.remote.api.CategoryApiService {
        return retrofit.create(com.casha.app.data.remote.api.CategoryApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBudgetApiService(retrofit: Retrofit): com.casha.app.data.remote.api.BudgetApiService {
        return retrofit.create(com.casha.app.data.remote.api.BudgetApiService::class.java)
    }
}

