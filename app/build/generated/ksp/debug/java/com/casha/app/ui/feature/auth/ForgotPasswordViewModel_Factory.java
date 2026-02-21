package com.casha.app.ui.feature.auth;

import com.casha.app.domain.usecase.auth.ResetPasswordUseCase;
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
public final class ForgotPasswordViewModel_Factory implements Factory<ForgotPasswordViewModel> {
  private final Provider<ResetPasswordUseCase> resetPasswordUseCaseProvider;

  public ForgotPasswordViewModel_Factory(
      Provider<ResetPasswordUseCase> resetPasswordUseCaseProvider) {
    this.resetPasswordUseCaseProvider = resetPasswordUseCaseProvider;
  }

  @Override
  public ForgotPasswordViewModel get() {
    return newInstance(resetPasswordUseCaseProvider.get());
  }

  public static ForgotPasswordViewModel_Factory create(
      javax.inject.Provider<ResetPasswordUseCase> resetPasswordUseCaseProvider) {
    return new ForgotPasswordViewModel_Factory(Providers.asDaggerProvider(resetPasswordUseCaseProvider));
  }

  public static ForgotPasswordViewModel_Factory create(
      Provider<ResetPasswordUseCase> resetPasswordUseCaseProvider) {
    return new ForgotPasswordViewModel_Factory(resetPasswordUseCaseProvider);
  }

  public static ForgotPasswordViewModel newInstance(ResetPasswordUseCase resetPasswordUseCase) {
    return new ForgotPasswordViewModel(resetPasswordUseCase);
  }
}
