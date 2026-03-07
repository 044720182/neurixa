package com.neurixa.domain.blog;

import java.time.Instant;
import java.util.Objects;

public class Tag {

    private final TagId id;
    private String name;
    private Slug slug;
    private boolean deleted;
    private Instant deletedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private Tag(TagId id, String name, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.setName(name);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = this.createdAt;
        this.deleted = false;
    }

    public static Tag create(String name) {
        return new Tag(TagId.generate(), name, Instant.now());
    }

    public static Tag fromState(TagId id,
                                String name,
                                Slug slug,
                                boolean deleted,
                                Instant deletedAt,
                                Instant createdAt,
                                Instant updatedAt) {
        Tag tag = new Tag(id, name, createdAt);
        tag.slug = Objects.requireNonNull(slug);
        tag.deleted = deleted;
        tag.deletedAt = deletedAt;
        tag.updatedAt = updatedAt != null ? updatedAt : createdAt;
        return tag;
    }

    public void update(String name) {
        this.setName(name);
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
            throw new IllegalArgumentException("Tag name cannot be empty.");
        }
        this.name = name;
        this.slug = new Slug(name);
    }

    public TagId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Slug getSlug() {
        return slug;
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
