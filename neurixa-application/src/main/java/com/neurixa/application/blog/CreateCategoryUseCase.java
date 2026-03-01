package com.neurixa.application.blog;

import com.neurixa.domain.blog.Category;
import com.neurixa.domain.blog.CategoryRepository;
import java.util.Objects;
import java.util.UUID;

public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CreateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = Objects.requireNonNull(categoryRepository);
    }

    public Category execute(String name, UUID parentId) {
        Category category = Category.create(name, parentId);
        categoryRepository.save(category);
        return category;
    }
}
