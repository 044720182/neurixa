package com.neurixa.domain.blog;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {
    void save(Article article);
    Optional<Article> findById(ArticleId id);
    Optional<Article> findBySlug(Slug slug);
    void incrementViewCountAtomic(ArticleId id);
    List<Article> findPublished(int page, int size);
    long countPublished();
}
