package com.casha.app.domain.usecase.transaction;

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
public final class GetTransactionsUseCase_Factory implements Factory<GetTransactionsUseCase> {
  private final Provider<TransactionRepository> repositoryProvider;

  public GetTransactionsUseCase_Factory(Provider<TransactionRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetTransactionsUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetTransactionsUseCase_Factory create(
      javax.inject.Provider<TransactionRepository> repositoryProvider) {
    return new GetTransactionsUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static GetTransactionsUseCase_Factory create(
      Provider<TransactionRepository> repositoryProvider) {
    return new GetTransactionsUseCase_Factory(repositoryProvider);
  }

  public static GetTransactionsUseCase newInstance(TransactionRepository repository) {
    return new GetTransactionsUseCase(repository);
  }
}
