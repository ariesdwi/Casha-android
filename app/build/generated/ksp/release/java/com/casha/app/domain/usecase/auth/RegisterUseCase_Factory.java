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
public final class RegisterUseCase_Factory implements Factory<RegisterUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public RegisterUseCase_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public RegisterUseCase get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static RegisterUseCase_Factory create(
      javax.inject.Provider<AuthRepository> authRepositoryProvider) {
    return new RegisterUseCase_Factory(Providers.asDaggerProvider(authRepositoryProvider));
  }

  public static RegisterUseCase_Factory create(Provider<AuthRepository> authRepositoryProvider) {
    return new RegisterUseCase_Factory(authRepositoryProvider);
  }

  public static RegisterUseCase newInstance(AuthRepository authRepository) {
    return new RegisterUseCase(authRepository);
  }
}
