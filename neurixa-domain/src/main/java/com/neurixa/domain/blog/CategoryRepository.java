package com.neurixa.domain.blog;

import java.util.Optional;

public interface CategoryRepository {
    void save(Category category);
    Optional<Category> findById(CategoryId id);
    Optional<Category> findBySlug(Slug slug);
}
