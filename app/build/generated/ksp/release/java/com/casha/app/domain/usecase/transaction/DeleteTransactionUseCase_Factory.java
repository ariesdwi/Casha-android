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
public final class DeleteTransactionUseCase_Factory implements Factory<DeleteTransactionUseCase> {
  private final Provider<TransactionRepository> repositoryProvider;

  public DeleteTransactionUseCase_Factory(Provider<TransactionRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DeleteTransactionUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static DeleteTransactionUseCase_Factory create(
      javax.inject.Provider<TransactionRepository> repositoryProvider) {
    return new DeleteTransactionUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static DeleteTransactionUseCase_Factory create(
      Provider<TransactionRepository> repositoryProvider) {
    return new DeleteTransactionUseCase_Factory(repositoryProvider);
  }

  public static DeleteTransactionUseCase newInstance(TransactionRepository repository) {
    return new DeleteTransactionUseCase(repository);
  }
}
