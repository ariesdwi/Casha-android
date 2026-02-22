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
public final class DeleteBudgetUseCase_Factory implements Factory<DeleteBudgetUseCase> {
  private final Provider<BudgetRepository> repositoryProvider;

  public DeleteBudgetUseCase_Factory(Provider<BudgetRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DeleteBudgetUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static DeleteBudgetUseCase_Factory create(
      javax.inject.Provider<BudgetRepository> repositoryProvider) {
    return new DeleteBudgetUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static DeleteBudgetUseCase_Factory create(Provider<BudgetRepository> repositoryProvider) {
    return new DeleteBudgetUseCase_Factory(repositoryProvider);
  }

  public static DeleteBudgetUseCase newInstance(BudgetRepository repository) {
    return new DeleteBudgetUseCase(repository);
  }
}
