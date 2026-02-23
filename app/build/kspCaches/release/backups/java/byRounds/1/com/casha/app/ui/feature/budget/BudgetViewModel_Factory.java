package com.casha.app.ui.feature.budget;

import com.casha.app.core.network.NetworkMonitor;
import com.casha.app.domain.usecase.budget.AddBudgetUseCase;
import com.casha.app.domain.usecase.budget.ApplyBudgetRecommendationsUseCase;
import com.casha.app.domain.usecase.budget.BudgetSyncUseCase;
import com.casha.app.domain.usecase.budget.DeleteBudgetUseCase;
import com.casha.app.domain.usecase.budget.GetBudgetRecommendationsUseCase;
import com.casha.app.domain.usecase.budget.GetBudgetSummaryUseCase;
import com.casha.app.domain.usecase.budget.GetBudgetsUseCase;
import com.casha.app.domain.usecase.budget.UpdateBudgetUseCase;
import com.casha.app.domain.usecase.category.CategorySyncUseCase;
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
public final class BudgetViewModel_Factory implements Factory<BudgetViewModel> {
  private final Provider<GetBudgetsUseCase> getBudgetsUseCaseProvider;

  private final Provider<AddBudgetUseCase> addBudgetUseCaseProvider;

  private final Provider<UpdateBudgetUseCase> updateBudgetUseCaseProvider;

  private final Provider<DeleteBudgetUseCase> deleteBudgetUseCaseProvider;

  private final Provider<GetBudgetSummaryUseCase> getBudgetSummaryUseCaseProvider;

  private final Provider<GetBudgetRecommendationsUseCase> getRecommendationsUseCaseProvider;

  private final Provider<ApplyBudgetRecommendationsUseCase> applyRecommendationsUseCaseProvider;

  private final Provider<BudgetSyncUseCase> budgetSyncUseCaseProvider;

  private final Provider<CategorySyncUseCase> categorySyncUseCaseProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  public BudgetViewModel_Factory(Provider<GetBudgetsUseCase> getBudgetsUseCaseProvider,
      Provider<AddBudgetUseCase> addBudgetUseCaseProvider,
      Provider<UpdateBudgetUseCase> updateBudgetUseCaseProvider,
      Provider<DeleteBudgetUseCase> deleteBudgetUseCaseProvider,
      Provider<GetBudgetSummaryUseCase> getBudgetSummaryUseCaseProvider,
      Provider<GetBudgetRecommendationsUseCase> getRecommendationsUseCaseProvider,
      Provider<ApplyBudgetRecommendationsUseCase> applyRecommendationsUseCaseProvider,
      Provider<BudgetSyncUseCase> budgetSyncUseCaseProvider,
      Provider<CategorySyncUseCase> categorySyncUseCaseProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    this.getBudgetsUseCaseProvider = getBudgetsUseCaseProvider;
    this.addBudgetUseCaseProvider = addBudgetUseCaseProvider;
    this.updateBudgetUseCaseProvider = updateBudgetUseCaseProvider;
    this.deleteBudgetUseCaseProvider = deleteBudgetUseCaseProvider;
    this.getBudgetSummaryUseCaseProvider = getBudgetSummaryUseCaseProvider;
    this.getRecommendationsUseCaseProvider = getRecommendationsUseCaseProvider;
    this.applyRecommendationsUseCaseProvider = applyRecommendationsUseCaseProvider;
    this.budgetSyncUseCaseProvider = budgetSyncUseCaseProvider;
    this.categorySyncUseCaseProvider = categorySyncUseCaseProvider;
    this.networkMonitorProvider = networkMonitorProvider;
  }

  @Override
  public BudgetViewModel get() {
    return newInstance(getBudgetsUseCaseProvider.get(), addBudgetUseCaseProvider.get(), updateBudgetUseCaseProvider.get(), deleteBudgetUseCaseProvider.get(), getBudgetSummaryUseCaseProvider.get(), getRecommendationsUseCaseProvider.get(), applyRecommendationsUseCaseProvider.get(), budgetSyncUseCaseProvider.get(), categorySyncUseCaseProvider.get(), networkMonitorProvider.get());
  }

