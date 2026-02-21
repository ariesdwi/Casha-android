package com.casha.app.ui.feature.profile;

import com.casha.app.domain.usecase.auth.logout.DeleteAllLocalDataUseCase;
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
  private final Provider<DeleteAllLocalDataUseCase> deleteAllLocalDataUseCaseProvider;

  public ProfileViewModel_Factory(
      Provider<DeleteAllLocalDataUseCase> deleteAllLocalDataUseCaseProvider) {
    this.deleteAllLocalDataUseCaseProvider = deleteAllLocalDataUseCaseProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(deleteAllLocalDataUseCaseProvider.get());
  }

  public static ProfileViewModel_Factory create(
      javax.inject.Provider<DeleteAllLocalDataUseCase> deleteAllLocalDataUseCaseProvider) {
    return new ProfileViewModel_Factory(Providers.asDaggerProvider(deleteAllLocalDataUseCaseProvider));
  }

  public static ProfileViewModel_Factory create(
      Provider<DeleteAllLocalDataUseCase> deleteAllLocalDataUseCaseProvider) {
    return new ProfileViewModel_Factory(deleteAllLocalDataUseCaseProvider);
  }

  public static ProfileViewModel newInstance(DeleteAllLocalDataUseCase deleteAllLocalDataUseCase) {
    return new ProfileViewModel(deleteAllLocalDataUseCase);
  }
}
