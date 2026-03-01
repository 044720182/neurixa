package com.neurixa.domain.blog;

import java.util.Optional;

public interface TagRepository {
    void save(Tag tag);
    Optional<Tag> findById(TagId id);
    Optional<Tag> findBySlug(Slug slug);
}
