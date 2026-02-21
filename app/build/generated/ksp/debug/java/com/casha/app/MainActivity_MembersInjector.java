package com.casha.app;

import com.casha.app.core.auth.AuthManager;
import com.casha.app.domain.usecase.auth.logout.DeleteAllLocalDataUseCase;
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

  private final Provider<DeleteAllLocalDataUseCase> deleteAllLocalDataUseCaseProvider;

  public MainActivity_MembersInjector(Provider<AuthManager> authManagerProvider,
      Provider<DeleteAllLocalDataUseCase> deleteAllLocalDataUseCaseProvider) {
    this.authManagerProvider = authManagerProvider;
    this.deleteAllLocalDataUseCaseProvider = deleteAllLocalDataUseCaseProvider;
  }

  public static MembersInjector<MainActivity> create(Provider<AuthManager> authManagerProvider,
      Provider<DeleteAllLocalDataUseCase> deleteAllLocalDataUseCaseProvider) {
    return new MainActivity_MembersInjector(authManagerProvider, deleteAllLocalDataUseCaseProvider);
  }

  public static MembersInjector<MainActivity> create(
      javax.inject.Provider<AuthManager> authManagerProvider,
      javax.inject.Provider<DeleteAllLocalDataUseCase> deleteAllLocalDataUseCaseProvider) {
    return new MainActivity_MembersInjector(Providers.asDaggerProvider(authManagerProvider), Providers.asDaggerProvider(deleteAllLocalDataUseCaseProvider));
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectAuthManager(instance, authManagerProvider.get());
    injectDeleteAllLocalDataUseCase(instance, deleteAllLocalDataUseCaseProvider.get());
  }

  @InjectedFieldSignature("com.casha.app.MainActivity.authManager")
  public static void injectAuthManager(MainActivity instance, AuthManager authManager) {
    instance.authManager = authManager;
  }

  @InjectedFieldSignature("com.casha.app.MainActivity.deleteAllLocalDataUseCase")
  public static void injectDeleteAllLocalDataUseCase(MainActivity instance,
      DeleteAllLocalDataUseCase deleteAllLocalDataUseCase) {
    instance.deleteAllLocalDataUseCase = deleteAllLocalDataUseCase;
  }
}
