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
public final class ResetPasswordUseCase_Factory implements Factory<ResetPasswordUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public ResetPasswordUseCase_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public ResetPasswordUseCase get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static ResetPasswordUseCase_Factory create(
      javax.inject.Provider<AuthRepository> authRepositoryProvider) {
    return new ResetPasswordUseCase_Factory(Providers.asDaggerProvider(authRepositoryProvider));
  }

  public static ResetPasswordUseCase_Factory create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new ResetPasswordUseCase_Factory(authRepositoryProvider);
  }

  public static ResetPasswordUseCase newInstance(AuthRepository authRepository) {
    return new ResetPasswordUseCase(authRepository);
  }
}
