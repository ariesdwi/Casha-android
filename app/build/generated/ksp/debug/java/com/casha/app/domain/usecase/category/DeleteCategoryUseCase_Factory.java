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
public final class DeleteCategoryUseCase_Factory implements Factory<DeleteCategoryUseCase> {
  private final Provider<CategoryRepository> categoryRepositoryProvider;

  public DeleteCategoryUseCase_Factory(Provider<CategoryRepository> categoryRepositoryProvider) {
    this.categoryRepositoryProvider = categoryRepositoryProvider;
  }

  @Override
  public DeleteCategoryUseCase get() {
    return newInstance(categoryRepositoryProvider.get());
  }

  public static DeleteCategoryUseCase_Factory create(
      javax.inject.Provider<CategoryRepository> categoryRepositoryProvider) {
    return new DeleteCategoryUseCase_Factory(Providers.asDaggerProvider(categoryRepositoryProvider));
  }

  public static DeleteCategoryUseCase_Factory create(
      Provider<CategoryRepository> categoryRepositoryProvider) {
    return new DeleteCategoryUseCase_Factory(categoryRepositoryProvider);
  }

  public static DeleteCategoryUseCase newInstance(CategoryRepository categoryRepository) {
    return new DeleteCategoryUseCase(categoryRepository);
  }
}
