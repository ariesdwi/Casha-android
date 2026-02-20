package com.casha.app.domain.usecase.auth;

import com.casha.app.domain.repository.AuthRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class AppleLoginUseCase_Factory implements Factory<AppleLoginUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public AppleLoginUseCase_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public AppleLoginUseCase get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static AppleLoginUseCase_Factory create(
      javax.inject.Provider<AuthRepository> authRepositoryProvider) {
    return new AppleLoginUseCase_Factory(Providers.asDaggerProvider(authRepositoryProvider));
  }

  public static AppleLoginUseCase_Factory create(Provider<AuthRepository> authRepositoryProvider) {
    return new AppleLoginUseCase_Factory(authRepositoryProvider);
  }

  public static AppleLoginUseCase newInstance(AuthRepository authRepository) {
    return new AppleLoginUseCase(authRepository);
  }
}
