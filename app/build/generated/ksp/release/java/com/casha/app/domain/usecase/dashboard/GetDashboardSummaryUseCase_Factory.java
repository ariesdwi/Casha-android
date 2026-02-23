package com.casha.app.domain.usecase.dashboard;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class GetDashboardSummaryUseCase_Factory implements Factory<GetDashboardSummaryUseCase> {
  @Override
  public GetDashboardSummaryUseCase get() {
    return newInstance();
  }

  public static GetDashboardSummaryUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GetDashboardSummaryUseCase newInstance() {
    return new GetDashboardSummaryUseCase();
  }

  private static final class InstanceHolder {
    static final GetDashboardSummaryUseCase_Factory INSTANCE = new GetDashboardSummaryUseCase_Factory();
  }
}
