package com.casha.app.core.notification;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class NotificationManager_Factory implements Factory<NotificationManager> {
  @Override
  public NotificationManager get() {
    return newInstance();
  }

  public static NotificationManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NotificationManager newInstance() {
    return new NotificationManager();
  }

  private static final class InstanceHolder {
    static final NotificationManager_Factory INSTANCE = new NotificationManager_Factory();
  }
}
