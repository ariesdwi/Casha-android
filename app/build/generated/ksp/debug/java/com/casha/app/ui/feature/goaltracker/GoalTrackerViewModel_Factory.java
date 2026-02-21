package com.casha.app.ui.feature.goaltracker;

import com.casha.app.domain.usecase.goal.GetGoalSummaryUseCase;
import com.casha.app.domain.usecase.goal.GetGoalsUseCase;
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
public final class GoalTrackerViewModel_Factory implements Factory<GoalTrackerViewModel> {
  private final Provider<GetGoalsUseCase> getGoalsUseCaseProvider;

  private final Provider<GetGoalSummaryUseCase> getGoalSummaryUseCaseProvider;

  public GoalTrackerViewModel_Factory(Provider<GetGoalsUseCase> getGoalsUseCaseProvider,
      Provider<GetGoalSummaryUseCase> getGoalSummaryUseCaseProvider) {
    this.getGoalsUseCaseProvider = getGoalsUseCaseProvider;
    this.getGoalSummaryUseCaseProvider = getGoalSummaryUseCaseProvider;
  }

  @Override
  public GoalTrackerViewModel get() {
    return newInstance(getGoalsUseCaseProvider.get(), getGoalSummaryUseCaseProvider.get());
  }

  public static GoalTrackerViewModel_Factory create(
      javax.inject.Provider<GetGoalsUseCase> getGoalsUseCaseProvider,
      javax.inject.Provider<GetGoalSummaryUseCase> getGoalSummaryUseCaseProvider) {
    return new GoalTrackerViewModel_Factory(Providers.asDaggerProvider(getGoalsUseCaseProvider), Providers.asDaggerProvider(getGoalSummaryUseCaseProvider));
  }

  public static GoalTrackerViewModel_Factory create(
      Provider<GetGoalsUseCase> getGoalsUseCaseProvider,
      Provider<GetGoalSummaryUseCase> getGoalSummaryUseCaseProvider) {
    return new GoalTrackerViewModel_Factory(getGoalsUseCaseProvider, getGoalSummaryUseCaseProvider);
  }

  public static GoalTrackerViewModel newInstance(GetGoalsUseCase getGoalsUseCase,
      GetGoalSummaryUseCase getGoalSummaryUseCase) {
    return new GoalTrackerViewModel(getGoalsUseCase, getGoalSummaryUseCase);
  }
}
