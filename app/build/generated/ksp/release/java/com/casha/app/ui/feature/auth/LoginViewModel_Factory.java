package com.casha.app.ui.feature.auth;

import com.casha.app.core.auth.AuthManager;
import com.casha.app.domain.usecase.auth.GoogleLoginUseCase;
import com.casha.app.domain.usecase.auth.LoginUseCase;
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
public final class LoginViewModel_Factory implements Factory<LoginViewModel> {
  private final Provider<LoginUseCase> loginUseCaseProvider;

  private final Provider<GoogleLoginUseCase> googleLoginUseCaseProvider;

  private final Provider<AuthManager> authManagerProvider;

  public LoginViewModel_Factory(Provider<LoginUseCase> loginUseCaseProvider,
      Provider<GoogleLoginUseCase> googleLoginUseCaseProvider,
      Provider<AuthManager> authManagerProvider) {
    this.loginUseCaseProvider = loginUseCaseProvider;
    this.googleLoginUseCaseProvider = googleLoginUseCaseProvider;
    this.authManagerProvider = authManagerProvider;
  }

  @Override
  public LoginViewModel get() {
    return newInstance(loginUseCaseProvider.get(), googleLoginUseCaseProvider.get(), authManagerProvider.get());
  }

  public static LoginViewModel_Factory create(
      javax.inject.Provider<LoginUseCase> loginUseCaseProvider,
      javax.inject.Provider<GoogleLoginUseCase> googleLoginUseCaseProvider,
      javax.inject.Provider<AuthManager> authManagerProvider) {
    return new LoginViewModel_Factory(Providers.asDaggerProvider(loginUseCaseProvider), Providers.asDaggerProvider(googleLoginUseCaseProvider), Providers.asDaggerProvider(authManagerProvider));
  }

  public static LoginViewModel_Factory create(Provider<LoginUseCase> loginUseCaseProvider,
      Provider<GoogleLoginUseCase> googleLoginUseCaseProvider,
      Provider<AuthManager> authManagerProvider) {
    return new LoginViewModel_Factory(loginUseCaseProvider, googleLoginUseCaseProvider, authManagerProvider);
  }

  public static LoginViewModel newInstance(LoginUseCase loginUseCase,
      GoogleLoginUseCase googleLoginUseCase, AuthManager authManager) {
    return new LoginViewModel(loginUseCase, googleLoginUseCase, authManager);
  }
}
