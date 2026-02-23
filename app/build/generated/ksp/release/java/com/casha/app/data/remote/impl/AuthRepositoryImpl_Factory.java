package com.casha.app.data.remote.impl;

import com.casha.app.data.remote.api.AuthApiService;
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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<AuthApiService> apiServiceProvider;

  public AuthRepositoryImpl_Factory(Provider<AuthApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(apiServiceProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(
      javax.inject.Provider<AuthApiService> apiServiceProvider) {
    return new AuthRepositoryImpl_Factory(Providers.asDaggerProvider(apiServiceProvider));
  }

  public static AuthRepositoryImpl_Factory create(Provider<AuthApiService> apiServiceProvider) {
    return new AuthRepositoryImpl_Factory(apiServiceProvider);
  }

  public static AuthRepositoryImpl newInstance(AuthApiService apiService) {
    return new AuthRepositoryImpl(apiService);
  }
}
