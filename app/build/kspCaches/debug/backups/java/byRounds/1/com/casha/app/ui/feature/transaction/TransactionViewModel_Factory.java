package com.casha.app.ui.feature.transaction;

import com.casha.app.core.network.SyncEventBus;
import com.casha.app.domain.usecase.category.CategorySyncUseCase;
import com.casha.app.domain.usecase.dashboard.GetCashflowHistoryUseCase;
import com.casha.app.domain.usecase.transaction.AddIncomeUseCase;
import com.casha.app.domain.usecase.transaction.AddTransactionUseCase;
import com.casha.app.domain.usecase.transaction.DeleteIncomeUseCase;
import com.casha.app.domain.usecase.transaction.DeleteTransactionUseCase;
import com.casha.app.domain.usecase.transaction.GetIncomesUseCase;
import com.casha.app.domain.usecase.transaction.GetTransactionsUseCase;
import com.casha.app.domain.usecase.transaction.SyncTransactionsUseCase;
import com.casha.app.domain.usecase.transaction.UpdateIncomeUseCase;
import com.casha.app.domain.usecase.transaction.UpdateTransactionUseCase;
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
public final class TransactionViewModel_Factory implements Factory<TransactionViewModel> {
  private final Provider<GetCashflowHistoryUseCase> getCashflowHistoryUseCaseProvider;

  private final Provider<GetTransactionsUseCase> getTransactionsUseCaseProvider;

  private final Provider<AddTransactionUseCase> addTransactionUseCaseProvider;

  private final Provider<AddIncomeUseCase> addIncomeUseCaseProvider;

  private final Provider<UpdateTransactionUseCase> updateTransactionUseCaseProvider;

  private final Provider<DeleteTransactionUseCase> deleteTransactionUseCaseProvider;

  private final Provider<SyncTransactionsUseCase> syncTransactionsUseCaseProvider;

  private final Provider<CategorySyncUseCase> categorySyncUseCaseProvider;

  private final Provider<GetIncomesUseCase> getIncomesUseCaseProvider;

  private final Provider<UpdateIncomeUseCase> updateIncomeUseCaseProvider;

  private final Provider<DeleteIncomeUseCase> deleteIncomeUseCaseProvider;

  private final Provider<SyncEventBus> syncEventBusProvider;

  public TransactionViewModel_Factory(
      Provider<GetCashflowHistoryUseCase> getCashflowHistoryUseCaseProvider,
      Provider<GetTransactionsUseCase> getTransactionsUseCaseProvider,
      Provider<AddTransactionUseCase> addTransactionUseCaseProvider,
      Provider<AddIncomeUseCase> addIncomeUseCaseProvider,
      Provider<UpdateTransactionUseCase> updateTransactionUseCaseProvider,
      Provider<DeleteTransactionUseCase> deleteTransactionUseCaseProvider,
      Provider<SyncTransactionsUseCase> syncTransactionsUseCaseProvider,
      Provider<CategorySyncUseCase> categorySyncUseCaseProvider,
      Provider<GetIncomesUseCase> getIncomesUseCaseProvider,
      Provider<UpdateIncomeUseCase> updateIncomeUseCaseProvider,
      Provider<DeleteIncomeUseCase> deleteIncomeUseCaseProvider,
      Provider<SyncEventBus> syncEventBusProvider) {
    this.getCashflowHistoryUseCaseProvider = getCashflowHistoryUseCaseProvider;
    this.getTransactionsUseCaseProvider = getTransactionsUseCaseProvider;
    this.addTransactionUseCaseProvider = addTransactionUseCaseProvider;
    this.addIncomeUseCaseProvider = addIncomeUseCaseProvider;
    this.updateTransactionUseCaseProvider = updateTransactionUseCaseProvider;
    this.deleteTransactionUseCaseProvider = deleteTransactionUseCaseProvider;
    this.syncTransactionsUseCaseProvider = syncTransactionsUseCaseProvider;
    this.categorySyncUseCaseProvider = categorySyncUseCaseProvider;
    this.getIncomesUseCaseProvider = getIncomesUseCaseProvider;
    this.updateIncomeUseCaseProvider = updateIncomeUseCaseProvider;
    this.deleteIncomeUseCaseProvider = deleteIncomeUseCaseProvider;
    this.syncEventBusProvider = syncEventBusProvider;
  }

  @Override
  public TransactionViewModel get() {
    return newInstance(getCashflowHistoryUseCaseProvider.get(), getTransactionsUseCaseProvider.get(), addTransactionUseCaseProvider.get(), addIncomeUseCaseProvider.get(), updateTransactionUseCaseProvider.get(), deleteTransactionUseCaseProvider.get(), syncTransactionsUseCaseProvider.get(), categorySyncUseCaseProvider.get(), getIncomesUseCaseProvider.get(), updateIncomeUseCaseProvider.get(), deleteIncomeUseCaseProvider.get(), syncEventBusProvider.get());
  }

