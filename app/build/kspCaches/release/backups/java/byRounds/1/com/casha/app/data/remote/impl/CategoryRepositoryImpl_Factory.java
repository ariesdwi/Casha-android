package com.casha.app.data.remote.impl;

import com.casha.app.data.local.dao.CategoryDao;
import com.casha.app.data.remote.api.CategoryApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class CategoryRepositoryImpl_Factory implements Factory<CategoryRepositoryImpl> {
  private final Provider<CategoryApiService> apiServiceProvider;

  private final Provider<CategoryDao> categoryDaoProvider;

  public CategoryRepositoryImpl_Factory(Provider<CategoryApiService> apiServiceProvider,
      Provider<CategoryDao> categoryDaoProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.categoryDaoProvider = categoryDaoProvider;
  }

  @Override
  public CategoryRepositoryImpl get() {
    return newInstance(apiServiceProvider.get(), categoryDaoProvider.get());
  }

  public static CategoryRepositoryImpl_Factory create(
      javax.inject.Provider<CategoryApiService> apiServiceProvider,
      javax.inject.Provider<CategoryDao> categoryDaoProvider) {
    return new CategoryRepositoryImpl_Factory(Providers.asDaggerProvider(apiServiceProvider), Providers.asDaggerProvider(categoryDaoProvider));
  }

  public static CategoryRepositoryImpl_Factory create(
      Provider<CategoryApiService> apiServiceProvider, Provider<CategoryDao> categoryDaoProvider) {
    return new CategoryRepositoryImpl_Factory(apiServiceProvider, categoryDaoProvider);
  }

  public static CategoryRepositoryImpl newInstance(CategoryApiService apiService,
      CategoryDao categoryDao) {
    return new CategoryRepositoryImpl(apiService, categoryDao);
  }
}
