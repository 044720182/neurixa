package com.neurixa.domain.blog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository {
    void save(Comment comment);
    Optional<Comment> findById(CommentId id);
    List<Comment> findByArticleIdAndStatus(UUID articleId, CommentStatus status);
}
