package com.casha.app.domain.usecase.dashboard;

import com.casha.app.domain.repository.CashflowRepository;
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
public final class GetCashflowHistoryUseCase_Factory implements Factory<GetCashflowHistoryUseCase> {
  private final Provider<CashflowRepository> repositoryProvider;

  public GetCashflowHistoryUseCase_Factory(Provider<CashflowRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetCashflowHistoryUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetCashflowHistoryUseCase_Factory create(
      javax.inject.Provider<CashflowRepository> repositoryProvider) {
    return new GetCashflowHistoryUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static GetCashflowHistoryUseCase_Factory create(
      Provider<CashflowRepository> repositoryProvider) {
    return new GetCashflowHistoryUseCase_Factory(repositoryProvider);
  }

  public static GetCashflowHistoryUseCase newInstance(CashflowRepository repository) {
    return new GetCashflowHistoryUseCase(repository);
  }
}
