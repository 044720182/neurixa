package com.neurixa.application.blog;

import com.neurixa.domain.blog.Article;
import com.neurixa.domain.blog.ArticleRepository;
import com.neurixa.domain.blog.ArticleId;
import java.util.Objects;
import java.util.UUID;

public class RestoreArticleUseCase {

    private final ArticleRepository articleRepository;

    public RestoreArticleUseCase(ArticleRepository articleRepository) {
        this.articleRepository = Objects.requireNonNull(articleRepository);
    }

    public Article execute(UUID articleId) {
        Article article = articleRepository.findById(new ArticleId(articleId))
                .orElseThrow(() -> new IllegalArgumentException("Article not found."));
        article.restore();
        articleRepository.save(article);
        return article;
    }
}
