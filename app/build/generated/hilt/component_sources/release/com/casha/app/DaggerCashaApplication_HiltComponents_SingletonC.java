package com.casha.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.casha.app.core.auth.AuthManager;
import com.casha.app.core.auth.SubscriptionManager;
import com.casha.app.core.network.AuthInterceptor;
import com.casha.app.core.network.ErrorInterceptor;
import com.casha.app.core.network.NetworkMonitor;
import com.casha.app.core.network.SyncEventBus;
import com.casha.app.core.notification.CashaFirebaseMessagingService;
import com.casha.app.core.notification.CashaFirebaseMessagingService_MembersInjector;
import com.casha.app.core.notification.NotificationManager;
import com.casha.app.data.local.dao.BudgetDao;
import com.casha.app.data.local.dao.CategoryDao;
import com.casha.app.data.local.dao.IncomeDao;
import com.casha.app.data.local.dao.TransactionDao;
import com.casha.app.data.local.database.CashaDatabase;
import com.casha.app.data.remote.api.AuthApiService;
import com.casha.app.data.remote.api.BudgetApiService;
import com.casha.app.data.remote.api.CashflowApiService;
import com.casha.app.data.remote.api.CategoryApiService;
import com.casha.app.data.remote.api.ChatApiService;
import com.casha.app.data.remote.api.GoalApiService;
import com.casha.app.data.remote.api.IncomeApiService;
import com.casha.app.data.remote.api.TransactionApiService;
import com.casha.app.data.remote.impl.AuthRepositoryImpl;
import com.casha.app.data.remote.impl.BudgetRepositoryImpl;
import com.casha.app.data.remote.impl.CashflowRepositoryImpl;
import com.casha.app.data.remote.impl.CategoryRepositoryImpl;
import com.casha.app.data.remote.impl.ChatRepositoryImpl;
import com.casha.app.data.remote.impl.GoalRepositoryImpl;
import com.casha.app.data.remote.impl.IncomeRepositoryImpl;
import com.casha.app.data.remote.impl.TransactionRepositoryImpl;
import com.casha.app.di.DatabaseModule_ProvideBudgetDaoFactory;
import com.casha.app.di.DatabaseModule_ProvideCategoryDaoFactory;
import com.casha.app.di.DatabaseModule_ProvideDatabaseFactory;
import com.casha.app.di.DatabaseModule_ProvideIncomeDaoFactory;
import com.casha.app.di.DatabaseModule_ProvideTransactionDaoFactory;
import com.casha.app.di.NetworkModule_ProvideAuthApiServiceFactory;
import com.casha.app.di.NetworkModule_ProvideBudgetApiServiceFactory;
import com.casha.app.di.NetworkModule_ProvideCashflowApiServiceFactory;
import com.casha.app.di.NetworkModule_ProvideCategoryApiServiceFactory;
import com.casha.app.di.NetworkModule_ProvideChatApiServiceFactory;
import com.casha.app.di.NetworkModule_ProvideGoalApiServiceFactory;
import com.casha.app.di.NetworkModule_ProvideIncomeApiServiceFactory;
import com.casha.app.di.NetworkModule_ProvideJsonFactory;
import com.casha.app.di.NetworkModule_ProvideOkHttpClientFactory;
import com.casha.app.di.NetworkModule_ProvideRetrofitFactory;
import com.casha.app.di.NetworkModule_ProvideTransactionApiServiceFactory;
import com.casha.app.domain.usecase.auth.GetProfileUseCase;
import com.casha.app.domain.usecase.auth.GoogleLoginUseCase;
import com.casha.app.domain.usecase.auth.LoginUseCase;
import com.casha.app.domain.usecase.auth.RegisterUseCase;
import com.casha.app.domain.usecase.auth.ResetPasswordUseCase;
import com.casha.app.domain.usecase.auth.logout.DeleteAllLocalDataUseCase;
import com.casha.app.domain.usecase.budget.AddBudgetUseCase;
import com.casha.app.domain.usecase.budget.ApplyBudgetRecommendationsUseCase;
import com.casha.app.domain.usecase.budget.BudgetSyncUseCase;
import com.casha.app.domain.usecase.budget.DeleteBudgetUseCase;
import com.casha.app.domain.usecase.budget.GetBudgetRecommendationsUseCase;
import com.casha.app.domain.usecase.budget.GetBudgetSummaryUseCase;
import com.casha.app.domain.usecase.budget.GetBudgetsUseCase;
import com.casha.app.domain.usecase.budget.UpdateBudgetUseCase;
import com.casha.app.domain.usecase.category.CategorySyncUseCase;
import com.casha.app.domain.usecase.category.CreateCategoryUseCase;
import com.casha.app.domain.usecase.category.DeleteCategoryUseCase;
import com.casha.app.domain.usecase.category.UpdateCategoryUseCase;
import com.casha.app.domain.usecase.dashboard.CashflowSyncUseCase;
import com.casha.app.domain.usecase.dashboard.GetCashflowHistoryUseCase;
import com.casha.app.domain.usecase.dashboard.GetCashflowSummaryUseCase;
import com.casha.app.domain.usecase.dashboard.GetRecentTransactionsUseCase;
import com.casha.app.domain.usecase.dashboard.GetSpendingReportUseCase;
import com.casha.app.domain.usecase.dashboard.GetTotalSpendingUseCase;
import com.casha.app.domain.usecase.dashboard.GetUnsyncTransactionCountUseCase;
import com.casha.app.domain.usecase.dashboard.TransactionSyncUseCase;
import com.casha.app.domain.usecase.goal.GetGoalSummaryUseCase;
import com.casha.app.domain.usecase.goal.GetGoalsUseCase;
import com.casha.app.domain.usecase.profile.DeleteAccountUseCase;
import com.casha.app.domain.usecase.profile.UpdateProfileUseCase;
import com.casha.app.domain.usecase.report.GetCategorySpendingUseCase;
import com.casha.app.domain.usecase.report.GetTransactionByCategoryUseCase;
import com.casha.app.domain.usecase.transaction.AddIncomeUseCase;
import com.casha.app.domain.usecase.transaction.AddTransactionUseCase;
import com.casha.app.domain.usecase.transaction.DeleteIncomeUseCase;
import com.casha.app.domain.usecase.transaction.DeleteTransactionUseCase;
import com.casha.app.domain.usecase.transaction.GetIncomesUseCase;
import com.casha.app.domain.usecase.transaction.GetTransactionsUseCase;
import com.casha.app.domain.usecase.transaction.SyncTransactionsUseCase;
import com.casha.app.domain.usecase.transaction.UpdateIncomeUseCase;
import com.casha.app.domain.usecase.transaction.UpdateTransactionUseCase;
import com.casha.app.ui.feature.auth.ForgotPasswordViewModel;
import com.casha.app.ui.feature.auth.ForgotPasswordViewModel_HiltModules;
import com.casha.app.ui.feature.auth.ForgotPasswordViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.auth.ForgotPasswordViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.auth.LoginViewModel;
import com.casha.app.ui.feature.auth.LoginViewModel_HiltModules;
import com.casha.app.ui.feature.auth.LoginViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.auth.LoginViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.auth.RegisterViewModel;
import com.casha.app.ui.feature.auth.RegisterViewModel_HiltModules;
import com.casha.app.ui.feature.auth.RegisterViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.auth.RegisterViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.auth.SetupCurrencyViewModel;
import com.casha.app.ui.feature.auth.SetupCurrencyViewModel_HiltModules;
import com.casha.app.ui.feature.auth.SetupCurrencyViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.auth.SetupCurrencyViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.budget.BudgetViewModel;
import com.casha.app.ui.feature.budget.BudgetViewModel_HiltModules;
import com.casha.app.ui.feature.budget.BudgetViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.budget.BudgetViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.dashboard.DashboardViewModel;
import com.casha.app.ui.feature.dashboard.DashboardViewModel_HiltModules;
import com.casha.app.ui.feature.dashboard.DashboardViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.dashboard.DashboardViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.goaltracker.GoalTrackerViewModel;
import com.casha.app.ui.feature.goaltracker.GoalTrackerViewModel_HiltModules;
import com.casha.app.ui.feature.goaltracker.GoalTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.goaltracker.GoalTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.loading.AppLoadingViewModel;
import com.casha.app.ui.feature.loading.AppLoadingViewModel_HiltModules;
import com.casha.app.ui.feature.loading.AppLoadingViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.loading.AppLoadingViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.main.MainViewModel;
import com.casha.app.ui.feature.main.MainViewModel_HiltModules;
import com.casha.app.ui.feature.main.MainViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.main.MainViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.profile.CategoryViewModel;
import com.casha.app.ui.feature.profile.CategoryViewModel_HiltModules;
import com.casha.app.ui.feature.profile.CategoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.profile.CategoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.profile.ProfileViewModel;
import com.casha.app.ui.feature.profile.ProfileViewModel_HiltModules;
import com.casha.app.ui.feature.profile.ProfileViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.profile.ProfileViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.report.ReportViewModel;
import com.casha.app.ui.feature.report.ReportViewModel_HiltModules;
import com.casha.app.ui.feature.report.ReportViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.report.ReportViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.transaction.TransactionViewModel;
import com.casha.app.ui.feature.transaction.TransactionViewModel_HiltModules;
import com.casha.app.ui.feature.transaction.TransactionViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.transaction.TransactionViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.transaction.coordinator.AddMessageViewModel;
import com.casha.app.ui.feature.transaction.coordinator.AddMessageViewModel_HiltModules;
import com.casha.app.ui.feature.transaction.coordinator.AddMessageViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.transaction.coordinator.AddMessageViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.transaction.coordinator.AddTransactionCoordinatorViewModel;
import com.casha.app.ui.feature.transaction.coordinator.AddTransactionCoordinatorViewModel_HiltModules;
import com.casha.app.ui.feature.transaction.coordinator.AddTransactionCoordinatorViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.transaction.coordinator.AddTransactionCoordinatorViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import kotlinx.serialization.json.Json;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class DaggerCashaApplication_HiltComponents_SingletonC {
  private DaggerCashaApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public CashaApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements CashaApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public CashaApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements CashaApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public CashaApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements CashaApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public CashaApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements CashaApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CashaApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements CashaApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CashaApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements CashaApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public CashaApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements CashaApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public CashaApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends CashaApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends CashaApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends CashaApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends CashaApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    private DeleteAllLocalDataUseCase deleteAllLocalDataUseCase() {
      return new DeleteAllLocalDataUseCase(singletonCImpl.authManagerProvider.get(), singletonCImpl.provideDatabaseProvider.get());
    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
      injectMainActivity2(mainActivity);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(ImmutableMap.<String, Boolean>builderWithExpectedSize(15).put(AddMessageViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AddMessageViewModel_HiltModules.KeyModule.provide()).put(AddTransactionCoordinatorViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AddTransactionCoordinatorViewModel_HiltModules.KeyModule.provide()).put(AppLoadingViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AppLoadingViewModel_HiltModules.KeyModule.provide()).put(BudgetViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, BudgetViewModel_HiltModules.KeyModule.provide()).put(CategoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CategoryViewModel_HiltModules.KeyModule.provide()).put(DashboardViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, DashboardViewModel_HiltModules.KeyModule.provide()).put(ForgotPasswordViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ForgotPasswordViewModel_HiltModules.KeyModule.provide()).put(GoalTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, GoalTrackerViewModel_HiltModules.KeyModule.provide()).put(LoginViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LoginViewModel_HiltModules.KeyModule.provide()).put(MainViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MainViewModel_HiltModules.KeyModule.provide()).put(ProfileViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ProfileViewModel_HiltModules.KeyModule.provide()).put(RegisterViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, RegisterViewModel_HiltModules.KeyModule.provide()).put(ReportViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ReportViewModel_HiltModules.KeyModule.provide()).put(SetupCurrencyViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SetupCurrencyViewModel_HiltModules.KeyModule.provide()).put(TransactionViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TransactionViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @CanIgnoreReturnValue
    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectAuthManager(instance, singletonCImpl.authManagerProvider.get());
      MainActivity_MembersInjector.injectDeleteAllLocalDataUseCase(instance, deleteAllLocalDataUseCase());
      MainActivity_MembersInjector.injectNotificationManager(instance, singletonCImpl.notificationManagerProvider.get());
      return instance;
    }
  }

  private static final class ViewModelCImpl extends CashaApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AddMessageViewModel> addMessageViewModelProvider;

    private Provider<AddTransactionCoordinatorViewModel> addTransactionCoordinatorViewModelProvider;

    private Provider<AppLoadingViewModel> appLoadingViewModelProvider;

    private Provider<BudgetViewModel> budgetViewModelProvider;

    private Provider<CategoryViewModel> categoryViewModelProvider;

    private Provider<DashboardViewModel> dashboardViewModelProvider;

    private Provider<ForgotPasswordViewModel> forgotPasswordViewModelProvider;

    private Provider<GoalTrackerViewModel> goalTrackerViewModelProvider;

    private Provider<LoginViewModel> loginViewModelProvider;

    private Provider<MainViewModel> mainViewModelProvider;

    private Provider<ProfileViewModel> profileViewModelProvider;

    private Provider<RegisterViewModel> registerViewModelProvider;

    private Provider<ReportViewModel> reportViewModelProvider;

    private Provider<SetupCurrencyViewModel> setupCurrencyViewModelProvider;

    private Provider<TransactionViewModel> transactionViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    private GetProfileUseCase getProfileUseCase() {
      return new GetProfileUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private CashflowSyncUseCase cashflowSyncUseCase() {
      return new CashflowSyncUseCase(singletonCImpl.cashflowRepositoryImplProvider.get(), singletonCImpl.transactionDao(), singletonCImpl.incomeDao());
    }

    private GetBudgetsUseCase getBudgetsUseCase() {
      return new GetBudgetsUseCase(singletonCImpl.budgetRepositoryImplProvider.get());
    }

    private AddBudgetUseCase addBudgetUseCase() {
      return new AddBudgetUseCase(singletonCImpl.budgetRepositoryImplProvider.get());
    }

    private UpdateBudgetUseCase updateBudgetUseCase() {
      return new UpdateBudgetUseCase(singletonCImpl.budgetRepositoryImplProvider.get());
    }

    private DeleteBudgetUseCase deleteBudgetUseCase() {
      return new DeleteBudgetUseCase(singletonCImpl.budgetRepositoryImplProvider.get());
    }

    private GetBudgetSummaryUseCase getBudgetSummaryUseCase() {
      return new GetBudgetSummaryUseCase(singletonCImpl.budgetRepositoryImplProvider.get());
    }

    private GetBudgetRecommendationsUseCase getBudgetRecommendationsUseCase() {
      return new GetBudgetRecommendationsUseCase(singletonCImpl.budgetRepositoryImplProvider.get());
    }

    private ApplyBudgetRecommendationsUseCase applyBudgetRecommendationsUseCase() {
      return new ApplyBudgetRecommendationsUseCase(singletonCImpl.budgetRepositoryImplProvider.get());
    }

    private BudgetSyncUseCase budgetSyncUseCase() {
      return new BudgetSyncUseCase(singletonCImpl.budgetRepositoryImplProvider.get());
    }

    private CategorySyncUseCase categorySyncUseCase() {
      return new CategorySyncUseCase(singletonCImpl.categoryRepositoryImplProvider.get(), singletonCImpl.categoryDao());
    }

    private CreateCategoryUseCase createCategoryUseCase() {
      return new CreateCategoryUseCase(singletonCImpl.categoryRepositoryImplProvider.get());
    }

    private UpdateCategoryUseCase updateCategoryUseCase() {
      return new UpdateCategoryUseCase(singletonCImpl.categoryRepositoryImplProvider.get());
    }

    private DeleteCategoryUseCase deleteCategoryUseCase() {
      return new DeleteCategoryUseCase(singletonCImpl.categoryRepositoryImplProvider.get());
    }

    private GetRecentTransactionsUseCase getRecentTransactionsUseCase() {
      return new GetRecentTransactionsUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
    }

    private GetTotalSpendingUseCase getTotalSpendingUseCase() {
      return new GetTotalSpendingUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
    }

    private GetSpendingReportUseCase getSpendingReportUseCase() {
      return new GetSpendingReportUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
    }

    private GetUnsyncTransactionCountUseCase getUnsyncTransactionCountUseCase() {
      return new GetUnsyncTransactionCountUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
    }

    private GetCashflowHistoryUseCase getCashflowHistoryUseCase() {
      return new GetCashflowHistoryUseCase(singletonCImpl.cashflowRepositoryImplProvider.get());
    }

    private GetCashflowSummaryUseCase getCashflowSummaryUseCase() {
      return new GetCashflowSummaryUseCase(singletonCImpl.cashflowRepositoryImplProvider.get());
    }

    private GetGoalsUseCase getGoalsUseCase() {
      return new GetGoalsUseCase(singletonCImpl.goalRepositoryImplProvider.get());
    }

    private GetGoalSummaryUseCase getGoalSummaryUseCase() {
      return new GetGoalSummaryUseCase(singletonCImpl.goalRepositoryImplProvider.get());
    }

    private TransactionSyncUseCase transactionSyncUseCase() {
      return new TransactionSyncUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
    }

    private ResetPasswordUseCase resetPasswordUseCase() {
      return new ResetPasswordUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private LoginUseCase loginUseCase() {
      return new LoginUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private GoogleLoginUseCase googleLoginUseCase() {
      return new GoogleLoginUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private com.casha.app.domain.usecase.profile.GetProfileUseCase getProfileUseCase2() {
      return new com.casha.app.domain.usecase.profile.GetProfileUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private UpdateProfileUseCase updateProfileUseCase() {
      return new UpdateProfileUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private DeleteAccountUseCase deleteAccountUseCase() {
      return new DeleteAccountUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private DeleteAllLocalDataUseCase deleteAllLocalDataUseCase() {
      return new DeleteAllLocalDataUseCase(singletonCImpl.authManagerProvider.get(), singletonCImpl.provideDatabaseProvider.get());
    }

    private RegisterUseCase registerUseCase() {
      return new RegisterUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private GetCategorySpendingUseCase getCategorySpendingUseCase() {
      return new GetCategorySpendingUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
    }

    private GetTransactionByCategoryUseCase getTransactionByCategoryUseCase() {
      return new GetTransactionByCategoryUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
    }

    private com.casha.app.domain.usecase.auth.UpdateProfileUseCase updateProfileUseCase2() {
      return new com.casha.app.domain.usecase.auth.UpdateProfileUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private GetTransactionsUseCase getTransactionsUseCase() {
      return new GetTransactionsUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
    }

    private AddTransactionUseCase addTransactionUseCase() {
      return new AddTransactionUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
    }

    private AddIncomeUseCase addIncomeUseCase() {
      return new AddIncomeUseCase(singletonCImpl.incomeRepositoryImplProvider.get());
    }

    private UpdateTransactionUseCase updateTransactionUseCase() {
      return new UpdateTransactionUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
    }

    private DeleteTransactionUseCase deleteTransactionUseCase() {
      return new DeleteTransactionUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
    }

    private SyncTransactionsUseCase syncTransactionsUseCase() {
      return new SyncTransactionsUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
    }

    private GetIncomesUseCase getIncomesUseCase() {
      return new GetIncomesUseCase(singletonCImpl.incomeRepositoryImplProvider.get());
    }

    private UpdateIncomeUseCase updateIncomeUseCase() {
      return new UpdateIncomeUseCase(singletonCImpl.incomeRepositoryImplProvider.get());
    }

    private DeleteIncomeUseCase deleteIncomeUseCase() {
      return new DeleteIncomeUseCase(singletonCImpl.incomeRepositoryImplProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.addMessageViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.addTransactionCoordinatorViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.appLoadingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.budgetViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.categoryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.dashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.forgotPasswordViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.goalTrackerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.loginViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.mainViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.profileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.registerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.reportViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.setupCurrencyViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
      this.transactionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 14);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(ImmutableMap.<String, javax.inject.Provider<ViewModel>>builderWithExpectedSize(15).put(AddMessageViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) addMessageViewModelProvider)).put(AddTransactionCoordinatorViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) addTransactionCoordinatorViewModelProvider)).put(AppLoadingViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) appLoadingViewModelProvider)).put(BudgetViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) budgetViewModelProvider)).put(CategoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) categoryViewModelProvider)).put(DashboardViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) dashboardViewModelProvider)).put(ForgotPasswordViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) forgotPasswordViewModelProvider)).put(GoalTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) goalTrackerViewModelProvider)).put(LoginViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) loginViewModelProvider)).put(MainViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) mainViewModelProvider)).put(ProfileViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) profileViewModelProvider)).put(RegisterViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) registerViewModelProvider)).put(ReportViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) reportViewModelProvider)).put(SetupCurrencyViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) setupCurrencyViewModelProvider)).put(TransactionViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) transactionViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<Class<?>, Object>of();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.casha.app.ui.feature.transaction.coordinator.AddMessageViewModel 
          return (T) new AddMessageViewModel(singletonCImpl.chatRepositoryImplProvider.get(), singletonCImpl.syncEventBusProvider.get());

          case 1: // com.casha.app.ui.feature.transaction.coordinator.AddTransactionCoordinatorViewModel 
          return (T) new AddTransactionCoordinatorViewModel(singletonCImpl.subscriptionManagerProvider.get(), singletonCImpl.syncEventBusProvider.get());

          case 2: // com.casha.app.ui.feature.loading.AppLoadingViewModel 
          return (T) new AppLoadingViewModel(viewModelCImpl.getProfileUseCase(), viewModelCImpl.cashflowSyncUseCase());

          case 3: // com.casha.app.ui.feature.budget.BudgetViewModel 
          return (T) new BudgetViewModel(viewModelCImpl.getBudgetsUseCase(), viewModelCImpl.addBudgetUseCase(), viewModelCImpl.updateBudgetUseCase(), viewModelCImpl.deleteBudgetUseCase(), viewModelCImpl.getBudgetSummaryUseCase(), viewModelCImpl.getBudgetRecommendationsUseCase(), viewModelCImpl.applyBudgetRecommendationsUseCase(), viewModelCImpl.budgetSyncUseCase(), viewModelCImpl.categorySyncUseCase(), singletonCImpl.networkMonitorProvider.get());

          case 4: // com.casha.app.ui.feature.profile.CategoryViewModel 
          return (T) new CategoryViewModel(viewModelCImpl.categorySyncUseCase(), viewModelCImpl.createCategoryUseCase(), viewModelCImpl.updateCategoryUseCase(), viewModelCImpl.deleteCategoryUseCase());

          case 5: // com.casha.app.ui.feature.dashboard.DashboardViewModel 
          return (T) new DashboardViewModel(viewModelCImpl.getRecentTransactionsUseCase(), viewModelCImpl.getTotalSpendingUseCase(), viewModelCImpl.getSpendingReportUseCase(), viewModelCImpl.getUnsyncTransactionCountUseCase(), viewModelCImpl.getCashflowHistoryUseCase(), viewModelCImpl.getCashflowSummaryUseCase(), viewModelCImpl.getGoalsUseCase(), viewModelCImpl.getGoalSummaryUseCase(), viewModelCImpl.cashflowSyncUseCase(), viewModelCImpl.transactionSyncUseCase(), viewModelCImpl.getProfileUseCase(), singletonCImpl.authManagerProvider.get(), singletonCImpl.networkMonitorProvider.get(), singletonCImpl.syncEventBusProvider.get());

          case 6: // com.casha.app.ui.feature.auth.ForgotPasswordViewModel 
          return (T) new ForgotPasswordViewModel(viewModelCImpl.resetPasswordUseCase());

          case 7: // com.casha.app.ui.feature.goaltracker.GoalTrackerViewModel 
          return (T) new GoalTrackerViewModel(viewModelCImpl.getGoalsUseCase(), viewModelCImpl.getGoalSummaryUseCase());

          case 8: // com.casha.app.ui.feature.auth.LoginViewModel 
          return (T) new LoginViewModel(viewModelCImpl.loginUseCase(), viewModelCImpl.googleLoginUseCase(), singletonCImpl.authManagerProvider.get());

          case 9: // com.casha.app.ui.feature.main.MainViewModel 
          return (T) new MainViewModel(singletonCImpl.notificationManagerProvider.get());

          case 10: // com.casha.app.ui.feature.profile.ProfileViewModel 
          return (T) new ProfileViewModel(viewModelCImpl.getProfileUseCase2(), viewModelCImpl.updateProfileUseCase(), viewModelCImpl.deleteAccountUseCase(), viewModelCImpl.deleteAllLocalDataUseCase(), singletonCImpl.authManagerProvider.get(), singletonCImpl.networkMonitorProvider.get());

          case 11: // com.casha.app.ui.feature.auth.RegisterViewModel 
          return (T) new RegisterViewModel(viewModelCImpl.registerUseCase(), singletonCImpl.authManagerProvider.get());

          case 12: // com.casha.app.ui.feature.report.ReportViewModel 
          return (T) new ReportViewModel(viewModelCImpl.getCategorySpendingUseCase(), viewModelCImpl.getTransactionByCategoryUseCase());

          case 13: // com.casha.app.ui.feature.auth.SetupCurrencyViewModel 
          return (T) new SetupCurrencyViewModel(singletonCImpl.authManagerProvider.get(), viewModelCImpl.updateProfileUseCase2());

          case 14: // com.casha.app.ui.feature.transaction.TransactionViewModel 
          return (T) new TransactionViewModel(viewModelCImpl.getCashflowHistoryUseCase(), viewModelCImpl.getTransactionsUseCase(), viewModelCImpl.addTransactionUseCase(), viewModelCImpl.addIncomeUseCase(), viewModelCImpl.updateTransactionUseCase(), viewModelCImpl.deleteTransactionUseCase(), viewModelCImpl.syncTransactionsUseCase(), viewModelCImpl.categorySyncUseCase(), viewModelCImpl.getIncomesUseCase(), viewModelCImpl.updateIncomeUseCase(), viewModelCImpl.deleteIncomeUseCase(), singletonCImpl.syncEventBusProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends CashaApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends CashaApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectCashaFirebaseMessagingService(
        CashaFirebaseMessagingService cashaFirebaseMessagingService) {
      injectCashaFirebaseMessagingService2(cashaFirebaseMessagingService);
    }

    @CanIgnoreReturnValue
    private CashaFirebaseMessagingService injectCashaFirebaseMessagingService2(
        CashaFirebaseMessagingService instance) {
      CashaFirebaseMessagingService_MembersInjector.injectAuthRepository(instance, singletonCImpl.authRepositoryImplProvider.get());
      CashaFirebaseMessagingService_MembersInjector.injectNotificationManager(instance, singletonCImpl.notificationManagerProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends CashaApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AuthManager> authManagerProvider;

    private Provider<CashaDatabase> provideDatabaseProvider;

    private Provider<NotificationManager> notificationManagerProvider;

    private Provider<AuthInterceptor> authInterceptorProvider;

    private Provider<ErrorInterceptor> errorInterceptorProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Json> provideJsonProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<ChatApiService> provideChatApiServiceProvider;

    private Provider<ChatRepositoryImpl> chatRepositoryImplProvider;

    private Provider<SyncEventBus> syncEventBusProvider;

    private Provider<SubscriptionManager> subscriptionManagerProvider;

    private Provider<AuthApiService> provideAuthApiServiceProvider;

    private Provider<AuthRepositoryImpl> authRepositoryImplProvider;

    private Provider<CashflowApiService> provideCashflowApiServiceProvider;

    private Provider<CashflowRepositoryImpl> cashflowRepositoryImplProvider;

    private Provider<BudgetApiService> provideBudgetApiServiceProvider;

    private Provider<BudgetRepositoryImpl> budgetRepositoryImplProvider;

    private Provider<CategoryApiService> provideCategoryApiServiceProvider;

    private Provider<CategoryRepositoryImpl> categoryRepositoryImplProvider;

    private Provider<NetworkMonitor> networkMonitorProvider;

    private Provider<TransactionApiService> provideTransactionApiServiceProvider;

    private Provider<TransactionRepositoryImpl> transactionRepositoryImplProvider;

    private Provider<GoalApiService> provideGoalApiServiceProvider;

    private Provider<GoalRepositoryImpl> goalRepositoryImplProvider;

    private Provider<IncomeApiService> provideIncomeApiServiceProvider;

    private Provider<IncomeRepositoryImpl> incomeRepositoryImplProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);
      initialize2(applicationContextModuleParam);

    }

    private TransactionDao transactionDao() {
      return DatabaseModule_ProvideTransactionDaoFactory.provideTransactionDao(provideDatabaseProvider.get());
    }

    private IncomeDao incomeDao() {
      return DatabaseModule_ProvideIncomeDaoFactory.provideIncomeDao(provideDatabaseProvider.get());
    }

    private BudgetDao budgetDao() {
      return DatabaseModule_ProvideBudgetDaoFactory.provideBudgetDao(provideDatabaseProvider.get());
    }

    private CategoryDao categoryDao() {
      return DatabaseModule_ProvideCategoryDaoFactory.provideCategoryDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.authManagerProvider = DoubleCheck.provider(new SwitchingProvider<AuthManager>(singletonCImpl, 0));
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<CashaDatabase>(singletonCImpl, 1));
      this.notificationManagerProvider = DoubleCheck.provider(new SwitchingProvider<NotificationManager>(singletonCImpl, 2));
      this.authInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<AuthInterceptor>(singletonCImpl, 7));
      this.errorInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<ErrorInterceptor>(singletonCImpl, 8));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 6));
      this.provideJsonProvider = DoubleCheck.provider(new SwitchingProvider<Json>(singletonCImpl, 9));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 5));
      this.provideChatApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<ChatApiService>(singletonCImpl, 4));
      this.chatRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<ChatRepositoryImpl>(singletonCImpl, 3));
      this.syncEventBusProvider = DoubleCheck.provider(new SwitchingProvider<SyncEventBus>(singletonCImpl, 10));
      this.subscriptionManagerProvider = DoubleCheck.provider(new SwitchingProvider<SubscriptionManager>(singletonCImpl, 11));
      this.provideAuthApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<AuthApiService>(singletonCImpl, 13));
      this.authRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepositoryImpl>(singletonCImpl, 12));
      this.provideCashflowApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<CashflowApiService>(singletonCImpl, 15));
      this.cashflowRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<CashflowRepositoryImpl>(singletonCImpl, 14));
      this.provideBudgetApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<BudgetApiService>(singletonCImpl, 17));
      this.budgetRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<BudgetRepositoryImpl>(singletonCImpl, 16));
      this.provideCategoryApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<CategoryApiService>(singletonCImpl, 19));
      this.categoryRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<CategoryRepositoryImpl>(singletonCImpl, 18));
      this.networkMonitorProvider = DoubleCheck.provider(new SwitchingProvider<NetworkMonitor>(singletonCImpl, 20));
      this.provideTransactionApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<TransactionApiService>(singletonCImpl, 22));
      this.transactionRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<TransactionRepositoryImpl>(singletonCImpl, 21));
      this.provideGoalApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<GoalApiService>(singletonCImpl, 24));
      this.goalRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<GoalRepositoryImpl>(singletonCImpl, 23));
    }

    @SuppressWarnings("unchecked")
    private void initialize2(final ApplicationContextModule applicationContextModuleParam) {
      this.provideIncomeApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<IncomeApiService>(singletonCImpl, 26));
      this.incomeRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<IncomeRepositoryImpl>(singletonCImpl, 25));
    }

    @Override
    public void injectCashaApplication(CashaApplication cashaApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.casha.app.core.auth.AuthManager 
          return (T) new AuthManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // com.casha.app.data.local.database.CashaDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.casha.app.core.notification.NotificationManager 
          return (T) new NotificationManager();

          case 3: // com.casha.app.data.remote.impl.ChatRepositoryImpl 
          return (T) new ChatRepositoryImpl(singletonCImpl.provideChatApiServiceProvider.get(), singletonCImpl.transactionDao(), singletonCImpl.incomeDao(), singletonCImpl.provideJsonProvider.get());

          case 4: // com.casha.app.data.remote.api.ChatApiService 
          return (T) NetworkModule_ProvideChatApiServiceFactory.provideChatApiService(singletonCImpl.provideRetrofitProvider.get());

          case 5: // retrofit2.Retrofit 
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.provideJsonProvider.get());

          case 6: // okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient(singletonCImpl.authInterceptorProvider.get(), singletonCImpl.errorInterceptorProvider.get());

          case 7: // com.casha.app.core.network.AuthInterceptor 
          return (T) new AuthInterceptor(singletonCImpl.authManagerProvider.get());

          case 8: // com.casha.app.core.network.ErrorInterceptor 
          return (T) new ErrorInterceptor();

          case 9: // kotlinx.serialization.json.Json 
          return (T) NetworkModule_ProvideJsonFactory.provideJson();

          case 10: // com.casha.app.core.network.SyncEventBus 
          return (T) new SyncEventBus();

          case 11: // com.casha.app.core.auth.SubscriptionManager 
          return (T) new SubscriptionManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 12: // com.casha.app.data.remote.impl.AuthRepositoryImpl 
          return (T) new AuthRepositoryImpl(singletonCImpl.provideAuthApiServiceProvider.get());

          case 13: // com.casha.app.data.remote.api.AuthApiService 
          return (T) NetworkModule_ProvideAuthApiServiceFactory.provideAuthApiService(singletonCImpl.provideRetrofitProvider.get());

          case 14: // com.casha.app.data.remote.impl.CashflowRepositoryImpl 
          return (T) new CashflowRepositoryImpl(singletonCImpl.provideCashflowApiServiceProvider.get());

          case 15: // com.casha.app.data.remote.api.CashflowApiService 
          return (T) NetworkModule_ProvideCashflowApiServiceFactory.provideCashflowApiService(singletonCImpl.provideRetrofitProvider.get());

          case 16: // com.casha.app.data.remote.impl.BudgetRepositoryImpl 
          return (T) new BudgetRepositoryImpl(singletonCImpl.provideBudgetApiServiceProvider.get(), singletonCImpl.budgetDao());

          case 17: // com.casha.app.data.remote.api.BudgetApiService 
          return (T) NetworkModule_ProvideBudgetApiServiceFactory.provideBudgetApiService(singletonCImpl.provideRetrofitProvider.get());

          case 18: // com.casha.app.data.remote.impl.CategoryRepositoryImpl 
          return (T) new CategoryRepositoryImpl(singletonCImpl.provideCategoryApiServiceProvider.get(), singletonCImpl.categoryDao());

          case 19: // com.casha.app.data.remote.api.CategoryApiService 
          return (T) NetworkModule_ProvideCategoryApiServiceFactory.provideCategoryApiService(singletonCImpl.provideRetrofitProvider.get());

          case 20: // com.casha.app.core.network.NetworkMonitor 
          return (T) new NetworkMonitor(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 21: // com.casha.app.data.remote.impl.TransactionRepositoryImpl 
          return (T) new TransactionRepositoryImpl(singletonCImpl.provideTransactionApiServiceProvider.get(), singletonCImpl.provideCashflowApiServiceProvider.get(), singletonCImpl.transactionDao());

          case 22: // com.casha.app.data.remote.api.TransactionApiService 
          return (T) NetworkModule_ProvideTransactionApiServiceFactory.provideTransactionApiService(singletonCImpl.provideRetrofitProvider.get());

          case 23: // com.casha.app.data.remote.impl.GoalRepositoryImpl 
          return (T) new GoalRepositoryImpl(singletonCImpl.provideGoalApiServiceProvider.get());

          case 24: // com.casha.app.data.remote.api.GoalApiService 
          return (T) NetworkModule_ProvideGoalApiServiceFactory.provideGoalApiService(singletonCImpl.provideRetrofitProvider.get());

          case 25: // com.casha.app.data.remote.impl.IncomeRepositoryImpl 
          return (T) new IncomeRepositoryImpl(singletonCImpl.provideIncomeApiServiceProvider.get(), singletonCImpl.provideCashflowApiServiceProvider.get(), singletonCImpl.incomeDao());

          case 26: // com.casha.app.data.remote.api.IncomeApiService 
          return (T) NetworkModule_ProvideIncomeApiServiceFactory.provideIncomeApiService(singletonCImpl.provideRetrofitProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
