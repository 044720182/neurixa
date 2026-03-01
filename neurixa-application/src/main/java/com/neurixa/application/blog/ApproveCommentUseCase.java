package com.neurixa.application.blog;

import com.neurixa.domain.blog.Comment;
import com.neurixa.domain.blog.CommentRepository;
import com.neurixa.domain.blog.CommentId;
import java.util.Objects;
import java.util.UUID;

public class ApproveCommentUseCase {

    private final CommentRepository commentRepository;

    public ApproveCommentUseCase(CommentRepository commentRepository) {
        this.commentRepository = Objects.requireNonNull(commentRepository);
    }

    public Comment execute(UUID commentId) {
        Comment comment = commentRepository.findById(new CommentId(commentId))
                .orElseThrow(() -> new IllegalArgumentException("Comment not found."));
        comment.approve();
        commentRepository.save(comment);
        return comment;
    }
}
