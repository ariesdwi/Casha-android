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
public final class UpdateProfileUseCase_Factory implements Factory<UpdateProfileUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public UpdateProfileUseCase_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public UpdateProfileUseCase get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static UpdateProfileUseCase_Factory create(
      javax.inject.Provider<AuthRepository> authRepositoryProvider) {
    return new UpdateProfileUseCase_Factory(Providers.asDaggerProvider(authRepositoryProvider));
  }

  public static UpdateProfileUseCase_Factory create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new UpdateProfileUseCase_Factory(authRepositoryProvider);
  }

  public static UpdateProfileUseCase newInstance(AuthRepository authRepository) {
    return new UpdateProfileUseCase(authRepository);
  }
}
