package com.neurixa.application.blog;

import com.neurixa.domain.blog.Article;
import com.neurixa.domain.blog.ArticleRepository;
import com.neurixa.domain.blog.Slug;
import java.util.Objects;

public class GetArticleDetailUseCase {

    private final ArticleRepository articleRepository;

    public GetArticleDetailUseCase(ArticleRepository articleRepository) {
        this.articleRepository = Objects.requireNonNull(articleRepository);
    }

    public Article execute(String slug) {
        return articleRepository.findBySlug(new Slug(slug))
                .orElseThrow(() -> new IllegalArgumentException("Article not found."));
    }
}
