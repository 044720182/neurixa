package com.neurixa.boot.dto.response;

import com.neurixa.domain.blog.Comment;
import com.neurixa.domain.blog.CommentStatus;

import java.time.Instant;
import java.util.UUID;

public class BlogCommentResponse {
    public UUID id;
    public UUID articleId;
    public String authorName;
    public String authorEmail;
    public String content;
    public CommentStatus status;
    public UUID replyTo;
    public Instant createdAt;
    public Instant updatedAt;

    public static BlogCommentResponse from(Comment comment) {
        BlogCommentResponse r = new BlogCommentResponse();
        r.id = comment.getCommentId().getValue();
        r.articleId = comment.getArticleId().getValue();
        r.authorName = comment.getAuthorName();
        r.authorEmail = comment.getAuthorEmail();
        r.content = comment.getContent();
        r.status = comment.getStatus();
        r.replyTo = comment.getReplyTo() != null ? comment.getReplyTo().getValue() : null;
        r.createdAt = comment.getCreatedAt();
        r.updatedAt = comment.getUpdatedAt();
        return r;
    }
}
