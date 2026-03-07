package com.neurixa.domain.blog.event;

import com.neurixa.domain.blog.ArticleId;
import com.neurixa.domain.blog.shared.DomainEvent;

import java.time.Instant;

public record ArticlePublishedEvent(ArticleId articleId, Instant occurredOn) implements DomainEvent {
    public ArticlePublishedEvent(ArticleId articleId) {
        this(articleId, Instant.now());
    }
}
