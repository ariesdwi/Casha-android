package com.casha.app.ui.feature.auth;

import com.casha.app.core.auth.AuthManager;
import com.casha.app.domain.usecase.auth.UpdateProfileUseCase;
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
public final class SetupCurrencyViewModel_Factory implements Factory<SetupCurrencyViewModel> {
  private final Provider<AuthManager> authManagerProvider;

  private final Provider<UpdateProfileUseCase> updateProfileUseCaseProvider;

  public SetupCurrencyViewModel_Factory(Provider<AuthManager> authManagerProvider,
      Provider<UpdateProfileUseCase> updateProfileUseCaseProvider) {
    this.authManagerProvider = authManagerProvider;
    this.updateProfileUseCaseProvider = updateProfileUseCaseProvider;
  }

  @Override
  public SetupCurrencyViewModel get() {
    return newInstance(authManagerProvider.get(), updateProfileUseCaseProvider.get());
  }

  public static SetupCurrencyViewModel_Factory create(
      javax.inject.Provider<AuthManager> authManagerProvider,
      javax.inject.Provider<UpdateProfileUseCase> updateProfileUseCaseProvider) {
    return new SetupCurrencyViewModel_Factory(Providers.asDaggerProvider(authManagerProvider), Providers.asDaggerProvider(updateProfileUseCaseProvider));
  }

  public static SetupCurrencyViewModel_Factory create(Provider<AuthManager> authManagerProvider,
      Provider<UpdateProfileUseCase> updateProfileUseCaseProvider) {
    return new SetupCurrencyViewModel_Factory(authManagerProvider, updateProfileUseCaseProvider);
  }

  public static SetupCurrencyViewModel newInstance(AuthManager authManager,
      UpdateProfileUseCase updateProfileUseCase) {
    return new SetupCurrencyViewModel(authManager, updateProfileUseCase);
  }
}
