package com.neurixa.domain.blog;

import com.neurixa.domain.blog.event.CommentApprovedEvent;
import com.neurixa.domain.blog.shared.BaseAggregateRoot;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Comment extends BaseAggregateRoot<CommentId> {

    private final CommentId id;
    private final ArticleId articleId;
    private final String authorName;
    private final String authorEmail;
    private final String content;
    private CommentStatus status;
    private final CommentId replyTo;
    private final Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private Instant deletedAt;

    private Comment(CommentId id, ArticleId articleId, String authorName, String authorEmail, String content, CommentId replyTo, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.articleId = Objects.requireNonNull(articleId);
        if (authorName == null || authorName.isBlank()) {
            throw new IllegalArgumentException("Author name cannot be empty.");
        }
        this.authorName = authorName;
        if (authorEmail == null || authorEmail.isBlank()) {
            throw new IllegalArgumentException("Author email cannot be empty.");
        }
        this.authorEmail = authorEmail;
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment content cannot be empty.");
        }
        this.content = content;
        this.replyTo = replyTo;
        this.status = CommentStatus.PENDING;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = this.createdAt;
        this.deleted = false;
    }

    public static Comment create(UUID articleId, String authorName, String authorEmail, String content, UUID replyTo) {
        return new Comment(CommentId.generate(), new ArticleId(Objects.requireNonNull(articleId)), authorName, authorEmail, content, replyTo != null ? new CommentId(replyTo) : null, Instant.now());
    }

    public static Comment fromState(CommentId id,
                                    ArticleId articleId,
                                    String authorName,
                                    String authorEmail,
                                    String content,
                                    CommentStatus status,
                                    CommentId replyTo,
                                    Instant createdAt,
                                    Instant updatedAt,
                                    boolean deleted,
                                    Instant deletedAt) {
        Comment comment = new Comment(id, articleId, authorName, authorEmail, content, replyTo, createdAt);
        comment.status = Objects.requireNonNull(status);
        comment.updatedAt = updatedAt != null ? updatedAt : createdAt;
        comment.deleted = deleted;
        comment.deletedAt = deletedAt;
        return comment;
    }

    public void approve() {
        if (this.status != CommentStatus.PENDING) {
            throw new IllegalStateException("Only a pending comment can be approved.");
        }
        this.status = CommentStatus.APPROVED;
        this.updatedAt = Instant.now();
        registerEvent(new CommentApprovedEvent(this.id));
    }

    public void reject() {
        if (this.status != CommentStatus.PENDING) {
            throw new IllegalStateException("Only a pending comment can be rejected.");
        }
        this.status = CommentStatus.REJECTED;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        if (this.deleted || this.status == CommentStatus.DELETED) {
            return;
        }
        this.status = CommentStatus.DELETED;
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.updatedAt = this.deletedAt;
    }

    @Override
    protected CommentId getId() {
        return id;
    }

    public CommentId getCommentId() {
        return id;
    }

    public ArticleId getArticleId() {
        return articleId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public String getContent() {
        return content;
    }

    public CommentStatus getStatus() {
        return status;
    }

    public CommentId getReplyTo() {
        return replyTo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
