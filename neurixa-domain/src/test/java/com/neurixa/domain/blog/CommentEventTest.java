package com.neurixa.domain.blog;

import com.neurixa.domain.blog.event.CommentApprovedEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentEventTest {

    @Test
    void shouldEmitCommentApprovedEvent() {
        Comment comment = Comment.create(UUID.randomUUID(), "Alice", "alice@example.com", "Nice post", null);
        comment.approve();
        var events = comment.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(CommentApprovedEvent.class);
    }
}
