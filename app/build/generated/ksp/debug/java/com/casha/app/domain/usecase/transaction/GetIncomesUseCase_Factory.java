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
public final class GetIncomesUseCase_Factory implements Factory<GetIncomesUseCase> {
  private final Provider<IncomeRepository> repositoryProvider;

  public GetIncomesUseCase_Factory(Provider<IncomeRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetIncomesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetIncomesUseCase_Factory create(
      javax.inject.Provider<IncomeRepository> repositoryProvider) {
    return new GetIncomesUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static GetIncomesUseCase_Factory create(Provider<IncomeRepository> repositoryProvider) {
    return new GetIncomesUseCase_Factory(repositoryProvider);
  }

  public static GetIncomesUseCase newInstance(IncomeRepository repository) {
    return new GetIncomesUseCase(repository);
  }
}
