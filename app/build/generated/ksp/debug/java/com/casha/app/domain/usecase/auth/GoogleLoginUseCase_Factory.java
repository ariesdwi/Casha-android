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
public final class GoogleLoginUseCase_Factory implements Factory<GoogleLoginUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public GoogleLoginUseCase_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public GoogleLoginUseCase get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static GoogleLoginUseCase_Factory create(
      javax.inject.Provider<AuthRepository> authRepositoryProvider) {
    return new GoogleLoginUseCase_Factory(Providers.asDaggerProvider(authRepositoryProvider));
  }

  public static GoogleLoginUseCase_Factory create(Provider<AuthRepository> authRepositoryProvider) {
    return new GoogleLoginUseCase_Factory(authRepositoryProvider);
  }

  public static GoogleLoginUseCase newInstance(AuthRepository authRepository) {
    return new GoogleLoginUseCase(authRepository);
  }
}
