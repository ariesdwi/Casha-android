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
public final class GetGoalSummaryUseCase_Factory implements Factory<GetGoalSummaryUseCase> {
  private final Provider<GoalRepository> repositoryProvider;

  public GetGoalSummaryUseCase_Factory(Provider<GoalRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetGoalSummaryUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetGoalSummaryUseCase_Factory create(
      javax.inject.Provider<GoalRepository> repositoryProvider) {
    return new GetGoalSummaryUseCase_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static GetGoalSummaryUseCase_Factory create(Provider<GoalRepository> repositoryProvider) {
    return new GetGoalSummaryUseCase_Factory(repositoryProvider);
  }

  public static GetGoalSummaryUseCase newInstance(GoalRepository repository) {
    return new GetGoalSummaryUseCase(repository);
  }
}
