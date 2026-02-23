package com.casha.app.ui.feature.transaction.coordinator;

import com.casha.app.core.network.SyncEventBus;
import com.casha.app.domain.repository.ChatRepository;
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
public final class AddMessageViewModel_Factory implements Factory<AddMessageViewModel> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<SyncEventBus> syncEventBusProvider;

  public AddMessageViewModel_Factory(Provider<ChatRepository> chatRepositoryProvider,
      Provider<SyncEventBus> syncEventBusProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.syncEventBusProvider = syncEventBusProvider;
  }

  @Override
  public AddMessageViewModel get() {
    return newInstance(chatRepositoryProvider.get(), syncEventBusProvider.get());
  }

  public static AddMessageViewModel_Factory create(
      javax.inject.Provider<ChatRepository> chatRepositoryProvider,
      javax.inject.Provider<SyncEventBus> syncEventBusProvider) {
    return new AddMessageViewModel_Factory(Providers.asDaggerProvider(chatRepositoryProvider), Providers.asDaggerProvider(syncEventBusProvider));
  }

  public static AddMessageViewModel_Factory create(Provider<ChatRepository> chatRepositoryProvider,
      Provider<SyncEventBus> syncEventBusProvider) {
    return new AddMessageViewModel_Factory(chatRepositoryProvider, syncEventBusProvider);
  }

  public static AddMessageViewModel newInstance(ChatRepository chatRepository,
      SyncEventBus syncEventBus) {
    return new AddMessageViewModel(chatRepository, syncEventBus);
  }
}
