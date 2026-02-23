package com.casha.app.domain.usecase.category;

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
public final class UpdateCategoryUseCase_Factory implements Factory<UpdateCategoryUseCase> {
  private final Provider<CategoryRepository> categoryRepositoryProvider;

  public UpdateCategoryUseCase_Factory(Provider<CategoryRepository> categoryRepositoryProvider) {
    this.categoryRepositoryProvider = categoryRepositoryProvider;
  }

  @Override
  public UpdateCategoryUseCase get() {
    return newInstance(categoryRepositoryProvider.get());
  }

  public static UpdateCategoryUseCase_Factory create(
      javax.inject.Provider<CategoryRepository> categoryRepositoryProvider) {
    return new UpdateCategoryUseCase_Factory(Providers.asDaggerProvider(categoryRepositoryProvider));
  }

  public static UpdateCategoryUseCase_Factory create(
      Provider<CategoryRepository> categoryRepositoryProvider) {
    return new UpdateCategoryUseCase_Factory(categoryRepositoryProvider);
  }

  public static UpdateCategoryUseCase newInstance(CategoryRepository categoryRepository) {
    return new UpdateCategoryUseCase(categoryRepository);
  }
}
