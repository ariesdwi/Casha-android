package com.casha.app.data.remote.impl;

import com.casha.app.data.local.dao.IncomeDao;
import com.casha.app.data.local.dao.TransactionDao;
import com.casha.app.data.remote.api.ChatApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.serialization.json.Json;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ChatRepositoryImpl_Factory implements Factory<ChatRepositoryImpl> {
  private final Provider<ChatApiService> apiServiceProvider;

  private final Provider<TransactionDao> transactionDaoProvider;

  private final Provider<IncomeDao> incomeDaoProvider;

  private final Provider<Json> jsonProvider;

  public ChatRepositoryImpl_Factory(Provider<ChatApiService> apiServiceProvider,
      Provider<TransactionDao> transactionDaoProvider, Provider<IncomeDao> incomeDaoProvider,
      Provider<Json> jsonProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.transactionDaoProvider = transactionDaoProvider;
    this.incomeDaoProvider = incomeDaoProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public ChatRepositoryImpl get() {
    return newInstance(apiServiceProvider.get(), transactionDaoProvider.get(), incomeDaoProvider.get(), jsonProvider.get());
  }

  public static ChatRepositoryImpl_Factory create(
      javax.inject.Provider<ChatApiService> apiServiceProvider,
      javax.inject.Provider<TransactionDao> transactionDaoProvider,
      javax.inject.Provider<IncomeDao> incomeDaoProvider,
      javax.inject.Provider<Json> jsonProvider) {
    return new ChatRepositoryImpl_Factory(Providers.asDaggerProvider(apiServiceProvider), Providers.asDaggerProvider(transactionDaoProvider), Providers.asDaggerProvider(incomeDaoProvider), Providers.asDaggerProvider(jsonProvider));
  }

  public static ChatRepositoryImpl_Factory create(Provider<ChatApiService> apiServiceProvider,
      Provider<TransactionDao> transactionDaoProvider, Provider<IncomeDao> incomeDaoProvider,
      Provider<Json> jsonProvider) {
    return new ChatRepositoryImpl_Factory(apiServiceProvider, transactionDaoProvider, incomeDaoProvider, jsonProvider);
  }

  public static ChatRepositoryImpl newInstance(ChatApiService apiService,
      TransactionDao transactionDao, IncomeDao incomeDao, Json json) {
    return new ChatRepositoryImpl(apiService, transactionDao, incomeDao, json);
  }
}
