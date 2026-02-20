package com.casha.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides database-related dependencies: Room database, DAOs.
 *
 * TODO: Create CashaDatabase class and provide DAOs here.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // TODO: Provide Room database instance
    // @Provides
    // @Singleton
    // fun provideDatabase(@ApplicationContext context: Context): CashaDatabase {
    //     return Room.databaseBuilder(
    //         context,
    //         CashaDatabase::class.java,
    //         "casha_database"
    //     ).build()
    // }

    // TODO: Provide DAOs
    // @Provides
    // fun provideTransactionDao(db: CashaDatabase) = db.transactionDao()
}
