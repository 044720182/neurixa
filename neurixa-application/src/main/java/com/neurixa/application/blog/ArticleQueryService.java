package com.neurixa.application.blog;

import com.neurixa.domain.blog.Article;
import com.neurixa.domain.blog.ArticleRepository;
import com.neurixa.domain.blog.Slug;
import java.util.List;
import java.util.Objects;

public class ArticleQueryService {

    private final ArticleRepository articleRepository;

    public ArticleQueryService(ArticleRepository articleRepository) {
        this.articleRepository = Objects.requireNonNull(articleRepository);
    }

    public Article getBySlug(String slug) {
        return articleRepository.findBySlug(new Slug(slug))
                .orElseThrow(() -> new IllegalArgumentException("Article not found."));
    }

    public List<Article> listPublished() {
        return articleRepository.findPublished();
    }
}
