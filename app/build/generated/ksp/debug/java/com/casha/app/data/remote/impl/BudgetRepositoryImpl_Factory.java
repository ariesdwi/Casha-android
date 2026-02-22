package com.casha.app.data.remote.impl;

import com.casha.app.data.local.dao.BudgetDao;
import com.casha.app.data.remote.api.BudgetApiService;
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
public final class BudgetRepositoryImpl_Factory implements Factory<BudgetRepositoryImpl> {
  private final Provider<BudgetApiService> apiServiceProvider;

  private final Provider<BudgetDao> budgetDaoProvider;

  public BudgetRepositoryImpl_Factory(Provider<BudgetApiService> apiServiceProvider,
      Provider<BudgetDao> budgetDaoProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.budgetDaoProvider = budgetDaoProvider;
  }

  @Override
  public BudgetRepositoryImpl get() {
    return newInstance(apiServiceProvider.get(), budgetDaoProvider.get());
  }

  public static BudgetRepositoryImpl_Factory create(
      javax.inject.Provider<BudgetApiService> apiServiceProvider,
      javax.inject.Provider<BudgetDao> budgetDaoProvider) {
    return new BudgetRepositoryImpl_Factory(Providers.asDaggerProvider(apiServiceProvider), Providers.asDaggerProvider(budgetDaoProvider));
  }

  public static BudgetRepositoryImpl_Factory create(Provider<BudgetApiService> apiServiceProvider,
      Provider<BudgetDao> budgetDaoProvider) {
    return new BudgetRepositoryImpl_Factory(apiServiceProvider, budgetDaoProvider);
  }

  public static BudgetRepositoryImpl newInstance(BudgetApiService apiService, BudgetDao budgetDao) {
    return new BudgetRepositoryImpl(apiService, budgetDao);
  }
}
