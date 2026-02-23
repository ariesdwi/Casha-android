package com.casha.app.domain.usecase.dashboard;

import com.casha.app.data.local.dao.IncomeDao;
import com.casha.app.data.local.dao.TransactionDao;
import com.casha.app.domain.repository.CashflowRepository;
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

  private final Provider<TransactionDao> transactionDaoProvider;

  private final Provider<IncomeDao> incomeDaoProvider;

  public CashflowSyncUseCase_Factory(Provider<CashflowRepository> cashflowRepositoryProvider,
      Provider<TransactionDao> transactionDaoProvider, Provider<IncomeDao> incomeDaoProvider) {
    this.cashflowRepositoryProvider = cashflowRepositoryProvider;
    this.transactionDaoProvider = transactionDaoProvider;
    this.incomeDaoProvider = incomeDaoProvider;
  }

  @Override
  public CashflowSyncUseCase get() {
    return newInstance(cashflowRepositoryProvider.get(), transactionDaoProvider.get(), incomeDaoProvider.get());
  }

  public static CashflowSyncUseCase_Factory create(
      javax.inject.Provider<CashflowRepository> cashflowRepositoryProvider,
      javax.inject.Provider<TransactionDao> transactionDaoProvider,
      javax.inject.Provider<IncomeDao> incomeDaoProvider) {
    return new CashflowSyncUseCase_Factory(Providers.asDaggerProvider(cashflowRepositoryProvider), Providers.asDaggerProvider(transactionDaoProvider), Providers.asDaggerProvider(incomeDaoProvider));
  }

  public static CashflowSyncUseCase_Factory create(
      Provider<CashflowRepository> cashflowRepositoryProvider,
      Provider<TransactionDao> transactionDaoProvider, Provider<IncomeDao> incomeDaoProvider) {
    return new CashflowSyncUseCase_Factory(cashflowRepositoryProvider, transactionDaoProvider, incomeDaoProvider);
  }

  public static CashflowSyncUseCase newInstance(CashflowRepository cashflowRepository,
      TransactionDao transactionDao, IncomeDao incomeDao) {
    return new CashflowSyncUseCase(cashflowRepository, transactionDao, incomeDao);
  }
}
