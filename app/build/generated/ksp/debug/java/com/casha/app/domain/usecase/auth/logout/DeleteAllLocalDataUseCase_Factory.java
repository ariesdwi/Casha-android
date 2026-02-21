package com.casha.app.domain.usecase.auth.logout;

import com.casha.app.core.auth.AuthManager;
import com.casha.app.data.local.database.CashaDatabase;
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
public final class DeleteAllLocalDataUseCase_Factory implements Factory<DeleteAllLocalDataUseCase> {
  private final Provider<AuthManager> authManagerProvider;

  private final Provider<CashaDatabase> databaseProvider;

  public DeleteAllLocalDataUseCase_Factory(Provider<AuthManager> authManagerProvider,
      Provider<CashaDatabase> databaseProvider) {
    this.authManagerProvider = authManagerProvider;
    this.databaseProvider = databaseProvider;
  }

  @Override
  public DeleteAllLocalDataUseCase get() {
    return newInstance(authManagerProvider.get(), databaseProvider.get());
  }

  public static DeleteAllLocalDataUseCase_Factory create(
      javax.inject.Provider<AuthManager> authManagerProvider,
      javax.inject.Provider<CashaDatabase> databaseProvider) {
    return new DeleteAllLocalDataUseCase_Factory(Providers.asDaggerProvider(authManagerProvider), Providers.asDaggerProvider(databaseProvider));
  }

  public static DeleteAllLocalDataUseCase_Factory create(Provider<AuthManager> authManagerProvider,
      Provider<CashaDatabase> databaseProvider) {
    return new DeleteAllLocalDataUseCase_Factory(authManagerProvider, databaseProvider);
  }

  public static DeleteAllLocalDataUseCase newInstance(AuthManager authManager,
      CashaDatabase database) {
    return new DeleteAllLocalDataUseCase(authManager, database);
  }
}
