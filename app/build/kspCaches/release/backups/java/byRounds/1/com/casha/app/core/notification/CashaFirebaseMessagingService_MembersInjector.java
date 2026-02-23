package com.casha.app.core.notification;

import com.casha.app.domain.repository.AuthRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class CashaFirebaseMessagingService_MembersInjector implements MembersInjector<CashaFirebaseMessagingService> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<NotificationManager> notificationManagerProvider;

  public CashaFirebaseMessagingService_MembersInjector(
      Provider<AuthRepository> authRepositoryProvider,
      Provider<NotificationManager> notificationManagerProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.notificationManagerProvider = notificationManagerProvider;
  }

  public static MembersInjector<CashaFirebaseMessagingService> create(
      Provider<AuthRepository> authRepositoryProvider,
      Provider<NotificationManager> notificationManagerProvider) {
    return new CashaFirebaseMessagingService_MembersInjector(authRepositoryProvider, notificationManagerProvider);
  }

  public static MembersInjector<CashaFirebaseMessagingService> create(
      javax.inject.Provider<AuthRepository> authRepositoryProvider,
      javax.inject.Provider<NotificationManager> notificationManagerProvider) {
    return new CashaFirebaseMessagingService_MembersInjector(Providers.asDaggerProvider(authRepositoryProvider), Providers.asDaggerProvider(notificationManagerProvider));
  }

  @Override
  public void injectMembers(CashaFirebaseMessagingService instance) {
    injectAuthRepository(instance, authRepositoryProvider.get());
    injectNotificationManager(instance, notificationManagerProvider.get());
  }

  @InjectedFieldSignature("com.casha.app.core.notification.CashaFirebaseMessagingService.authRepository")
  public static void injectAuthRepository(CashaFirebaseMessagingService instance,
      AuthRepository authRepository) {
    instance.authRepository = authRepository;
  }

  @InjectedFieldSignature("com.casha.app.core.notification.CashaFirebaseMessagingService.notificationManager")
  public static void injectNotificationManager(CashaFirebaseMessagingService instance,
      NotificationManager notificationManager) {
    instance.notificationManager = notificationManager;
  }
}
