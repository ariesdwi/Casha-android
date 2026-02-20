package com.casha.app.core.network;

import com.casha.app.core.auth.AuthManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
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
public final class AuthInterceptor_Factory implements Factory<AuthInterceptor> {
  private final Provider<AuthManager> authManagerProvider;

  public AuthInterceptor_Factory(Provider<AuthManager> authManagerProvider) {
    this.authManagerProvider = authManagerProvider;
  }

  @Override
  public AuthInterceptor get() {
    return newInstance(authManagerProvider.get());
  }

  public static AuthInterceptor_Factory create(
      javax.inject.Provider<AuthManager> authManagerProvider) {
    return new AuthInterceptor_Factory(Providers.asDaggerProvider(authManagerProvider));
  }

  public static AuthInterceptor_Factory create(Provider<AuthManager> authManagerProvider) {
    return new AuthInterceptor_Factory(authManagerProvider);
  }

  public static AuthInterceptor newInstance(AuthManager authManager) {
    return new AuthInterceptor(authManager);
  }
}
