package com.casha.app.domain.usecase.cashflow;

import com.casha.app.domain.repository.CashflowRepository;
import com.casha.app.domain.repository.IncomeRepository;
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
public final class CashflowSyncUseCase_Factory implements Factory<CashflowSyncUseCase> {
  private final Provider<CashflowRepository> cashflowRepositoryProvider;

  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<IncomeRepository> incomeRepositoryProvider;

  public CashflowSyncUseCase_Factory(Provider<CashflowRepository> cashflowRepositoryProvider,
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<IncomeRepository> incomeRepositoryProvider) {
    this.cashflowRepositoryProvider = cashflowRepositoryProvider;
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.incomeRepositoryProvider = incomeRepositoryProvider;
  }

  @Override
  public CashflowSyncUseCase get() {
    return newInstance(cashflowRepositoryProvider.get(), transactionRepositoryProvider.get(), incomeRepositoryProvider.get());
  }

  public static CashflowSyncUseCase_Factory create(
      javax.inject.Provider<CashflowRepository> cashflowRepositoryProvider,
      javax.inject.Provider<TransactionRepository> transactionRepositoryProvider,
      javax.inject.Provider<IncomeRepository> incomeRepositoryProvider) {
    return new CashflowSyncUseCase_Factory(Providers.asDaggerProvider(cashflowRepositoryProvider), Providers.asDaggerProvider(transactionRepositoryProvider), Providers.asDaggerProvider(incomeRepositoryProvider));
  }

  public static CashflowSyncUseCase_Factory create(
      Provider<CashflowRepository> cashflowRepositoryProvider,
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<IncomeRepository> incomeRepositoryProvider) {
    return new CashflowSyncUseCase_Factory(cashflowRepositoryProvider, transactionRepositoryProvider, incomeRepositoryProvider);
  }

  public static CashflowSyncUseCase newInstance(CashflowRepository cashflowRepository,
      TransactionRepository transactionRepository, IncomeRepository incomeRepository) {
    return new CashflowSyncUseCase(cashflowRepository, transactionRepository, incomeRepository);
  }
}
