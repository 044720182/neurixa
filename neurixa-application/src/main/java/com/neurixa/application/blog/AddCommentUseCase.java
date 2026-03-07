package com.neurixa.application.blog;

import com.neurixa.domain.blog.Comment;
import com.neurixa.domain.blog.CommentRepository;
import java.util.Objects;
import java.util.UUID;

public class AddCommentUseCase {

    private final CommentRepository commentRepository;

    public AddCommentUseCase(CommentRepository commentRepository) {
        this.commentRepository = Objects.requireNonNull(commentRepository);
    }

    public Comment execute(UUID articleId, String authorName, String authorEmail, String content, UUID replyTo) {
        Comment comment = Comment.create(articleId, authorName, authorEmail, content, replyTo);
        commentRepository.save(comment);
        return comment;
    }
}
