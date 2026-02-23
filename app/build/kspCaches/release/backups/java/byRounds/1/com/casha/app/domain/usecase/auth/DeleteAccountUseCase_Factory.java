package com.casha.app.domain.usecase.auth;

import com.casha.app.domain.repository.AuthRepository;
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
public final class DeleteAccountUseCase_Factory implements Factory<DeleteAccountUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public DeleteAccountUseCase_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public DeleteAccountUseCase get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static DeleteAccountUseCase_Factory create(
      javax.inject.Provider<AuthRepository> authRepositoryProvider) {
    return new DeleteAccountUseCase_Factory(Providers.asDaggerProvider(authRepositoryProvider));
  }

  public static DeleteAccountUseCase_Factory create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new DeleteAccountUseCase_Factory(authRepositoryProvider);
  }

  public static DeleteAccountUseCase newInstance(AuthRepository authRepository) {
    return new DeleteAccountUseCase(authRepository);
  }
}
