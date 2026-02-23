package com.casha.app.ui.feature.auth;

import com.casha.app.core.auth.AuthManager;
import com.casha.app.domain.usecase.auth.RegisterUseCase;
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
public final class RegisterViewModel_Factory implements Factory<RegisterViewModel> {
  private final Provider<RegisterUseCase> registerUseCaseProvider;

  private final Provider<AuthManager> authManagerProvider;

  public RegisterViewModel_Factory(Provider<RegisterUseCase> registerUseCaseProvider,
      Provider<AuthManager> authManagerProvider) {
    this.registerUseCaseProvider = registerUseCaseProvider;
    this.authManagerProvider = authManagerProvider;
  }

  @Override
  public RegisterViewModel get() {
    return newInstance(registerUseCaseProvider.get(), authManagerProvider.get());
  }

  public static RegisterViewModel_Factory create(
      javax.inject.Provider<RegisterUseCase> registerUseCaseProvider,
      javax.inject.Provider<AuthManager> authManagerProvider) {
    return new RegisterViewModel_Factory(Providers.asDaggerProvider(registerUseCaseProvider), Providers.asDaggerProvider(authManagerProvider));
  }

  public static RegisterViewModel_Factory create(Provider<RegisterUseCase> registerUseCaseProvider,
      Provider<AuthManager> authManagerProvider) {
    return new RegisterViewModel_Factory(registerUseCaseProvider, authManagerProvider);
  }

  public static RegisterViewModel newInstance(RegisterUseCase registerUseCase,
      AuthManager authManager) {
    return new RegisterViewModel(registerUseCase, authManager);
  }
}
