package com.casha.app.core.network;

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
public final class ErrorInterceptor_Factory implements Factory<ErrorInterceptor> {
  @Override
  public ErrorInterceptor get() {
    return newInstance();
  }

  public static ErrorInterceptor_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ErrorInterceptor newInstance() {
    return new ErrorInterceptor();
  }

  private static final class InstanceHolder {
    static final ErrorInterceptor_Factory INSTANCE = new ErrorInterceptor_Factory();
  }
}
