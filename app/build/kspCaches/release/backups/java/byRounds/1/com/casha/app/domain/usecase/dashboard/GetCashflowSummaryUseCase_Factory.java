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
public final class GetCashflowSummaryUseCase_Factory implements Factory<GetCashflowSummaryUseCase> {
  private final Provider<CashflowRepository> repositoryProvider;

  public GetCashflowSummaryUseCase_Factory(Provider<CashflowRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetCashflowSummaryUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetCashflowSummaryUseCase_Factory create(
      javax.inject.Provider<CashflowRepository> repositoryProvider) {
    return new GetCashflowSummaryUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static GetCashflowSummaryUseCase_Factory create(
      Provider<CashflowRepository> repositoryProvider) {
    return new GetCashflowSummaryUseCase_Factory(repositoryProvider);
  }

  public static GetCashflowSummaryUseCase newInstance(CashflowRepository repository) {
    return new GetCashflowSummaryUseCase(repository);
  }
}
