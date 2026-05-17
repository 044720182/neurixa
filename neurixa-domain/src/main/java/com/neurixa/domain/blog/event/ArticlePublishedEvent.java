package com.neurixa.domain.blog.event;

import com.neurixa.core.event.DomainEvent;

/**
 * Domain event published when an article transitions to PUBLISHED status.
 * 
 * This event indicates that an article is now visible to the public.
 * Handlers might send notifications, update search indexes, or trigger analytics.
 */
public class ArticlePublishedEvent extends DomainEvent {
    private final String articleId;
    private final String title;
    private final String slug;

    /**
     * Create an article published event.
     * 
     * @param articleId The ID of the published article
     * @param title The article title
     * @param slug The URL-friendly slug
     */
    public ArticlePublishedEvent(String articleId, String title, String slug) {
        super();
        this.articleId = articleId;
        this.title = title;
        this.slug = slug;
    }

    public String getArticleId() {
        return articleId;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    @Override
    public String toString() {
        return "ArticlePublishedEvent{" +
                "articleId='" + articleId + '\'' +
                ", title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", eventId='" + getEventId() + '\'' +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
