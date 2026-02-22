package com.casha.app.ui.feature.report;

import com.casha.app.domain.usecase.report.GetCategorySpendingUseCase;
import com.casha.app.domain.usecase.report.GetTransactionByCategoryUseCase;
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
public final class ReportViewModel_Factory implements Factory<ReportViewModel> {
  private final Provider<GetCategorySpendingUseCase> getCategorySpendingUseCaseProvider;

  private final Provider<GetTransactionByCategoryUseCase> getTransactionByCategoryUseCaseProvider;

  public ReportViewModel_Factory(
      Provider<GetCategorySpendingUseCase> getCategorySpendingUseCaseProvider,
      Provider<GetTransactionByCategoryUseCase> getTransactionByCategoryUseCaseProvider) {
    this.getCategorySpendingUseCaseProvider = getCategorySpendingUseCaseProvider;
    this.getTransactionByCategoryUseCaseProvider = getTransactionByCategoryUseCaseProvider;
  }

  @Override
  public ReportViewModel get() {
    return newInstance(getCategorySpendingUseCaseProvider.get(), getTransactionByCategoryUseCaseProvider.get());
  }

  public static ReportViewModel_Factory create(
      javax.inject.Provider<GetCategorySpendingUseCase> getCategorySpendingUseCaseProvider,
      javax.inject.Provider<GetTransactionByCategoryUseCase> getTransactionByCategoryUseCaseProvider) {
    return new ReportViewModel_Factory(Providers.asDaggerProvider(getCategorySpendingUseCaseProvider), Providers.asDaggerProvider(getTransactionByCategoryUseCaseProvider));
  }

  public static ReportViewModel_Factory create(
      Provider<GetCategorySpendingUseCase> getCategorySpendingUseCaseProvider,
      Provider<GetTransactionByCategoryUseCase> getTransactionByCategoryUseCaseProvider) {
    return new ReportViewModel_Factory(getCategorySpendingUseCaseProvider, getTransactionByCategoryUseCaseProvider);
  }

  public static ReportViewModel newInstance(GetCategorySpendingUseCase getCategorySpendingUseCase,
      GetTransactionByCategoryUseCase getTransactionByCategoryUseCase) {
    return new ReportViewModel(getCategorySpendingUseCase, getTransactionByCategoryUseCase);
  }
}
