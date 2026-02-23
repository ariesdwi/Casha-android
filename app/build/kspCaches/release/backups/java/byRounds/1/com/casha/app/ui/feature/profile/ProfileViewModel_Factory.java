package com.casha.app.ui.feature.profile;

import com.casha.app.core.auth.AuthManager;
import com.casha.app.core.network.NetworkMonitor;
import com.casha.app.domain.usecase.auth.logout.DeleteAllLocalDataUseCase;
import com.casha.app.domain.usecase.profile.DeleteAccountUseCase;
import com.casha.app.domain.usecase.profile.GetProfileUseCase;
import com.casha.app.domain.usecase.profile.UpdateProfileUseCase;
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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<GetProfileUseCase> getProfileUseCaseProvider;

  private final Provider<UpdateProfileUseCase> updateProfileUseCaseProvider;

  private final Provider<DeleteAccountUseCase> deleteAccountUseCaseProvider;

  private final Provider<DeleteAllLocalDataUseCase> deleteAllLocalDataUseCaseProvider;

  private final Provider<AuthManager> authManagerProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  public ProfileViewModel_Factory(Provider<GetProfileUseCase> getProfileUseCaseProvider,
      Provider<UpdateProfileUseCase> updateProfileUseCaseProvider,
      Provider<DeleteAccountUseCase> deleteAccountUseCaseProvider,
      Provider<DeleteAllLocalDataUseCase> deleteAllLocalDataUseCaseProvider,
      Provider<AuthManager> authManagerProvider, Provider<NetworkMonitor> networkMonitorProvider) {
    this.getProfileUseCaseProvider = getProfileUseCaseProvider;
    this.updateProfileUseCaseProvider = updateProfileUseCaseProvider;
    this.deleteAccountUseCaseProvider = deleteAccountUseCaseProvider;
    this.deleteAllLocalDataUseCaseProvider = deleteAllLocalDataUseCaseProvider;
    this.authManagerProvider = authManagerProvider;
    this.networkMonitorProvider = networkMonitorProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(getProfileUseCaseProvider.get(), updateProfileUseCaseProvider.get(), deleteAccountUseCaseProvider.get(), deleteAllLocalDataUseCaseProvider.get(), authManagerProvider.get(), networkMonitorProvider.get());
  }

  public static ProfileViewModel_Factory create(
      javax.inject.Provider<GetProfileUseCase> getProfileUseCaseProvider,
      javax.inject.Provider<UpdateProfileUseCase> updateProfileUseCaseProvider,
      javax.inject.Provider<DeleteAccountUseCase> deleteAccountUseCaseProvider,
      javax.inject.Provider<DeleteAllLocalDataUseCase> deleteAllLocalDataUseCaseProvider,
      javax.inject.Provider<AuthManager> authManagerProvider,
      javax.inject.Provider<NetworkMonitor> networkMonitorProvider) {
    return new ProfileViewModel_Factory(Providers.asDaggerProvider(getProfileUseCaseProvider), Providers.asDaggerProvider(updateProfileUseCaseProvider), Providers.asDaggerProvider(deleteAccountUseCaseProvider), Providers.asDaggerProvider(deleteAllLocalDataUseCaseProvider), Providers.asDaggerProvider(authManagerProvider), Providers.asDaggerProvider(networkMonitorProvider));
  }

  public static ProfileViewModel_Factory create(
      Provider<GetProfileUseCase> getProfileUseCaseProvider,
      Provider<UpdateProfileUseCase> updateProfileUseCaseProvider,
      Provider<DeleteAccountUseCase> deleteAccountUseCaseProvider,
      Provider<DeleteAllLocalDataUseCase> deleteAllLocalDataUseCaseProvider,
      Provider<AuthManager> authManagerProvider, Provider<NetworkMonitor> networkMonitorProvider) {
    return new ProfileViewModel_Factory(getProfileUseCaseProvider, updateProfileUseCaseProvider, deleteAccountUseCaseProvider, deleteAllLocalDataUseCaseProvider, authManagerProvider, networkMonitorProvider);
  }

  public static ProfileViewModel newInstance(GetProfileUseCase getProfileUseCase,
      UpdateProfileUseCase updateProfileUseCase, DeleteAccountUseCase deleteAccountUseCase,
      DeleteAllLocalDataUseCase deleteAllLocalDataUseCase, AuthManager authManager,
      NetworkMonitor networkMonitor) {
    return new ProfileViewModel(getProfileUseCase, updateProfileUseCase, deleteAccountUseCase, deleteAllLocalDataUseCase, authManager, networkMonitor);
  }
}