  public static TransactionViewModel_Factory create(
      javax.inject.Provider<GetCashflowHistoryUseCase> getCashflowHistoryUseCaseProvider,
      javax.inject.Provider<GetTransactionsUseCase> getTransactionsUseCaseProvider,
      javax.inject.Provider<AddTransactionUseCase> addTransactionUseCaseProvider,
      javax.inject.Provider<AddIncomeUseCase> addIncomeUseCaseProvider,
      javax.inject.Provider<UpdateTransactionUseCase> updateTransactionUseCaseProvider,
      javax.inject.Provider<DeleteTransactionUseCase> deleteTransactionUseCaseProvider,
      javax.inject.Provider<SyncTransactionsUseCase> syncTransactionsUseCaseProvider,
      javax.inject.Provider<CategorySyncUseCase> categorySyncUseCaseProvider,
      javax.inject.Provider<GetIncomesUseCase> getIncomesUseCaseProvider,
      javax.inject.Provider<UpdateIncomeUseCase> updateIncomeUseCaseProvider,
      javax.inject.Provider<DeleteIncomeUseCase> deleteIncomeUseCaseProvider,
      javax.inject.Provider<SyncEventBus> syncEventBusProvider) {
    return new TransactionViewModel_Factory(Providers.asDaggerProvider(getCashflowHistoryUseCaseProvider), Providers.asDaggerProvider(getTransactionsUseCaseProvider), Providers.asDaggerProvider(addTransactionUseCaseProvider), Providers.asDaggerProvider(addIncomeUseCaseProvider), Providers.asDaggerProvider(updateTransactionUseCaseProvider), Providers.asDaggerProvider(deleteTransactionUseCaseProvider), Providers.asDaggerProvider(syncTransactionsUseCaseProvider), Providers.asDaggerProvider(categorySyncUseCaseProvider), Providers.asDaggerProvider(getIncomesUseCaseProvider), Providers.asDaggerProvider(updateIncomeUseCaseProvider), Providers.asDaggerProvider(deleteIncomeUseCaseProvider), Providers.asDaggerProvider(syncEventBusProvider));
  }

  public static TransactionViewModel_Factory create(
      Provider<GetCashflowHistoryUseCase> getCashflowHistoryUseCaseProvider,
      Provider<GetTransactionsUseCase> getTransactionsUseCaseProvider,
      Provider<AddTransactionUseCase> addTransactionUseCaseProvider,
      Provider<AddIncomeUseCase> addIncomeUseCaseProvider,
      Provider<UpdateTransactionUseCase> updateTransactionUseCaseProvider,
      Provider<DeleteTransactionUseCase> deleteTransactionUseCaseProvider,
      Provider<SyncTransactionsUseCase> syncTransactionsUseCaseProvider,
      Provider<CategorySyncUseCase> categorySyncUseCaseProvider,
      Provider<GetIncomesUseCase> getIncomesUseCaseProvider,
      Provider<UpdateIncomeUseCase> updateIncomeUseCaseProvider,
      Provider<DeleteIncomeUseCase> deleteIncomeUseCaseProvider,
      Provider<SyncEventBus> syncEventBusProvider) {
    return new TransactionViewModel_Factory(getCashflowHistoryUseCaseProvider, getTransactionsUseCaseProvider, addTransactionUseCaseProvider, addIncomeUseCaseProvider, updateTransactionUseCaseProvider, deleteTransactionUseCaseProvider, syncTransactionsUseCaseProvider, categorySyncUseCaseProvider, getIncomesUseCaseProvider, updateIncomeUseCaseProvider, deleteIncomeUseCaseProvider, syncEventBusProvider);
  }

  public static TransactionViewModel newInstance(
      GetCashflowHistoryUseCase getCashflowHistoryUseCase,
      GetTransactionsUseCase getTransactionsUseCase, AddTransactionUseCase addTransactionUseCase,
      AddIncomeUseCase addIncomeUseCase, UpdateTransactionUseCase updateTransactionUseCase,
      DeleteTransactionUseCase deleteTransactionUseCase,
      SyncTransactionsUseCase syncTransactionsUseCase, CategorySyncUseCase categorySyncUseCase,
      GetIncomesUseCase getIncomesUseCase, UpdateIncomeUseCase updateIncomeUseCase,
      DeleteIncomeUseCase deleteIncomeUseCase, SyncEventBus syncEventBus) {
    return new TransactionViewModel(getCashflowHistoryUseCase, getTransactionsUseCase, addTransactionUseCase, addIncomeUseCase, updateTransactionUseCase, deleteTransactionUseCase, syncTransactionsUseCase, categorySyncUseCase, getIncomesUseCase, updateIncomeUseCase, deleteIncomeUseCase, syncEventBus);
  }
}
