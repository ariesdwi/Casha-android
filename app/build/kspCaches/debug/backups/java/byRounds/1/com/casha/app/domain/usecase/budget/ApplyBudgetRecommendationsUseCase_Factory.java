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
public final class ApplyBudgetRecommendationsUseCase_Factory implements Factory<ApplyBudgetRecommendationsUseCase> {
  private final Provider<BudgetRepository> repositoryProvider;

  public ApplyBudgetRecommendationsUseCase_Factory(Provider<BudgetRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ApplyBudgetRecommendationsUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static ApplyBudgetRecommendationsUseCase_Factory create(
      javax.inject.Provider<BudgetRepository> repositoryProvider) {
    return new ApplyBudgetRecommendationsUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static ApplyBudgetRecommendationsUseCase_Factory create(
      Provider<BudgetRepository> repositoryProvider) {
    return new ApplyBudgetRecommendationsUseCase_Factory(repositoryProvider);
  }

  public static ApplyBudgetRecommendationsUseCase newInstance(BudgetRepository repository) {
    return new ApplyBudgetRecommendationsUseCase(repository);
  }
}
