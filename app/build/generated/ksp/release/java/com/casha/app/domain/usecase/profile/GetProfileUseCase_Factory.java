package com.casha.app.domain.usecase.profile;

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
public final class GetProfileUseCase_Factory implements Factory<GetProfileUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public GetProfileUseCase_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public GetProfileUseCase get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static GetProfileUseCase_Factory create(
      javax.inject.Provider<AuthRepository> authRepositoryProvider) {
    return new GetProfileUseCase_Factory(Providers.asDaggerProvider(authRepositoryProvider));
  }

  public static GetProfileUseCase_Factory create(Provider<AuthRepository> authRepositoryProvider) {
    return new GetProfileUseCase_Factory(authRepositoryProvider);
  }

  public static GetProfileUseCase newInstance(AuthRepository authRepository) {
    return new GetProfileUseCase(authRepository);
  }
}
