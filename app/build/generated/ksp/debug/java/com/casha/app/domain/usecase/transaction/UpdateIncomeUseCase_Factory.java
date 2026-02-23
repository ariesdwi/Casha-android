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
public final class UpdateIncomeUseCase_Factory implements Factory<UpdateIncomeUseCase> {
  private final Provider<IncomeRepository> repositoryProvider;

  public UpdateIncomeUseCase_Factory(Provider<IncomeRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public UpdateIncomeUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static UpdateIncomeUseCase_Factory create(
      javax.inject.Provider<IncomeRepository> repositoryProvider) {
    return new UpdateIncomeUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static UpdateIncomeUseCase_Factory create(Provider<IncomeRepository> repositoryProvider) {
    return new UpdateIncomeUseCase_Factory(repositoryProvider);
  }

  public static UpdateIncomeUseCase newInstance(IncomeRepository repository) {
    return new UpdateIncomeUseCase(repository);
  }
}
