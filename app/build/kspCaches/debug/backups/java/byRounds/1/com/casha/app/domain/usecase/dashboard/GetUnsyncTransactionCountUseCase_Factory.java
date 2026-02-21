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
public final class GetUnsyncTransactionCountUseCase_Factory implements Factory<GetUnsyncTransactionCountUseCase> {
  private final Provider<TransactionRepository> repositoryProvider;

  public GetUnsyncTransactionCountUseCase_Factory(
      Provider<TransactionRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetUnsyncTransactionCountUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetUnsyncTransactionCountUseCase_Factory create(
      javax.inject.Provider<TransactionRepository> repositoryProvider) {
    return new GetUnsyncTransactionCountUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static GetUnsyncTransactionCountUseCase_Factory create(
      Provider<TransactionRepository> repositoryProvider) {
    return new GetUnsyncTransactionCountUseCase_Factory(repositoryProvider);
  }

  public static GetUnsyncTransactionCountUseCase newInstance(TransactionRepository repository) {
    return new GetUnsyncTransactionCountUseCase(repository);
  }
}
