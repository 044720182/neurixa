package com.neurixa.application.blog;

import com.neurixa.domain.blog.Comment;
import com.neurixa.domain.blog.CommentRepository;
import com.neurixa.domain.blog.CommentStatus;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CommentQueryService {

    private final CommentRepository commentRepository;

    public CommentQueryService(CommentRepository commentRepository) {
        this.commentRepository = Objects.requireNonNull(commentRepository);
    }

    public List<Comment> listByArticleAndStatus(UUID articleId, CommentStatus status) {
        return commentRepository.findByArticleIdAndStatus(articleId, status);
    }
}
