package com.casha.app.ui.feature.loading;

import com.casha.app.domain.usecase.auth.GetProfileUseCase;
import com.casha.app.domain.usecase.dashboard.CashflowSyncUseCase;
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
public final class AppLoadingViewModel_Factory implements Factory<AppLoadingViewModel> {
  private final Provider<GetProfileUseCase> getProfileUseCaseProvider;

  private final Provider<CashflowSyncUseCase> cashflowSyncUseCaseProvider;

  public AppLoadingViewModel_Factory(Provider<GetProfileUseCase> getProfileUseCaseProvider,
      Provider<CashflowSyncUseCase> cashflowSyncUseCaseProvider) {
    this.getProfileUseCaseProvider = getProfileUseCaseProvider;
    this.cashflowSyncUseCaseProvider = cashflowSyncUseCaseProvider;
  }

  @Override
  public AppLoadingViewModel get() {
    return newInstance(getProfileUseCaseProvider.get(), cashflowSyncUseCaseProvider.get());
  }

  public static AppLoadingViewModel_Factory create(
      javax.inject.Provider<GetProfileUseCase> getProfileUseCaseProvider,
      javax.inject.Provider<CashflowSyncUseCase> cashflowSyncUseCaseProvider) {
    return new AppLoadingViewModel_Factory(Providers.asDaggerProvider(getProfileUseCaseProvider), Providers.asDaggerProvider(cashflowSyncUseCaseProvider));
  }

  public static AppLoadingViewModel_Factory create(
      Provider<GetProfileUseCase> getProfileUseCaseProvider,
      Provider<CashflowSyncUseCase> cashflowSyncUseCaseProvider) {
    return new AppLoadingViewModel_Factory(getProfileUseCaseProvider, cashflowSyncUseCaseProvider);
  }

  public static AppLoadingViewModel newInstance(GetProfileUseCase getProfileUseCase,
      CashflowSyncUseCase cashflowSyncUseCase) {
    return new AppLoadingViewModel(getProfileUseCase, cashflowSyncUseCase);
  }
}
