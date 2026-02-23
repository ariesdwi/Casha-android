package com.casha.app.data.remote.impl;

import com.casha.app.data.remote.api.GoalApiService;
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
public final class GoalRepositoryImpl_Factory implements Factory<GoalRepositoryImpl> {
  private final Provider<GoalApiService> apiServiceProvider;

  public GoalRepositoryImpl_Factory(Provider<GoalApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public GoalRepositoryImpl get() {
    return newInstance(apiServiceProvider.get());
  }

  public static GoalRepositoryImpl_Factory create(
      javax.inject.Provider<GoalApiService> apiServiceProvider) {
    return new GoalRepositoryImpl_Factory(Providers.asDaggerProvider(apiServiceProvider));
  }

  public static GoalRepositoryImpl_Factory create(Provider<GoalApiService> apiServiceProvider) {
    return new GoalRepositoryImpl_Factory(apiServiceProvider);
  }

  public static GoalRepositoryImpl newInstance(GoalApiService apiService) {
    return new GoalRepositoryImpl(apiService);
  }
}
