package com.casha.app.domain.usecase.budget;

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
public final class RecalculateBudgetSpentUseCase_Factory implements Factory<RecalculateBudgetSpentUseCase> {
  private final Provider<BudgetSyncUseCase> budgetSyncUseCaseProvider;

  public RecalculateBudgetSpentUseCase_Factory(
      Provider<BudgetSyncUseCase> budgetSyncUseCaseProvider) {
    this.budgetSyncUseCaseProvider = budgetSyncUseCaseProvider;
  }

  @Override
  public RecalculateBudgetSpentUseCase get() {
    return newInstance(budgetSyncUseCaseProvider.get());
  }

  public static RecalculateBudgetSpentUseCase_Factory create(
      javax.inject.Provider<BudgetSyncUseCase> budgetSyncUseCaseProvider) {
    return new RecalculateBudgetSpentUseCase_Factory(Providers.asDaggerProvider(budgetSyncUseCaseProvider));
  }

  public static RecalculateBudgetSpentUseCase_Factory create(
      Provider<BudgetSyncUseCase> budgetSyncUseCaseProvider) {
    return new RecalculateBudgetSpentUseCase_Factory(budgetSyncUseCaseProvider);
  }

  public static RecalculateBudgetSpentUseCase newInstance(BudgetSyncUseCase budgetSyncUseCase) {
    return new RecalculateBudgetSpentUseCase(budgetSyncUseCase);
  }
}
