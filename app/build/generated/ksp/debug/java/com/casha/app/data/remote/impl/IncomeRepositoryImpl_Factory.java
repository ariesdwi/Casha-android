package com.casha.app.data.remote.impl;

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

  public IncomeRepositoryImpl_Factory(Provider<IncomeApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public IncomeRepositoryImpl get() {
    return newInstance(apiServiceProvider.get());
  }

  public static IncomeRepositoryImpl_Factory create(
      javax.inject.Provider<IncomeApiService> apiServiceProvider) {
    return new IncomeRepositoryImpl_Factory(Providers.asDaggerProvider(apiServiceProvider));
  }

  public static IncomeRepositoryImpl_Factory create(Provider<IncomeApiService> apiServiceProvider) {
    return new IncomeRepositoryImpl_Factory(apiServiceProvider);
  }

  public static IncomeRepositoryImpl newInstance(IncomeApiService apiService) {
    return new IncomeRepositoryImpl(apiService);
  }
}
