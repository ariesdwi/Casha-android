package com.casha.app.domain.usecase.budget;

import com.casha.app.domain.repository.BudgetRepository;
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
public final class GetBudgetSummaryUseCase_Factory implements Factory<GetBudgetSummaryUseCase> {
  private final Provider<BudgetRepository> repositoryProvider;

  public GetBudgetSummaryUseCase_Factory(Provider<BudgetRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetBudgetSummaryUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetBudgetSummaryUseCase_Factory create(
      javax.inject.Provider<BudgetRepository> repositoryProvider) {
    return new GetBudgetSummaryUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static GetBudgetSummaryUseCase_Factory create(
      Provider<BudgetRepository> repositoryProvider) {
    return new GetBudgetSummaryUseCase_Factory(repositoryProvider);
  }

  public static GetBudgetSummaryUseCase newInstance(BudgetRepository repository) {
    return new GetBudgetSummaryUseCase(repository);
  }
}