  public static BudgetViewModel_Factory create(
      javax.inject.Provider<GetBudgetsUseCase> getBudgetsUseCaseProvider,
      javax.inject.Provider<AddBudgetUseCase> addBudgetUseCaseProvider,
      javax.inject.Provider<UpdateBudgetUseCase> updateBudgetUseCaseProvider,
      javax.inject.Provider<DeleteBudgetUseCase> deleteBudgetUseCaseProvider,
      javax.inject.Provider<GetBudgetSummaryUseCase> getBudgetSummaryUseCaseProvider,
      javax.inject.Provider<GetBudgetRecommendationsUseCase> getRecommendationsUseCaseProvider,
      javax.inject.Provider<ApplyBudgetRecommendationsUseCase> applyRecommendationsUseCaseProvider,
      javax.inject.Provider<BudgetSyncUseCase> budgetSyncUseCaseProvider,
      javax.inject.Provider<CategorySyncUseCase> categorySyncUseCaseProvider,
      javax.inject.Provider<NetworkMonitor> networkMonitorProvider) {
    return new BudgetViewModel_Factory(Providers.asDaggerProvider(getBudgetsUseCaseProvider), Providers.asDaggerProvider(addBudgetUseCaseProvider), Providers.asDaggerProvider(updateBudgetUseCaseProvider), Providers.asDaggerProvider(deleteBudgetUseCaseProvider), Providers.asDaggerProvider(getBudgetSummaryUseCaseProvider), Providers.asDaggerProvider(getRecommendationsUseCaseProvider), Providers.asDaggerProvider(applyRecommendationsUseCaseProvider), Providers.asDaggerProvider(budgetSyncUseCaseProvider), Providers.asDaggerProvider(categorySyncUseCaseProvider), Providers.asDaggerProvider(networkMonitorProvider));
  }

  public static BudgetViewModel_Factory create(
      Provider<GetBudgetsUseCase> getBudgetsUseCaseProvider,
      Provider<AddBudgetUseCase> addBudgetUseCaseProvider,
      Provider<UpdateBudgetUseCase> updateBudgetUseCaseProvider,
      Provider<DeleteBudgetUseCase> deleteBudgetUseCaseProvider,
      Provider<GetBudgetSummaryUseCase> getBudgetSummaryUseCaseProvider,
      Provider<GetBudgetRecommendationsUseCase> getRecommendationsUseCaseProvider,
      Provider<ApplyBudgetRecommendationsUseCase> applyRecommendationsUseCaseProvider,
      Provider<BudgetSyncUseCase> budgetSyncUseCaseProvider,
      Provider<CategorySyncUseCase> categorySyncUseCaseProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    return new BudgetViewModel_Factory(getBudgetsUseCaseProvider, addBudgetUseCaseProvider, updateBudgetUseCaseProvider, deleteBudgetUseCaseProvider, getBudgetSummaryUseCaseProvider, getRecommendationsUseCaseProvider, applyRecommendationsUseCaseProvider, budgetSyncUseCaseProvider, categorySyncUseCaseProvider, networkMonitorProvider);
  }

  public static BudgetViewModel newInstance(GetBudgetsUseCase getBudgetsUseCase,
      AddBudgetUseCase addBudgetUseCase, UpdateBudgetUseCase updateBudgetUseCase,
      DeleteBudgetUseCase deleteBudgetUseCase, GetBudgetSummaryUseCase getBudgetSummaryUseCase,
      GetBudgetRecommendationsUseCase getRecommendationsUseCase,
      ApplyBudgetRecommendationsUseCase applyRecommendationsUseCase,
      BudgetSyncUseCase budgetSyncUseCase, CategorySyncUseCase categorySyncUseCase,
      NetworkMonitor networkMonitor) {
    return new BudgetViewModel(getBudgetsUseCase, addBudgetUseCase, updateBudgetUseCase, deleteBudgetUseCase, getBudgetSummaryUseCase, getRecommendationsUseCase, applyRecommendationsUseCase, budgetSyncUseCase, categorySyncUseCase, networkMonitor);
  }
}
