package com.casha.app.data.remote.impl;

import com.casha.app.data.local.dao.TransactionDao;
import com.casha.app.data.remote.api.TransactionApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class TransactionRepositoryImpl_Factory implements Factory<TransactionRepositoryImpl> {
  private final Provider<TransactionApiService> apiServiceProvider;

  private final Provider<TransactionDao> transactionDaoProvider;

  public TransactionRepositoryImpl_Factory(Provider<TransactionApiService> apiServiceProvider,
      Provider<TransactionDao> transactionDaoProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.transactionDaoProvider = transactionDaoProvider;
  }

  @Override
  public TransactionRepositoryImpl get() {
    return newInstance(apiServiceProvider.get(), transactionDaoProvider.get());
  }

  public static TransactionRepositoryImpl_Factory create(
      javax.inject.Provider<TransactionApiService> apiServiceProvider,
      javax.inject.Provider<TransactionDao> transactionDaoProvider) {
    return new TransactionRepositoryImpl_Factory(Providers.asDaggerProvider(apiServiceProvider), Providers.asDaggerProvider(transactionDaoProvider));
  }

  public static TransactionRepositoryImpl_Factory create(
      Provider<TransactionApiService> apiServiceProvider,
      Provider<TransactionDao> transactionDaoProvider) {
    return new TransactionRepositoryImpl_Factory(apiServiceProvider, transactionDaoProvider);
  }

  public static TransactionRepositoryImpl newInstance(TransactionApiService apiService,
      TransactionDao transactionDao) {
    return new TransactionRepositoryImpl(apiService, transactionDao);
  }
}
