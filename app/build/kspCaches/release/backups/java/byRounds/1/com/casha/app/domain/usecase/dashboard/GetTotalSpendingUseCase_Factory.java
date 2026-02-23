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
public final class GetTotalSpendingUseCase_Factory implements Factory<GetTotalSpendingUseCase> {
  private final Provider<TransactionRepository> repositoryProvider;

  public GetTotalSpendingUseCase_Factory(Provider<TransactionRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetTotalSpendingUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetTotalSpendingUseCase_Factory create(
      javax.inject.Provider<TransactionRepository> repositoryProvider) {
    return new GetTotalSpendingUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static GetTotalSpendingUseCase_Factory create(
      Provider<TransactionRepository> repositoryProvider) {
    return new GetTotalSpendingUseCase_Factory(repositoryProvider);
  }

  public static GetTotalSpendingUseCase newInstance(TransactionRepository repository) {
    return new GetTotalSpendingUseCase(repository);
  }
}
