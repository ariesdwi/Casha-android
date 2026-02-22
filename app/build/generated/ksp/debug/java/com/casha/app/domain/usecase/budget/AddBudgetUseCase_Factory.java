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
public final class AddBudgetUseCase_Factory implements Factory<AddBudgetUseCase> {
  private final Provider<BudgetRepository> repositoryProvider;

  public AddBudgetUseCase_Factory(Provider<BudgetRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AddBudgetUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static AddBudgetUseCase_Factory create(
      javax.inject.Provider<BudgetRepository> repositoryProvider) {
    return new AddBudgetUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static AddBudgetUseCase_Factory create(Provider<BudgetRepository> repositoryProvider) {
    return new AddBudgetUseCase_Factory(repositoryProvider);
  }

  public static AddBudgetUseCase newInstance(BudgetRepository repository) {
    return new AddBudgetUseCase(repository);
  }
}
