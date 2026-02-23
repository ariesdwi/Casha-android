package com.casha.app.domain.usecase.dashboard;

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
public final class TransactionSyncUseCase_Factory implements Factory<TransactionSyncUseCase> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  public TransactionSyncUseCase_Factory(
      Provider<TransactionRepository> transactionRepositoryProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
  }

  @Override
  public TransactionSyncUseCase get() {
    return newInstance(transactionRepositoryProvider.get());
  }

  public static TransactionSyncUseCase_Factory create(
      javax.inject.Provider<TransactionRepository> transactionRepositoryProvider) {
    return new TransactionSyncUseCase_Factory(Providers.asDaggerProvider(transactionRepositoryProvider));
  }

  public static TransactionSyncUseCase_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider) {
    return new TransactionSyncUseCase_Factory(transactionRepositoryProvider);
  }

  public static TransactionSyncUseCase newInstance(TransactionRepository transactionRepository) {
    return new TransactionSyncUseCase(transactionRepository);
  }
}
