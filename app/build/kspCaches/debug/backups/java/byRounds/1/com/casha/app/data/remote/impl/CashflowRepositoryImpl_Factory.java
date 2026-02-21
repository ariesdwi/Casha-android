package com.casha.app.data.remote.impl;

import com.casha.app.data.remote.api.CashflowApiService;
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
public final class CashflowRepositoryImpl_Factory implements Factory<CashflowRepositoryImpl> {
  private final Provider<CashflowApiService> apiServiceProvider;

  public CashflowRepositoryImpl_Factory(Provider<CashflowApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public CashflowRepositoryImpl get() {
    return newInstance(apiServiceProvider.get());
  }

  public static CashflowRepositoryImpl_Factory create(
      javax.inject.Provider<CashflowApiService> apiServiceProvider) {
    return new CashflowRepositoryImpl_Factory(Providers.asDaggerProvider(apiServiceProvider));
  }

  public static CashflowRepositoryImpl_Factory create(
      Provider<CashflowApiService> apiServiceProvider) {
    return new CashflowRepositoryImpl_Factory(apiServiceProvider);
  }

  public static CashflowRepositoryImpl newInstance(CashflowApiService apiService) {
    return new CashflowRepositoryImpl(apiService);
  }
}
