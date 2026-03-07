package com.neurixa.domain.blog.event;

import com.neurixa.domain.blog.CommentId;
import com.neurixa.domain.blog.shared.DomainEvent;

import java.time.Instant;

public record CommentApprovedEvent(CommentId commentId, Instant occurredOn) implements DomainEvent {
    public CommentApprovedEvent(CommentId commentId) {
        this(commentId, Instant.now());
    }
}
