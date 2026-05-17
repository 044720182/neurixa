package com.neurixa.domain.blog.event;

import com.neurixa.core.event.DomainEvent;

/**
 * Domain event published when a comment is approved by a moderator.
 * 
 * This event indicates that a comment has passed moderation and is now
 * visible to the public. Handlers might send notifications or update comment counts.
 */
public class CommentApprovedEvent extends DomainEvent {
    private final String commentId;
    private final String articleId;
    private final String authorName;

    /**
     * Create a comment approved event.
     * 
     * @param commentId The ID of the approved comment
     * @param articleId The ID of the article the comment belongs to
     * @param authorName The name of the comment author
     */
    public CommentApprovedEvent(String commentId, String articleId, String authorName) {
        super();
        this.commentId = commentId;
        this.articleId = articleId;
        this.authorName = authorName;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getArticleId() {
        return articleId;
    }

    public String getAuthorName() {
        return authorName;
    }

    @Override
    public String toString() {
        return "CommentApprovedEvent{" +
                "commentId='" + commentId + '\'' +
                ", articleId='" + articleId + '\'' +
                ", authorName='" + authorName + '\'' +
                ", eventId='" + getEventId() + '\'' +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
