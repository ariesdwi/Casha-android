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
public final class BudgetSyncUseCase_Factory implements Factory<BudgetSyncUseCase> {
  private final Provider<BudgetRepository> repositoryProvider;

  public BudgetSyncUseCase_Factory(Provider<BudgetRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public BudgetSyncUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static BudgetSyncUseCase_Factory create(
      javax.inject.Provider<BudgetRepository> repositoryProvider) {
    return new BudgetSyncUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static BudgetSyncUseCase_Factory create(Provider<BudgetRepository> repositoryProvider) {
    return new BudgetSyncUseCase_Factory(repositoryProvider);
  }

  public static BudgetSyncUseCase newInstance(BudgetRepository repository) {
    return new BudgetSyncUseCase(repository);
  }
}
