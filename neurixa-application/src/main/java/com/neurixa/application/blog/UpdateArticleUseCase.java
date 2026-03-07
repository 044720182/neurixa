package com.neurixa.application.blog;

import com.neurixa.domain.blog.Article;
import com.neurixa.domain.blog.ArticleRepository;
import com.neurixa.domain.blog.ArticleId;
import java.util.Objects;
import java.util.UUID;

public class UpdateArticleUseCase {

    private final ArticleRepository articleRepository;

    public UpdateArticleUseCase(ArticleRepository articleRepository) {
        this.articleRepository = Objects.requireNonNull(articleRepository);
    }

    public Article execute(UUID articleId, String title, String content, String excerpt) {
        Article article = articleRepository.findById(new ArticleId(articleId))
                .orElseThrow(() -> new IllegalArgumentException("Article not found."));
        article.update(title, content, excerpt);
        articleRepository.save(article);
        return article;
    }
}
