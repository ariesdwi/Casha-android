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
public final class CreateCategoryUseCase_Factory implements Factory<CreateCategoryUseCase> {
  private final Provider<CategoryRepository> categoryRepositoryProvider;

  public CreateCategoryUseCase_Factory(Provider<CategoryRepository> categoryRepositoryProvider) {
    this.categoryRepositoryProvider = categoryRepositoryProvider;
  }

  @Override
  public CreateCategoryUseCase get() {
    return newInstance(categoryRepositoryProvider.get());
  }

  public static CreateCategoryUseCase_Factory create(
      javax.inject.Provider<CategoryRepository> categoryRepositoryProvider) {
    return new CreateCategoryUseCase_Factory(Providers.asDaggerProvider(categoryRepositoryProvider));
  }

  public static CreateCategoryUseCase_Factory create(
      Provider<CategoryRepository> categoryRepositoryProvider) {
    return new CreateCategoryUseCase_Factory(categoryRepositoryProvider);
  }

  public static CreateCategoryUseCase newInstance(CategoryRepository categoryRepository) {
    return new CreateCategoryUseCase(categoryRepository);
  }
}
