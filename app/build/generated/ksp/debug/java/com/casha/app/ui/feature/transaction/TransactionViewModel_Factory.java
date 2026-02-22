package com.casha.app.ui.feature.transaction;

import com.casha.app.core.network.SyncEventBus;
import com.casha.app.domain.usecase.category.CategorySyncUseCase;
import com.casha.app.domain.usecase.dashboard.GetCashflowHistoryUseCase;
import com.casha.app.domain.usecase.transaction.AddTransactionUseCase;
import com.casha.app.domain.usecase.transaction.DeleteTransactionUseCase;
import com.casha.app.domain.usecase.transaction.GetTransactionsUseCase;
import com.casha.app.domain.usecase.transaction.SyncTransactionsUseCase;
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

  private final Provider<UpdateTransactionUseCase> updateTransactionUseCaseProvider;

  private final Provider<DeleteTransactionUseCase> deleteTransactionUseCaseProvider;

  private final Provider<SyncTransactionsUseCase> syncTransactionsUseCaseProvider;

  private final Provider<CategorySyncUseCase> categorySyncUseCaseProvider;

  private final Provider<SyncEventBus> syncEventBusProvider;

  public TransactionViewModel_Factory(
      Provider<GetCashflowHistoryUseCase> getCashflowHistoryUseCaseProvider,
      Provider<GetTransactionsUseCase> getTransactionsUseCaseProvider,
      Provider<AddTransactionUseCase> addTransactionUseCaseProvider,
      Provider<UpdateTransactionUseCase> updateTransactionUseCaseProvider,
      Provider<DeleteTransactionUseCase> deleteTransactionUseCaseProvider,
      Provider<SyncTransactionsUseCase> syncTransactionsUseCaseProvider,
      Provider<CategorySyncUseCase> categorySyncUseCaseProvider,
      Provider<SyncEventBus> syncEventBusProvider) {
    this.getCashflowHistoryUseCaseProvider = getCashflowHistoryUseCaseProvider;
    this.getTransactionsUseCaseProvider = getTransactionsUseCaseProvider;
    this.addTransactionUseCaseProvider = addTransactionUseCaseProvider;
    this.updateTransactionUseCaseProvider = updateTransactionUseCaseProvider;
    this.deleteTransactionUseCaseProvider = deleteTransactionUseCaseProvider;
    this.syncTransactionsUseCaseProvider = syncTransactionsUseCaseProvider;
    this.categorySyncUseCaseProvider = categorySyncUseCaseProvider;
    this.syncEventBusProvider = syncEventBusProvider;
  }

  @Override
  public TransactionViewModel get() {
    return newInstance(getCashflowHistoryUseCaseProvider.get(), getTransactionsUseCaseProvider.get(), addTransactionUseCaseProvider.get(), updateTransactionUseCaseProvider.get(), deleteTransactionUseCaseProvider.get(), syncTransactionsUseCaseProvider.get(), categorySyncUseCaseProvider.get(), syncEventBusProvider.get());
  }

  public static TransactionViewModel_Factory create(
      javax.inject.Provider<GetCashflowHistoryUseCase> getCashflowHistoryUseCaseProvider,
      javax.inject.Provider<GetTransactionsUseCase> getTransactionsUseCaseProvider,
      javax.inject.Provider<AddTransactionUseCase> addTransactionUseCaseProvider,
      javax.inject.Provider<UpdateTransactionUseCase> updateTransactionUseCaseProvider,
      javax.inject.Provider<DeleteTransactionUseCase> deleteTransactionUseCaseProvider,
      javax.inject.Provider<SyncTransactionsUseCase> syncTransactionsUseCaseProvider,
      javax.inject.Provider<CategorySyncUseCase> categorySyncUseCaseProvider,
      javax.inject.Provider<SyncEventBus> syncEventBusProvider) {
    return new TransactionViewModel_Factory(Providers.asDaggerProvider(getCashflowHistoryUseCaseProvider), Providers.asDaggerProvider(getTransactionsUseCaseProvider), Providers.asDaggerProvider(addTransactionUseCaseProvider), Providers.asDaggerProvider(updateTransactionUseCaseProvider), Providers.asDaggerProvider(deleteTransactionUseCaseProvider), Providers.asDaggerProvider(syncTransactionsUseCaseProvider), Providers.asDaggerProvider(categorySyncUseCaseProvider), Providers.asDaggerProvider(syncEventBusProvider));
  }

  public static TransactionViewModel_Factory create(
      Provider<GetCashflowHistoryUseCase> getCashflowHistoryUseCaseProvider,
      Provider<GetTransactionsUseCase> getTransactionsUseCaseProvider,
      Provider<AddTransactionUseCase> addTransactionUseCaseProvider,
      Provider<UpdateTransactionUseCase> updateTransactionUseCaseProvider,
      Provider<DeleteTransactionUseCase> deleteTransactionUseCaseProvider,
      Provider<SyncTransactionsUseCase> syncTransactionsUseCaseProvider,
      Provider<CategorySyncUseCase> categorySyncUseCaseProvider,
      Provider<SyncEventBus> syncEventBusProvider) {
    return new TransactionViewModel_Factory(getCashflowHistoryUseCaseProvider, getTransactionsUseCaseProvider, addTransactionUseCaseProvider, updateTransactionUseCaseProvider, deleteTransactionUseCaseProvider, syncTransactionsUseCaseProvider, categorySyncUseCaseProvider, syncEventBusProvider);
  }

  public static TransactionViewModel newInstance(
      GetCashflowHistoryUseCase getCashflowHistoryUseCase,
      GetTransactionsUseCase getTransactionsUseCase, AddTransactionUseCase addTransactionUseCase,
      UpdateTransactionUseCase updateTransactionUseCase,
      DeleteTransactionUseCase deleteTransactionUseCase,
      SyncTransactionsUseCase syncTransactionsUseCase, CategorySyncUseCase categorySyncUseCase,
      SyncEventBus syncEventBus) {
    return new TransactionViewModel(getCashflowHistoryUseCase, getTransactionsUseCase, addTransactionUseCase, updateTransactionUseCase, deleteTransactionUseCase, syncTransactionsUseCase, categorySyncUseCase, syncEventBus);
  }
}
