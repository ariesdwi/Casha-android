package com.casha.app.domain.usecase.category;

import com.casha.app.data.local.dao.CategoryDao;
import com.casha.app.domain.repository.CategoryRepository;
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
public final class CategorySyncUseCase_Factory implements Factory<CategorySyncUseCase> {
  private final Provider<CategoryRepository> categoryRepositoryProvider;

  private final Provider<CategoryDao> categoryDaoProvider;

  public CategorySyncUseCase_Factory(Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<CategoryDao> categoryDaoProvider) {
    this.categoryRepositoryProvider = categoryRepositoryProvider;
    this.categoryDaoProvider = categoryDaoProvider;
  }

  @Override
  public CategorySyncUseCase get() {
    return newInstance(categoryRepositoryProvider.get(), categoryDaoProvider.get());
  }

  public static CategorySyncUseCase_Factory create(
      javax.inject.Provider<CategoryRepository> categoryRepositoryProvider,
      javax.inject.Provider<CategoryDao> categoryDaoProvider) {
    return new CategorySyncUseCase_Factory(Providers.asDaggerProvider(categoryRepositoryProvider), Providers.asDaggerProvider(categoryDaoProvider));
  }

  public static CategorySyncUseCase_Factory create(
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<CategoryDao> categoryDaoProvider) {
    return new CategorySyncUseCase_Factory(categoryRepositoryProvider, categoryDaoProvider);
  }

  public static CategorySyncUseCase newInstance(CategoryRepository categoryRepository,
      CategoryDao categoryDao) {
    return new CategorySyncUseCase(categoryRepository, categoryDao);
  }
}
