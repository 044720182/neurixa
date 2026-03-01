package com.neurixa.boot.dto.response;

import com.neurixa.domain.blog.Tag;

import java.time.Instant;
import java.util.UUID;

public class BlogTagResponse {
    public UUID id;
    public String name;
    public String slug;
    public boolean deleted;
    public Instant createdAt;
    public Instant updatedAt;

    public static BlogTagResponse from(Tag tag) {
        BlogTagResponse r = new BlogTagResponse();
        r.id = tag.getId().getValue();
        r.name = tag.getName();
        r.slug = tag.getSlug().getValue();
        r.deleted = tag.isDeleted();
        r.createdAt = tag.getCreatedAt();
        r.updatedAt = tag.getUpdatedAt();
        return r;
    }
}
