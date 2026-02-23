package com.casha.app.data.remote.impl;

import com.casha.app.data.local.dao.TransactionDao;
import com.casha.app.data.remote.api.CashflowApiService;
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

  private final Provider<CashflowApiService> cashflowApiServiceProvider;

  private final Provider<TransactionDao> transactionDaoProvider;

  public TransactionRepositoryImpl_Factory(Provider<TransactionApiService> apiServiceProvider,
      Provider<CashflowApiService> cashflowApiServiceProvider,
      Provider<TransactionDao> transactionDaoProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.cashflowApiServiceProvider = cashflowApiServiceProvider;
    this.transactionDaoProvider = transactionDaoProvider;
  }

  @Override
  public TransactionRepositoryImpl get() {
    return newInstance(apiServiceProvider.get(), cashflowApiServiceProvider.get(), transactionDaoProvider.get());
  }

  public static TransactionRepositoryImpl_Factory create(
      javax.inject.Provider<TransactionApiService> apiServiceProvider,
      javax.inject.Provider<CashflowApiService> cashflowApiServiceProvider,
      javax.inject.Provider<TransactionDao> transactionDaoProvider) {
    return new TransactionRepositoryImpl_Factory(Providers.asDaggerProvider(apiServiceProvider), Providers.asDaggerProvider(cashflowApiServiceProvider), Providers.asDaggerProvider(transactionDaoProvider));
  }

  public static TransactionRepositoryImpl_Factory create(
      Provider<TransactionApiService> apiServiceProvider,
      Provider<CashflowApiService> cashflowApiServiceProvider,
      Provider<TransactionDao> transactionDaoProvider) {
    return new TransactionRepositoryImpl_Factory(apiServiceProvider, cashflowApiServiceProvider, transactionDaoProvider);
  }

  public static TransactionRepositoryImpl newInstance(TransactionApiService apiService,
      CashflowApiService cashflowApiService, TransactionDao transactionDao) {
    return new TransactionRepositoryImpl(apiService, cashflowApiService, transactionDao);
  }
}
