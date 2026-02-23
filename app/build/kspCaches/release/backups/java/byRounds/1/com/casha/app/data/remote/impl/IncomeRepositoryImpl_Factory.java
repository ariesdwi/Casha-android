package com.casha.app.data.remote.impl;

import com.casha.app.data.local.dao.IncomeDao;
import com.casha.app.data.remote.api.CashflowApiService;
import com.casha.app.data.remote.api.IncomeApiService;
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
public final class IncomeRepositoryImpl_Factory implements Factory<IncomeRepositoryImpl> {
  private final Provider<IncomeApiService> apiServiceProvider;

  private final Provider<CashflowApiService> cashflowApiServiceProvider;

  private final Provider<IncomeDao> incomeDaoProvider;

  public IncomeRepositoryImpl_Factory(Provider<IncomeApiService> apiServiceProvider,
      Provider<CashflowApiService> cashflowApiServiceProvider,
      Provider<IncomeDao> incomeDaoProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.cashflowApiServiceProvider = cashflowApiServiceProvider;
    this.incomeDaoProvider = incomeDaoProvider;
  }

  @Override
  public IncomeRepositoryImpl get() {
    return newInstance(apiServiceProvider.get(), cashflowApiServiceProvider.get(), incomeDaoProvider.get());
  }

  public static IncomeRepositoryImpl_Factory create(
      javax.inject.Provider<IncomeApiService> apiServiceProvider,
      javax.inject.Provider<CashflowApiService> cashflowApiServiceProvider,
      javax.inject.Provider<IncomeDao> incomeDaoProvider) {
    return new IncomeRepositoryImpl_Factory(Providers.asDaggerProvider(apiServiceProvider), Providers.asDaggerProvider(cashflowApiServiceProvider), Providers.asDaggerProvider(incomeDaoProvider));
  }

  public static IncomeRepositoryImpl_Factory create(Provider<IncomeApiService> apiServiceProvider,
      Provider<CashflowApiService> cashflowApiServiceProvider,
      Provider<IncomeDao> incomeDaoProvider) {
    return new IncomeRepositoryImpl_Factory(apiServiceProvider, cashflowApiServiceProvider, incomeDaoProvider);
  }

  public static IncomeRepositoryImpl newInstance(IncomeApiService apiService,
      CashflowApiService cashflowApiService, IncomeDao incomeDao) {
    return new IncomeRepositoryImpl(apiService, cashflowApiService, incomeDao);
  }
}
