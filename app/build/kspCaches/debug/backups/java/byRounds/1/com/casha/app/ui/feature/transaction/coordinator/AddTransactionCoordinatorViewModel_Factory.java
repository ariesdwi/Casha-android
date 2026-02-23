package com.casha.app.ui.feature.transaction.coordinator;

import com.casha.app.core.auth.SubscriptionManager;
import com.casha.app.core.network.SyncEventBus;
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
public final class AddTransactionCoordinatorViewModel_Factory implements Factory<AddTransactionCoordinatorViewModel> {
  private final Provider<SubscriptionManager> subscriptionManagerProvider;

  private final Provider<SyncEventBus> syncEventBusProvider;

  public AddTransactionCoordinatorViewModel_Factory(
      Provider<SubscriptionManager> subscriptionManagerProvider,
      Provider<SyncEventBus> syncEventBusProvider) {
    this.subscriptionManagerProvider = subscriptionManagerProvider;
    this.syncEventBusProvider = syncEventBusProvider;
  }

  @Override
  public AddTransactionCoordinatorViewModel get() {
    return newInstance(subscriptionManagerProvider.get(), syncEventBusProvider.get());
  }

  public static AddTransactionCoordinatorViewModel_Factory create(
      javax.inject.Provider<SubscriptionManager> subscriptionManagerProvider,
      javax.inject.Provider<SyncEventBus> syncEventBusProvider) {
    return new AddTransactionCoordinatorViewModel_Factory(Providers.asDaggerProvider(subscriptionManagerProvider), Providers.asDaggerProvider(syncEventBusProvider));
  }

  public static AddTransactionCoordinatorViewModel_Factory create(
      Provider<SubscriptionManager> subscriptionManagerProvider,
      Provider<SyncEventBus> syncEventBusProvider) {
    return new AddTransactionCoordinatorViewModel_Factory(subscriptionManagerProvider, syncEventBusProvider);
  }

  public static AddTransactionCoordinatorViewModel newInstance(
      SubscriptionManager subscriptionManager, SyncEventBus syncEventBus) {
    return new AddTransactionCoordinatorViewModel(subscriptionManager, syncEventBus);
  }
}
