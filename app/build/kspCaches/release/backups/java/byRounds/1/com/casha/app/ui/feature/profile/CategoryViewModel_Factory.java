package com.casha.app.ui.feature.profile;

import com.casha.app.domain.usecase.category.CategorySyncUseCase;
import com.casha.app.domain.usecase.category.CreateCategoryUseCase;
import com.casha.app.domain.usecase.category.DeleteCategoryUseCase;
import com.casha.app.domain.usecase.category.UpdateCategoryUseCase;
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
public final class CategoryViewModel_Factory implements Factory<CategoryViewModel> {
  private final Provider<CategorySyncUseCase> syncUseCaseProvider;

  private final Provider<CreateCategoryUseCase> createUseCaseProvider;

  private final Provider<UpdateCategoryUseCase> updateUseCaseProvider;

  private final Provider<DeleteCategoryUseCase> deleteUseCaseProvider;

  public CategoryViewModel_Factory(Provider<CategorySyncUseCase> syncUseCaseProvider,
      Provider<CreateCategoryUseCase> createUseCaseProvider,
      Provider<UpdateCategoryUseCase> updateUseCaseProvider,
      Provider<DeleteCategoryUseCase> deleteUseCaseProvider) {
    this.syncUseCaseProvider = syncUseCaseProvider;
    this.createUseCaseProvider = createUseCaseProvider;
    this.updateUseCaseProvider = updateUseCaseProvider;
    this.deleteUseCaseProvider = deleteUseCaseProvider;
  }

  @Override
  public CategoryViewModel get() {
    return newInstance(syncUseCaseProvider.get(), createUseCaseProvider.get(), updateUseCaseProvider.get(), deleteUseCaseProvider.get());
  }

  public static CategoryViewModel_Factory create(
      javax.inject.Provider<CategorySyncUseCase> syncUseCaseProvider,
      javax.inject.Provider<CreateCategoryUseCase> createUseCaseProvider,
      javax.inject.Provider<UpdateCategoryUseCase> updateUseCaseProvider,
      javax.inject.Provider<DeleteCategoryUseCase> deleteUseCaseProvider) {
    return new CategoryViewModel_Factory(Providers.asDaggerProvider(syncUseCaseProvider), Providers.asDaggerProvider(createUseCaseProvider), Providers.asDaggerProvider(updateUseCaseProvider), Providers.asDaggerProvider(deleteUseCaseProvider));
  }

  public static CategoryViewModel_Factory create(Provider<CategorySyncUseCase> syncUseCaseProvider,
      Provider<CreateCategoryUseCase> createUseCaseProvider,
      Provider<UpdateCategoryUseCase> updateUseCaseProvider,
      Provider<DeleteCategoryUseCase> deleteUseCaseProvider) {
    return new CategoryViewModel_Factory(syncUseCaseProvider, createUseCaseProvider, updateUseCaseProvider, deleteUseCaseProvider);
  }

  public static CategoryViewModel newInstance(CategorySyncUseCase syncUseCase,
      CreateCategoryUseCase createUseCase, UpdateCategoryUseCase updateUseCase,
      DeleteCategoryUseCase deleteUseCase) {
    return new CategoryViewModel(syncUseCase, createUseCase, updateUseCase, deleteUseCase);
  }
}
