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
public final class GetBudgetRecommendationsUseCase_Factory implements Factory<GetBudgetRecommendationsUseCase> {
  private final Provider<BudgetRepository> repositoryProvider;

  public GetBudgetRecommendationsUseCase_Factory(Provider<BudgetRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetBudgetRecommendationsUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetBudgetRecommendationsUseCase_Factory create(
      javax.inject.Provider<BudgetRepository> repositoryProvider) {
    return new GetBudgetRecommendationsUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static GetBudgetRecommendationsUseCase_Factory create(
      Provider<BudgetRepository> repositoryProvider) {
    return new GetBudgetRecommendationsUseCase_Factory(repositoryProvider);
  }

  public static GetBudgetRecommendationsUseCase newInstance(BudgetRepository repository) {
    return new GetBudgetRecommendationsUseCase(repository);
  }
}
