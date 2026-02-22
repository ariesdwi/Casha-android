package com.casha.app.ui.feature.dashboard;

import com.casha.app.core.auth.AuthManager;
import com.casha.app.core.network.NetworkMonitor;
import com.casha.app.core.network.SyncEventBus;
import com.casha.app.domain.usecase.auth.GetProfileUseCase;
import com.casha.app.domain.usecase.dashboard.CashflowSyncUseCase;
import com.casha.app.domain.usecase.dashboard.GetCashflowHistoryUseCase;
import com.casha.app.domain.usecase.dashboard.GetCashflowSummaryUseCase;
import com.casha.app.domain.usecase.dashboard.GetRecentTransactionsUseCase;
import com.casha.app.domain.usecase.dashboard.GetSpendingReportUseCase;
import com.casha.app.domain.usecase.dashboard.GetTotalSpendingUseCase;
import com.casha.app.domain.usecase.dashboard.GetUnsyncTransactionCountUseCase;
import com.casha.app.domain.usecase.dashboard.TransactionSyncUseCase;
import com.casha.app.domain.usecase.goal.GetGoalSummaryUseCase;
import com.casha.app.domain.usecase.goal.GetGoalsUseCase;
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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<GetRecentTransactionsUseCase> getRecentTransactionsUseCaseProvider;

  private final Provider<GetTotalSpendingUseCase> getTotalSpendingUseCaseProvider;

  private final Provider<GetSpendingReportUseCase> getSpendingReportUseCaseProvider;

  private final Provider<GetUnsyncTransactionCountUseCase> getUnsyncTransactionCountUseCaseProvider;

  private final Provider<GetCashflowHistoryUseCase> getCashflowHistoryUseCaseProvider;

  private final Provider<GetCashflowSummaryUseCase> getCashflowSummaryUseCaseProvider;

  private final Provider<GetGoalsUseCase> getGoalsUseCaseProvider;

  private final Provider<GetGoalSummaryUseCase> getGoalSummaryUseCaseProvider;

  private final Provider<CashflowSyncUseCase> cashflowSyncUseCaseProvider;

  private final Provider<TransactionSyncUseCase> transactionSyncUseCaseProvider;

  private final Provider<GetProfileUseCase> getProfileUseCaseProvider;

  private final Provider<AuthManager> authManagerProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  private final Provider<SyncEventBus> syncEventBusProvider;

  public DashboardViewModel_Factory(
      Provider<GetRecentTransactionsUseCase> getRecentTransactionsUseCaseProvider,
      Provider<GetTotalSpendingUseCase> getTotalSpendingUseCaseProvider,
      Provider<GetSpendingReportUseCase> getSpendingReportUseCaseProvider,
      Provider<GetUnsyncTransactionCountUseCase> getUnsyncTransactionCountUseCaseProvider,
      Provider<GetCashflowHistoryUseCase> getCashflowHistoryUseCaseProvider,
      Provider<GetCashflowSummaryUseCase> getCashflowSummaryUseCaseProvider,
      Provider<GetGoalsUseCase> getGoalsUseCaseProvider,
      Provider<GetGoalSummaryUseCase> getGoalSummaryUseCaseProvider,
      Provider<CashflowSyncUseCase> cashflowSyncUseCaseProvider,
      Provider<TransactionSyncUseCase> transactionSyncUseCaseProvider,
      Provider<GetProfileUseCase> getProfileUseCaseProvider,
      Provider<AuthManager> authManagerProvider, Provider<NetworkMonitor> networkMonitorProvider,
      Provider<SyncEventBus> syncEventBusProvider) {
    this.getRecentTransactionsUseCaseProvider = getRecentTransactionsUseCaseProvider;
    this.getTotalSpendingUseCaseProvider = getTotalSpendingUseCaseProvider;
    this.getSpendingReportUseCaseProvider = getSpendingReportUseCaseProvider;
    this.getUnsyncTransactionCountUseCaseProvider = getUnsyncTransactionCountUseCaseProvider;
    this.getCashflowHistoryUseCaseProvider = getCashflowHistoryUseCaseProvider;
    this.getCashflowSummaryUseCaseProvider = getCashflowSummaryUseCaseProvider;
    this.getGoalsUseCaseProvider = getGoalsUseCaseProvider;
    this.getGoalSummaryUseCaseProvider = getGoalSummaryUseCaseProvider;
    this.cashflowSyncUseCaseProvider = cashflowSyncUseCaseProvider;
    this.transactionSyncUseCaseProvider = transactionSyncUseCaseProvider;
    this.getProfileUseCaseProvider = getProfileUseCaseProvider;
    this.authManagerProvider = authManagerProvider;
    this.networkMonitorProvider = networkMonitorProvider;
    this.syncEventBusProvider = syncEventBusProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(getRecentTransactionsUseCaseProvider.get(), getTotalSpendingUseCaseProvider.get(), getSpendingReportUseCaseProvider.get(), getUnsyncTransactionCountUseCaseProvider.get(), getCashflowHistoryUseCaseProvider.get(), getCashflowSummaryUseCaseProvider.get(), getGoalsUseCaseProvider.get(), getGoalSummaryUseCaseProvider.get(), cashflowSyncUseCaseProvider.get(), transactionSyncUseCaseProvider.get(), getProfileUseCaseProvider.get(), authManagerProvider.get(), networkMonitorProvider.get(), syncEventBusProvider.get());
  }

  public static DashboardViewModel_Factory create(
      javax.inject.Provider<GetRecentTransactionsUseCase> getRecentTransactionsUseCaseProvider,
      javax.inject.Provider<GetTotalSpendingUseCase> getTotalSpendingUseCaseProvider,
      javax.inject.Provider<GetSpendingReportUseCase> getSpendingReportUseCaseProvider,
      javax.inject.Provider<GetUnsyncTransactionCountUseCase> getUnsyncTransactionCountUseCaseProvider,
      javax.inject.Provider<GetCashflowHistoryUseCase> getCashflowHistoryUseCaseProvider,
      javax.inject.Provider<GetCashflowSummaryUseCase> getCashflowSummaryUseCaseProvider,
      javax.inject.Provider<GetGoalsUseCase> getGoalsUseCaseProvider,
      javax.inject.Provider<GetGoalSummaryUseCase> getGoalSummaryUseCaseProvider,
      javax.inject.Provider<CashflowSyncUseCase> cashflowSyncUseCaseProvider,
      javax.inject.Provider<TransactionSyncUseCase> transactionSyncUseCaseProvider,
      javax.inject.Provider<GetProfileUseCase> getProfileUseCaseProvider,
      javax.inject.Provider<AuthManager> authManagerProvider,
      javax.inject.Provider<NetworkMonitor> networkMonitorProvider,
      javax.inject.Provider<SyncEventBus> syncEventBusProvider) {
    return new DashboardViewModel_Factory(Providers.asDaggerProvider(getRecentTransactionsUseCaseProvider), Providers.asDaggerProvider(getTotalSpendingUseCaseProvider), Providers.asDaggerProvider(getSpendingReportUseCaseProvider), Providers.asDaggerProvider(getUnsyncTransactionCountUseCaseProvider), Providers.asDaggerProvider(getCashflowHistoryUseCaseProvider), Providers.asDaggerProvider(getCashflowSummaryUseCaseProvider), Providers.asDaggerProvider(getGoalsUseCaseProvider), Providers.asDaggerProvider(getGoalSummaryUseCaseProvider), Providers.asDaggerProvider(cashflowSyncUseCaseProvider), Providers.asDaggerProvider(transactionSyncUseCaseProvider), Providers.asDaggerProvider(getProfileUseCaseProvider), Providers.asDaggerProvider(authManagerProvider), Providers.asDaggerProvider(networkMonitorProvider), Providers.asDaggerProvider(syncEventBusProvider));
  }

  public static DashboardViewModel_Factory create(
      Provider<GetRecentTransactionsUseCase> getRecentTransactionsUseCaseProvider,
      Provider<GetTotalSpendingUseCase> getTotalSpendingUseCaseProvider,
      Provider<GetSpendingReportUseCase> getSpendingReportUseCaseProvider,
      Provider<GetUnsyncTransactionCountUseCase> getUnsyncTransactionCountUseCaseProvider,
      Provider<GetCashflowHistoryUseCase> getCashflowHistoryUseCaseProvider,
      Provider<GetCashflowSummaryUseCase> getCashflowSummaryUseCaseProvider,
      Provider<GetGoalsUseCase> getGoalsUseCaseProvider,
      Provider<GetGoalSummaryUseCase> getGoalSummaryUseCaseProvider,
      Provider<CashflowSyncUseCase> cashflowSyncUseCaseProvider,
      Provider<TransactionSyncUseCase> transactionSyncUseCaseProvider,
      Provider<GetProfileUseCase> getProfileUseCaseProvider,
      Provider<AuthManager> authManagerProvider, Provider<NetworkMonitor> networkMonitorProvider,
      Provider<SyncEventBus> syncEventBusProvider) {
    return new DashboardViewModel_Factory(getRecentTransactionsUseCaseProvider, getTotalSpendingUseCaseProvider, getSpendingReportUseCaseProvider, getUnsyncTransactionCountUseCaseProvider, getCashflowHistoryUseCaseProvider, getCashflowSummaryUseCaseProvider, getGoalsUseCaseProvider, getGoalSummaryUseCaseProvider, cashflowSyncUseCaseProvider, transactionSyncUseCaseProvider, getProfileUseCaseProvider, authManagerProvider, networkMonitorProvider, syncEventBusProvider);
  }

  public static DashboardViewModel newInstance(
      GetRecentTransactionsUseCase getRecentTransactionsUseCase,
      GetTotalSpendingUseCase getTotalSpendingUseCase,
      GetSpendingReportUseCase getSpendingReportUseCase,
      GetUnsyncTransactionCountUseCase getUnsyncTransactionCountUseCase,
      GetCashflowHistoryUseCase getCashflowHistoryUseCase,
      GetCashflowSummaryUseCase getCashflowSummaryUseCase, GetGoalsUseCase getGoalsUseCase,
      GetGoalSummaryUseCase getGoalSummaryUseCase, CashflowSyncUseCase cashflowSyncUseCase,
      TransactionSyncUseCase transactionSyncUseCase, GetProfileUseCase getProfileUseCase,
      AuthManager authManager, NetworkMonitor networkMonitor, SyncEventBus syncEventBus) {
    return new DashboardViewModel(getRecentTransactionsUseCase, getTotalSpendingUseCase, getSpendingReportUseCase, getUnsyncTransactionCountUseCase, getCashflowHistoryUseCase, getCashflowSummaryUseCase, getGoalsUseCase, getGoalSummaryUseCase, cashflowSyncUseCase, transactionSyncUseCase, getProfileUseCase, authManager, networkMonitor, syncEventBus);
  }
}
