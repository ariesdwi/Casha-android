package com.casha.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.casha.app.core.auth.AuthManager;
import com.casha.app.core.network.AuthInterceptor;
import com.casha.app.core.network.ErrorInterceptor;
import com.casha.app.core.network.NetworkMonitor;
import com.casha.app.data.local.dao.IncomeDao;
import com.casha.app.data.local.dao.TransactionDao;
import com.casha.app.data.local.database.CashaDatabase;
import com.casha.app.data.remote.api.AuthApiService;
import com.casha.app.data.remote.api.CashflowApiService;
import com.casha.app.data.remote.api.GoalApiService;
import com.casha.app.data.remote.api.TransactionApiService;
import com.casha.app.data.remote.impl.AuthRepositoryImpl;
import com.casha.app.data.remote.impl.CashflowRepositoryImpl;
import com.casha.app.data.remote.impl.GoalRepositoryImpl;
import com.casha.app.data.remote.impl.TransactionRepositoryImpl;
import com.casha.app.di.DatabaseModule_ProvideDatabaseFactory;
import com.casha.app.di.DatabaseModule_ProvideIncomeDaoFactory;
import com.casha.app.di.DatabaseModule_ProvideTransactionDaoFactory;
import com.casha.app.di.NetworkModule_ProvideAuthApiServiceFactory;
import com.casha.app.di.NetworkModule_ProvideCashflowApiServiceFactory;
import com.casha.app.di.NetworkModule_ProvideGoalApiServiceFactory;
import com.casha.app.di.NetworkModule_ProvideJsonFactory;
import com.casha.app.di.NetworkModule_ProvideOkHttpClientFactory;
import com.casha.app.di.NetworkModule_ProvideRetrofitFactory;
import com.casha.app.di.NetworkModule_ProvideTransactionApiServiceFactory;
import com.casha.app.domain.usecase.auth.GetProfileUseCase;
import com.casha.app.domain.usecase.auth.GoogleLoginUseCase;
import com.casha.app.domain.usecase.auth.LoginUseCase;
import com.casha.app.domain.usecase.auth.RegisterUseCase;
import com.casha.app.domain.usecase.auth.ResetPasswordUseCase;
import com.casha.app.domain.usecase.auth.UpdateProfileUseCase;
import com.casha.app.domain.usecase.auth.logout.DeleteAllLocalDataUseCase;
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
import com.casha.app.domain.usecase.transaction.AddTransactionUseCase;
import com.casha.app.domain.usecase.transaction.DeleteTransactionUseCase;
import com.casha.app.domain.usecase.transaction.GetTransactionsUseCase;
import com.casha.app.domain.usecase.transaction.SyncTransactionsUseCase;
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
import com.casha.app.ui.feature.profile.ProfileViewModel;
import com.casha.app.ui.feature.profile.ProfileViewModel_HiltModules;
import com.casha.app.ui.feature.profile.ProfileViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.profile.ProfileViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.casha.app.ui.feature.transaction.TransactionViewModel;
import com.casha.app.ui.feature.transaction.TransactionViewModel_HiltModules;
import com.casha.app.ui.feature.transaction.TransactionViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.casha.app.ui.feature.transaction.TransactionViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
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
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
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
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(9).put(AppLoadingViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AppLoadingViewModel_HiltModules.KeyModule.provide()).put(DashboardViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, DashboardViewModel_HiltModules.KeyModule.provide()).put(ForgotPasswordViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ForgotPasswordViewModel_HiltModules.KeyModule.provide()).put(GoalTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, GoalTrackerViewModel_HiltModules.KeyModule.provide()).put(LoginViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LoginViewModel_HiltModules.KeyModule.provide()).put(ProfileViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ProfileViewModel_HiltModules.KeyModule.provide()).put(RegisterViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, RegisterViewModel_HiltModules.KeyModule.provide()).put(SetupCurrencyViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SetupCurrencyViewModel_HiltModules.KeyModule.provide()).put(TransactionViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TransactionViewModel_HiltModules.KeyModule.provide()).build());
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

    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectAuthManager(instance, singletonCImpl.authManagerProvider.get());
      MainActivity_MembersInjector.injectDeleteAllLocalDataUseCase(instance, deleteAllLocalDataUseCase());
      return instance;
    }
  }

  private static final class ViewModelCImpl extends CashaApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AppLoadingViewModel> appLoadingViewModelProvider;

    private Provider<DashboardViewModel> dashboardViewModelProvider;

    private Provider<ForgotPasswordViewModel> forgotPasswordViewModelProvider;

    private Provider<GoalTrackerViewModel> goalTrackerViewModelProvider;

    private Provider<LoginViewModel> loginViewModelProvider;

    private Provider<ProfileViewModel> profileViewModelProvider;

    private Provider<RegisterViewModel> registerViewModelProvider;

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

    private DeleteAllLocalDataUseCase deleteAllLocalDataUseCase() {
      return new DeleteAllLocalDataUseCase(singletonCImpl.authManagerProvider.get(), singletonCImpl.provideDatabaseProvider.get());
    }

    private RegisterUseCase registerUseCase() {
      return new RegisterUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private UpdateProfileUseCase updateProfileUseCase() {
      return new UpdateProfileUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private GetTransactionsUseCase getTransactionsUseCase() {
      return new GetTransactionsUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
    }

    private AddTransactionUseCase addTransactionUseCase() {
      return new AddTransactionUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
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

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.appLoadingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.dashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.forgotPasswordViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.goalTrackerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.loginViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.profileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.registerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.setupCurrencyViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.transactionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(9).put(AppLoadingViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) appLoadingViewModelProvider)).put(DashboardViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) dashboardViewModelProvider)).put(ForgotPasswordViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) forgotPasswordViewModelProvider)).put(GoalTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) goalTrackerViewModelProvider)).put(LoginViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) loginViewModelProvider)).put(ProfileViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) profileViewModelProvider)).put(RegisterViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) registerViewModelProvider)).put(SetupCurrencyViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) setupCurrencyViewModelProvider)).put(TransactionViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) transactionViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
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
          case 0: // com.casha.app.ui.feature.loading.AppLoadingViewModel 
          return (T) new AppLoadingViewModel(viewModelCImpl.getProfileUseCase(), viewModelCImpl.cashflowSyncUseCase());

          case 1: // com.casha.app.ui.feature.dashboard.DashboardViewModel 
          return (T) new DashboardViewModel(viewModelCImpl.getRecentTransactionsUseCase(), viewModelCImpl.getTotalSpendingUseCase(), viewModelCImpl.getSpendingReportUseCase(), viewModelCImpl.getUnsyncTransactionCountUseCase(), viewModelCImpl.getCashflowHistoryUseCase(), viewModelCImpl.getCashflowSummaryUseCase(), viewModelCImpl.getGoalsUseCase(), viewModelCImpl.getGoalSummaryUseCase(), viewModelCImpl.cashflowSyncUseCase(), viewModelCImpl.transactionSyncUseCase(), viewModelCImpl.getProfileUseCase(), singletonCImpl.authManagerProvider.get(), singletonCImpl.networkMonitorProvider.get());

          case 2: // com.casha.app.ui.feature.auth.ForgotPasswordViewModel 
          return (T) new ForgotPasswordViewModel(viewModelCImpl.resetPasswordUseCase());

          case 3: // com.casha.app.ui.feature.goaltracker.GoalTrackerViewModel 
          return (T) new GoalTrackerViewModel(viewModelCImpl.getGoalsUseCase(), viewModelCImpl.getGoalSummaryUseCase());

          case 4: // com.casha.app.ui.feature.auth.LoginViewModel 
          return (T) new LoginViewModel(viewModelCImpl.loginUseCase(), viewModelCImpl.googleLoginUseCase(), singletonCImpl.authManagerProvider.get());

          case 5: // com.casha.app.ui.feature.profile.ProfileViewModel 
          return (T) new ProfileViewModel(viewModelCImpl.deleteAllLocalDataUseCase());

          case 6: // com.casha.app.ui.feature.auth.RegisterViewModel 
          return (T) new RegisterViewModel(viewModelCImpl.registerUseCase(), singletonCImpl.authManagerProvider.get());

          case 7: // com.casha.app.ui.feature.auth.SetupCurrencyViewModel 
          return (T) new SetupCurrencyViewModel(singletonCImpl.authManagerProvider.get(), viewModelCImpl.updateProfileUseCase());

          case 8: // com.casha.app.ui.feature.transaction.TransactionViewModel 
          return (T) new TransactionViewModel(viewModelCImpl.getTransactionsUseCase(), viewModelCImpl.addTransactionUseCase(), viewModelCImpl.updateTransactionUseCase(), viewModelCImpl.deleteTransactionUseCase(), viewModelCImpl.syncTransactionsUseCase());

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
  }

  private static final class SingletonCImpl extends CashaApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AuthManager> authManagerProvider;

    private Provider<CashaDatabase> provideDatabaseProvider;

    private Provider<AuthInterceptor> authInterceptorProvider;

    private Provider<ErrorInterceptor> errorInterceptorProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Json> provideJsonProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<AuthApiService> provideAuthApiServiceProvider;

    private Provider<AuthRepositoryImpl> authRepositoryImplProvider;

    private Provider<CashflowApiService> provideCashflowApiServiceProvider;

    private Provider<CashflowRepositoryImpl> cashflowRepositoryImplProvider;

    private Provider<TransactionApiService> provideTransactionApiServiceProvider;

    private Provider<TransactionRepositoryImpl> transactionRepositoryImplProvider;

    private Provider<GoalApiService> provideGoalApiServiceProvider;

    private Provider<GoalRepositoryImpl> goalRepositoryImplProvider;

    private Provider<NetworkMonitor> networkMonitorProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private TransactionDao transactionDao() {
      return DatabaseModule_ProvideTransactionDaoFactory.provideTransactionDao(provideDatabaseProvider.get());
    }

    private IncomeDao incomeDao() {
      return DatabaseModule_ProvideIncomeDaoFactory.provideIncomeDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.authManagerProvider = DoubleCheck.provider(new SwitchingProvider<AuthManager>(singletonCImpl, 0));
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<CashaDatabase>(singletonCImpl, 1));
      this.authInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<AuthInterceptor>(singletonCImpl, 6));
      this.errorInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<ErrorInterceptor>(singletonCImpl, 7));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 5));
      this.provideJsonProvider = DoubleCheck.provider(new SwitchingProvider<Json>(singletonCImpl, 8));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 4));
      this.provideAuthApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<AuthApiService>(singletonCImpl, 3));
      this.authRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepositoryImpl>(singletonCImpl, 2));
      this.provideCashflowApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<CashflowApiService>(singletonCImpl, 10));
      this.cashflowRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<CashflowRepositoryImpl>(singletonCImpl, 9));
      this.provideTransactionApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<TransactionApiService>(singletonCImpl, 12));
      this.transactionRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<TransactionRepositoryImpl>(singletonCImpl, 11));
      this.provideGoalApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<GoalApiService>(singletonCImpl, 14));
      this.goalRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<GoalRepositoryImpl>(singletonCImpl, 13));
      this.networkMonitorProvider = DoubleCheck.provider(new SwitchingProvider<NetworkMonitor>(singletonCImpl, 15));
    }

    @Override
    public void injectCashaApplication(CashaApplication cashaApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
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

          case 2: // com.casha.app.data.remote.impl.AuthRepositoryImpl 
          return (T) new AuthRepositoryImpl(singletonCImpl.provideAuthApiServiceProvider.get());

          case 3: // com.casha.app.data.remote.api.AuthApiService 
          return (T) NetworkModule_ProvideAuthApiServiceFactory.provideAuthApiService(singletonCImpl.provideRetrofitProvider.get());

          case 4: // retrofit2.Retrofit 
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.provideJsonProvider.get());

          case 5: // okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient(singletonCImpl.authInterceptorProvider.get(), singletonCImpl.errorInterceptorProvider.get());

          case 6: // com.casha.app.core.network.AuthInterceptor 
          return (T) new AuthInterceptor(singletonCImpl.authManagerProvider.get());

          case 7: // com.casha.app.core.network.ErrorInterceptor 
          return (T) new ErrorInterceptor();

          case 8: // kotlinx.serialization.json.Json 
          return (T) NetworkModule_ProvideJsonFactory.provideJson();

          case 9: // com.casha.app.data.remote.impl.CashflowRepositoryImpl 
          return (T) new CashflowRepositoryImpl(singletonCImpl.provideCashflowApiServiceProvider.get());

          case 10: // com.casha.app.data.remote.api.CashflowApiService 
          return (T) NetworkModule_ProvideCashflowApiServiceFactory.provideCashflowApiService(singletonCImpl.provideRetrofitProvider.get());

          case 11: // com.casha.app.data.remote.impl.TransactionRepositoryImpl 
          return (T) new TransactionRepositoryImpl(singletonCImpl.provideTransactionApiServiceProvider.get(), singletonCImpl.transactionDao());

          case 12: // com.casha.app.data.remote.api.TransactionApiService 
          return (T) NetworkModule_ProvideTransactionApiServiceFactory.provideTransactionApiService(singletonCImpl.provideRetrofitProvider.get());

          case 13: // com.casha.app.data.remote.impl.GoalRepositoryImpl 
          return (T) new GoalRepositoryImpl(singletonCImpl.provideGoalApiServiceProvider.get());

          case 14: // com.casha.app.data.remote.api.GoalApiService 
          return (T) NetworkModule_ProvideGoalApiServiceFactory.provideGoalApiService(singletonCImpl.provideRetrofitProvider.get());

          case 15: // com.casha.app.core.network.NetworkMonitor 
          return (T) new NetworkMonitor(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
