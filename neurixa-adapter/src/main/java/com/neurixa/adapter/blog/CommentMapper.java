package com.neurixa.adapter.blog;

import com.neurixa.domain.blog.Comment;
import com.neurixa.domain.blog.CommentId;
import com.neurixa.domain.blog.ArticleId;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDocument toDocument(Comment comment) {
        CommentDocument doc = new CommentDocument();
        doc.setId(comment.getCommentId().getValue());
        doc.setArticleId(comment.getArticleId().getValue());
        doc.setAuthorName(comment.getAuthorName());
        doc.setAuthorEmail(comment.getAuthorEmail());
        doc.setContent(comment.getContent());
        doc.setStatus(comment.getStatus());
        doc.setReplyTo(comment.getReplyTo() != null ? comment.getReplyTo().getValue() : null);
        doc.setCreatedAt(comment.getCreatedAt());
        doc.setUpdatedAt(comment.getUpdatedAt());
        doc.setDeleted(comment.isDeleted());
        doc.setDeletedAt(comment.getDeletedAt());
        return doc;
    }

    public Comment toDomain(CommentDocument doc) {
        return Comment.fromState(
                new CommentId(doc.getId()),
                new ArticleId(doc.getArticleId()),
                doc.getAuthorName(),
                doc.getAuthorEmail(),
                doc.getContent(),
                doc.getStatus(),
                doc.getReplyTo() != null ? new CommentId(doc.getReplyTo()) : null,
                doc.getCreatedAt(),
                doc.getUpdatedAt(),
                doc.isDeleted(),
                doc.getDeletedAt()
        );
    }
}
