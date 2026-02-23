package com.casha.app.ui.feature.main;

import com.casha.app.core.notification.NotificationManager;
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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<NotificationManager> notificationManagerProvider;

  public MainViewModel_Factory(Provider<NotificationManager> notificationManagerProvider) {
    this.notificationManagerProvider = notificationManagerProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(notificationManagerProvider.get());
  }

  public static MainViewModel_Factory create(
      javax.inject.Provider<NotificationManager> notificationManagerProvider) {
    return new MainViewModel_Factory(Providers.asDaggerProvider(notificationManagerProvider));
  }

  public static MainViewModel_Factory create(
      Provider<NotificationManager> notificationManagerProvider) {
    return new MainViewModel_Factory(notificationManagerProvider);
  }

  public static MainViewModel newInstance(NotificationManager notificationManager) {
    return new MainViewModel(notificationManager);
  }
}
