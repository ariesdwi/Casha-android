package com.casha.app;

import com.casha.app.core.auth.AuthManager;
import com.casha.app.domain.usecase.auth.ResetPasswordUseCase;
import com.casha.app.domain.usecase.auth.UpdateProfileUseCase;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<AuthManager> authManagerProvider;

  private final Provider<ResetPasswordUseCase> resetPasswordUseCaseProvider;

  private final Provider<UpdateProfileUseCase> updateProfileUseCaseProvider;

  public MainActivity_MembersInjector(Provider<AuthManager> authManagerProvider,
      Provider<ResetPasswordUseCase> resetPasswordUseCaseProvider,
      Provider<UpdateProfileUseCase> updateProfileUseCaseProvider) {
    this.authManagerProvider = authManagerProvider;
    this.resetPasswordUseCaseProvider = resetPasswordUseCaseProvider;
    this.updateProfileUseCaseProvider = updateProfileUseCaseProvider;
  }

  public static MembersInjector<MainActivity> create(Provider<AuthManager> authManagerProvider,
      Provider<ResetPasswordUseCase> resetPasswordUseCaseProvider,
      Provider<UpdateProfileUseCase> updateProfileUseCaseProvider) {
    return new MainActivity_MembersInjector(authManagerProvider, resetPasswordUseCaseProvider, updateProfileUseCaseProvider);
  }

  public static MembersInjector<MainActivity> create(
      javax.inject.Provider<AuthManager> authManagerProvider,
      javax.inject.Provider<ResetPasswordUseCase> resetPasswordUseCaseProvider,
      javax.inject.Provider<UpdateProfileUseCase> updateProfileUseCaseProvider) {
    return new MainActivity_MembersInjector(Providers.asDaggerProvider(authManagerProvider), Providers.asDaggerProvider(resetPasswordUseCaseProvider), Providers.asDaggerProvider(updateProfileUseCaseProvider));
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectAuthManager(instance, authManagerProvider.get());
    injectResetPasswordUseCase(instance, resetPasswordUseCaseProvider.get());
    injectUpdateProfileUseCase(instance, updateProfileUseCaseProvider.get());
  }

  @InjectedFieldSignature("com.casha.app.MainActivity.authManager")
  public static void injectAuthManager(MainActivity instance, AuthManager authManager) {
    instance.authManager = authManager;
  }

  @InjectedFieldSignature("com.casha.app.MainActivity.resetPasswordUseCase")
  public static void injectResetPasswordUseCase(MainActivity instance,
      ResetPasswordUseCase resetPasswordUseCase) {
    instance.resetPasswordUseCase = resetPasswordUseCase;
  }

  @InjectedFieldSignature("com.casha.app.MainActivity.updateProfileUseCase")
  public static void injectUpdateProfileUseCase(MainActivity instance,
      UpdateProfileUseCase updateProfileUseCase) {
    instance.updateProfileUseCase = updateProfileUseCase;
  }
}
