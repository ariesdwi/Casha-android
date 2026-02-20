package com.casha.app.di;

import com.casha.app.core.network.AuthInterceptor;
import com.casha.app.core.network.ErrorInterceptor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

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
public final class NetworkModule_ProvideOkHttpClientFactory implements Factory<OkHttpClient> {
  private final Provider<AuthInterceptor> authInterceptorProvider;

  private final Provider<ErrorInterceptor> errorInterceptorProvider;

  public NetworkModule_ProvideOkHttpClientFactory(Provider<AuthInterceptor> authInterceptorProvider,
      Provider<ErrorInterceptor> errorInterceptorProvider) {
    this.authInterceptorProvider = authInterceptorProvider;
    this.errorInterceptorProvider = errorInterceptorProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideOkHttpClient(authInterceptorProvider.get(), errorInterceptorProvider.get());
  }

  public static NetworkModule_ProvideOkHttpClientFactory create(
      javax.inject.Provider<AuthInterceptor> authInterceptorProvider,
      javax.inject.Provider<ErrorInterceptor> errorInterceptorProvider) {
    return new NetworkModule_ProvideOkHttpClientFactory(Providers.asDaggerProvider(authInterceptorProvider), Providers.asDaggerProvider(errorInterceptorProvider));
  }

  public static NetworkModule_ProvideOkHttpClientFactory create(
      Provider<AuthInterceptor> authInterceptorProvider,
      Provider<ErrorInterceptor> errorInterceptorProvider) {
    return new NetworkModule_ProvideOkHttpClientFactory(authInterceptorProvider, errorInterceptorProvider);
  }

  public static OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor,
      ErrorInterceptor errorInterceptor) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideOkHttpClient(authInterceptor, errorInterceptor));
  }
}
