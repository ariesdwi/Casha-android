package com.casha.app.ui.feature.transaction;

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
  private final Provider<GetTransactionsUseCase> getTransactionsUseCaseProvider;

  private final Provider<AddTransactionUseCase> addTransactionUseCaseProvider;

  private final Provider<UpdateTransactionUseCase> updateTransactionUseCaseProvider;

  private final Provider<DeleteTransactionUseCase> deleteTransactionUseCaseProvider;

  private final Provider<SyncTransactionsUseCase> syncTransactionsUseCaseProvider;

  public TransactionViewModel_Factory(
      Provider<GetTransactionsUseCase> getTransactionsUseCaseProvider,
      Provider<AddTransactionUseCase> addTransactionUseCaseProvider,
      Provider<UpdateTransactionUseCase> updateTransactionUseCaseProvider,
      Provider<DeleteTransactionUseCase> deleteTransactionUseCaseProvider,
      Provider<SyncTransactionsUseCase> syncTransactionsUseCaseProvider) {
    this.getTransactionsUseCaseProvider = getTransactionsUseCaseProvider;
    this.addTransactionUseCaseProvider = addTransactionUseCaseProvider;
    this.updateTransactionUseCaseProvider = updateTransactionUseCaseProvider;
    this.deleteTransactionUseCaseProvider = deleteTransactionUseCaseProvider;
    this.syncTransactionsUseCaseProvider = syncTransactionsUseCaseProvider;
  }

  @Override
  public TransactionViewModel get() {
    return newInstance(getTransactionsUseCaseProvider.get(), addTransactionUseCaseProvider.get(), updateTransactionUseCaseProvider.get(), deleteTransactionUseCaseProvider.get(), syncTransactionsUseCaseProvider.get());
  }

  public static TransactionViewModel_Factory create(
      javax.inject.Provider<GetTransactionsUseCase> getTransactionsUseCaseProvider,
      javax.inject.Provider<AddTransactionUseCase> addTransactionUseCaseProvider,
      javax.inject.Provider<UpdateTransactionUseCase> updateTransactionUseCaseProvider,
      javax.inject.Provider<DeleteTransactionUseCase> deleteTransactionUseCaseProvider,
      javax.inject.Provider<SyncTransactionsUseCase> syncTransactionsUseCaseProvider) {
    return new TransactionViewModel_Factory(Providers.asDaggerProvider(getTransactionsUseCaseProvider), Providers.asDaggerProvider(addTransactionUseCaseProvider), Providers.asDaggerProvider(updateTransactionUseCaseProvider), Providers.asDaggerProvider(deleteTransactionUseCaseProvider), Providers.asDaggerProvider(syncTransactionsUseCaseProvider));
  }

  public static TransactionViewModel_Factory create(
      Provider<GetTransactionsUseCase> getTransactionsUseCaseProvider,
      Provider<AddTransactionUseCase> addTransactionUseCaseProvider,
      Provider<UpdateTransactionUseCase> updateTransactionUseCaseProvider,
      Provider<DeleteTransactionUseCase> deleteTransactionUseCaseProvider,
      Provider<SyncTransactionsUseCase> syncTransactionsUseCaseProvider) {
    return new TransactionViewModel_Factory(getTransactionsUseCaseProvider, addTransactionUseCaseProvider, updateTransactionUseCaseProvider, deleteTransactionUseCaseProvider, syncTransactionsUseCaseProvider);
  }

  public static TransactionViewModel newInstance(GetTransactionsUseCase getTransactionsUseCase,
      AddTransactionUseCase addTransactionUseCase,
      UpdateTransactionUseCase updateTransactionUseCase,
      DeleteTransactionUseCase deleteTransactionUseCase,
      SyncTransactionsUseCase syncTransactionsUseCase) {
    return new TransactionViewModel(getTransactionsUseCase, addTransactionUseCase, updateTransactionUseCase, deleteTransactionUseCase, syncTransactionsUseCase);
  }
}
