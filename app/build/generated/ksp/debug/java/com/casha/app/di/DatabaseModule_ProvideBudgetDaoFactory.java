package com.casha.app.di;

import com.casha.app.data.local.dao.BudgetDao;
import com.casha.app.data.local.database.CashaDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideBudgetDaoFactory implements Factory<BudgetDao> {
  private final Provider<CashaDatabase> dbProvider;

  public DatabaseModule_ProvideBudgetDaoFactory(Provider<CashaDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public BudgetDao get() {
    return provideBudgetDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideBudgetDaoFactory create(
      javax.inject.Provider<CashaDatabase> dbProvider) {
    return new DatabaseModule_ProvideBudgetDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static DatabaseModule_ProvideBudgetDaoFactory create(Provider<CashaDatabase> dbProvider) {
    return new DatabaseModule_ProvideBudgetDaoFactory(dbProvider);
  }

  public static BudgetDao provideBudgetDao(CashaDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideBudgetDao(db));
  }
}
