package com.casha.app.domain.usecase.report;

import com.casha.app.domain.repository.TransactionRepository;
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
public final class GetCategorySpendingUseCase_Factory implements Factory<GetCategorySpendingUseCase> {
  private final Provider<TransactionRepository> repositoryProvider;

  public GetCategorySpendingUseCase_Factory(Provider<TransactionRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetCategorySpendingUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetCategorySpendingUseCase_Factory create(
      javax.inject.Provider<TransactionRepository> repositoryProvider) {
    return new GetCategorySpendingUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static GetCategorySpendingUseCase_Factory create(
      Provider<TransactionRepository> repositoryProvider) {
    return new GetCategorySpendingUseCase_Factory(repositoryProvider);
  }

  public static GetCategorySpendingUseCase newInstance(TransactionRepository repository) {
    return new GetCategorySpendingUseCase(repository);
  }
}
