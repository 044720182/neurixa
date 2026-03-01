package com.neurixa.application.blog;

import com.neurixa.domain.blog.Comment;
import com.neurixa.domain.blog.CommentId;
import com.neurixa.domain.blog.CommentRepository;
import java.util.Objects;
import java.util.UUID;

public class CommentCommandService {

    private final CommentRepository commentRepository;

    public CommentCommandService(CommentRepository commentRepository) {
        this.commentRepository = Objects.requireNonNull(commentRepository);
    }

    public Comment add(UUID articleId, String authorName, String authorEmail, String content, UUID replyTo) {
        Comment comment = Comment.create(articleId, authorName, authorEmail, content, replyTo);
        commentRepository.save(comment);
        return comment;
    }

    public Comment approve(UUID commentId) {
        Comment comment = commentRepository.findById(new CommentId(commentId))
                .orElseThrow(() -> new IllegalArgumentException("Comment not found."));
        comment.approve();
        commentRepository.save(comment);
        return comment;
    }

    public Comment reject(UUID commentId) {
        Comment comment = commentRepository.findById(new CommentId(commentId))
                .orElseThrow(() -> new IllegalArgumentException("Comment not found."));
        comment.reject();
        commentRepository.save(comment);
        return comment;
    }

    public void delete(UUID commentId) {
        Comment comment = commentRepository.findById(new CommentId(commentId))
                .orElseThrow(() -> new IllegalArgumentException("Comment not found."));
        comment.softDelete();
        commentRepository.save(comment);
    }
}
