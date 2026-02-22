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
public final class GetBudgetsUseCase_Factory implements Factory<GetBudgetsUseCase> {
  private final Provider<BudgetRepository> repositoryProvider;

  public GetBudgetsUseCase_Factory(Provider<BudgetRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetBudgetsUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetBudgetsUseCase_Factory create(
      javax.inject.Provider<BudgetRepository> repositoryProvider) {
    return new GetBudgetsUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static GetBudgetsUseCase_Factory create(Provider<BudgetRepository> repositoryProvider) {
    return new GetBudgetsUseCase_Factory(repositoryProvider);
  }

  public static GetBudgetsUseCase newInstance(BudgetRepository repository) {
    return new GetBudgetsUseCase(repository);
  }
}
