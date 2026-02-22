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
public final class GetTransactionByCategoryUseCase_Factory implements Factory<GetTransactionByCategoryUseCase> {
  private final Provider<TransactionRepository> repositoryProvider;

  public GetTransactionByCategoryUseCase_Factory(
      Provider<TransactionRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetTransactionByCategoryUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetTransactionByCategoryUseCase_Factory create(
      javax.inject.Provider<TransactionRepository> repositoryProvider) {
    return new GetTransactionByCategoryUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static GetTransactionByCategoryUseCase_Factory create(
      Provider<TransactionRepository> repositoryProvider) {
    return new GetTransactionByCategoryUseCase_Factory(repositoryProvider);
  }

  public static GetTransactionByCategoryUseCase newInstance(TransactionRepository repository) {
    return new GetTransactionByCategoryUseCase(repository);
  }
}
