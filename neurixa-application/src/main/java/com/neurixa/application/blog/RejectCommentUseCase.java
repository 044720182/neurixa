package com.neurixa.application.blog;

import com.neurixa.domain.blog.Comment;
import com.neurixa.domain.blog.CommentRepository;
import com.neurixa.domain.blog.CommentId;
import java.util.Objects;
import java.util.UUID;

public class RejectCommentUseCase {

    private final CommentRepository commentRepository;

    public RejectCommentUseCase(CommentRepository commentRepository) {
        this.commentRepository = Objects.requireNonNull(commentRepository);
    }

    public Comment execute(UUID commentId) {
        Comment comment = commentRepository.findById(new CommentId(commentId))
                .orElseThrow(() -> new IllegalArgumentException("Comment not found."));
        comment.reject();
        commentRepository.save(comment);
        return comment;
    }
}
