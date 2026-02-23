package com.casha.app.domain.usecase.transaction;

import com.casha.app.domain.repository.IncomeRepository;
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
public final class AddIncomeUseCase_Factory implements Factory<AddIncomeUseCase> {
  private final Provider<IncomeRepository> repositoryProvider;

  public AddIncomeUseCase_Factory(Provider<IncomeRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AddIncomeUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static AddIncomeUseCase_Factory create(
      javax.inject.Provider<IncomeRepository> repositoryProvider) {
    return new AddIncomeUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static AddIncomeUseCase_Factory create(Provider<IncomeRepository> repositoryProvider) {
    return new AddIncomeUseCase_Factory(repositoryProvider);
  }

  public static AddIncomeUseCase newInstance(IncomeRepository repository) {
    return new AddIncomeUseCase(repository);
  }
}
