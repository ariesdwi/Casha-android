package com.casha.app.domain.usecase.goal;

import com.casha.app.domain.repository.GoalRepository;
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
public final class GetGoalsUseCase_Factory implements Factory<GetGoalsUseCase> {
  private final Provider<GoalRepository> repositoryProvider;

  public GetGoalsUseCase_Factory(Provider<GoalRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetGoalsUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetGoalsUseCase_Factory create(
      javax.inject.Provider<GoalRepository> repositoryProvider) {
    return new GetGoalsUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static GetGoalsUseCase_Factory create(Provider<GoalRepository> repositoryProvider) {
    return new GetGoalsUseCase_Factory(repositoryProvider);
  }

  public static GetGoalsUseCase newInstance(GoalRepository repository) {
    return new GetGoalsUseCase(repository);
  }
}
