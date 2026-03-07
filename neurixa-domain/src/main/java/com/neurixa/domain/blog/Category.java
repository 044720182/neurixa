package com.neurixa.domain.blog;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Category {

    private final CategoryId id;
    private String name;
    private Slug slug;
    private CategoryId parentId;
    private boolean deleted;
    private Instant deletedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private Category(CategoryId id, String name, CategoryId parentId, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.setName(name);
        this.parentId = parentId;
        this.deleted = false;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = this.createdAt;
    }

    public static Category create(String name, UUID parentId) {
        return new Category(CategoryId.generate(), name, parentId != null ? new CategoryId(parentId) : null, Instant.now());
    }

    public static Category fromState(CategoryId id,
                                     String name,
                                     Slug slug,
                                     CategoryId parentId,
                                     boolean deleted,
                                     Instant deletedAt,
                                     Instant createdAt,
                                     Instant updatedAt) {
        Category category = new Category(id, name, parentId, createdAt);
        category.slug = Objects.requireNonNull(slug);
        category.deleted = deleted;
        category.deletedAt = deletedAt;
        category.updatedAt = updatedAt != null ? updatedAt : createdAt;
        return category;
    }

    public void update(String name, UUID parentId) {
        this.setName(name);
        this.parentId = parentId != null ? new CategoryId(parentId) : null;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        if (this.deleted) {
            return;
        }
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.updatedAt = this.deletedAt;
    }

    private void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        this.name = name;
        this.slug = new Slug(name);
    }

    public CategoryId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Slug getSlug() {
        return slug;
    }

    public CategoryId getParentId() {
        return parentId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
