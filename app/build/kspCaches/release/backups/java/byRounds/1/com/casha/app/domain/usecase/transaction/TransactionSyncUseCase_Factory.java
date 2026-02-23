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
public final class TransactionSyncUseCase_Factory implements Factory<TransactionSyncUseCase> {
  private final Provider<TransactionRepository> repositoryProvider;

  public TransactionSyncUseCase_Factory(Provider<TransactionRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public TransactionSyncUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static TransactionSyncUseCase_Factory create(
      javax.inject.Provider<TransactionRepository> repositoryProvider) {
    return new TransactionSyncUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static TransactionSyncUseCase_Factory create(
      Provider<TransactionRepository> repositoryProvider) {
    return new TransactionSyncUseCase_Factory(repositoryProvider);
  }

  public static TransactionSyncUseCase newInstance(TransactionRepository repository) {
    return new TransactionSyncUseCase(repository);
  }
}
