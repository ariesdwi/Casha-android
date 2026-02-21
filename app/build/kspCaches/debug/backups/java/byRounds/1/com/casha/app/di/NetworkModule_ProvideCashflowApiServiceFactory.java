package com.casha.app.di;

import com.casha.app.data.remote.api.CashflowApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideCashflowApiServiceFactory implements Factory<CashflowApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideCashflowApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public CashflowApiService get() {
    return provideCashflowApiService(retrofitProvider.get());
  }

  public static NetworkModule_ProvideCashflowApiServiceFactory create(
      javax.inject.Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideCashflowApiServiceFactory(Providers.asDaggerProvider(retrofitProvider));
  }

  public static NetworkModule_ProvideCashflowApiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideCashflowApiServiceFactory(retrofitProvider);
  }

  public static CashflowApiService provideCashflowApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideCashflowApiService(retrofit));
  }
}
