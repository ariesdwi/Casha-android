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
public final class DeleteIncomeUseCase_Factory implements Factory<DeleteIncomeUseCase> {
  private final Provider<IncomeRepository> repositoryProvider;

  public DeleteIncomeUseCase_Factory(Provider<IncomeRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DeleteIncomeUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static DeleteIncomeUseCase_Factory create(
      javax.inject.Provider<IncomeRepository> repositoryProvider) {
    return new DeleteIncomeUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static DeleteIncomeUseCase_Factory create(Provider<IncomeRepository> repositoryProvider) {
    return new DeleteIncomeUseCase_Factory(repositoryProvider);
  }

  public static DeleteIncomeUseCase newInstance(IncomeRepository repository) {
    return new DeleteIncomeUseCase(repository);
  }
}
