package com.neurixa.domain.blog;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SoftDeleteTest {

    @Test
    void shouldSoftDeleteTag() {
        Tag tag = Tag.create("Java");
        tag.softDelete();
        assertThat(tag.isDeleted()).isTrue();
        assertThat(tag.getDeletedAt()).isNotNull();
    }

    @Test
    void shouldSoftDeleteComment() {
        Comment comment = Comment.create(UUID.randomUUID(), "Bob", "bob@example.com", "Great", null);
        comment.softDelete();
        assertThat(comment.isDeleted()).isTrue();
        assertThat(comment.getDeletedAt()).isNotNull();
    }

    @Test
    void shouldSoftDeleteCategory() {
        Category category = Category.create("Tech", null);
        category.softDelete();
        assertThat(category.isDeleted()).isTrue();
        assertThat(category.getDeletedAt()).isNotNull();
    }
}
