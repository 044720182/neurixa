package com.neurixa.boot.dto.response;

import com.neurixa.domain.blog.Category;

import java.time.Instant;
import java.util.UUID;

public class BlogCategoryResponse {
    public UUID id;
    public String name;
    public String slug;
    public UUID parentId;
    public boolean deleted;
    public Instant createdAt;
    public Instant updatedAt;

    public static BlogCategoryResponse from(Category category) {
        BlogCategoryResponse r = new BlogCategoryResponse();
        r.id = category.getId().getValue();
        r.name = category.getName();
        r.slug = category.getSlug().getValue();
        r.parentId = category.getParentId() != null ? category.getParentId().getValue() : null;
        r.deleted = category.isDeleted();
        r.createdAt = category.getCreatedAt();
        r.updatedAt = category.getUpdatedAt();
        return r;
    }
}
