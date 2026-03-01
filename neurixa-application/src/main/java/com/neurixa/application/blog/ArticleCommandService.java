package com.neurixa.application.blog;

import com.neurixa.domain.blog.Article;
import com.neurixa.domain.blog.ArticleId;
import com.neurixa.domain.blog.ArticleRepository;
import java.util.Objects;
import java.util.UUID;

public class ArticleCommandService {

    private final ArticleRepository articleRepository;

    public ArticleCommandService(ArticleRepository articleRepository) {
        this.articleRepository = Objects.requireNonNull(articleRepository);
    }

    public Article createDraft(String title, String content, String excerpt) {
        Article article = Article.createDraft(title, content, excerpt);
        articleRepository.save(article);
        return article;
    }

    public Article update(UUID articleId, String title, String content, String excerpt) {
        Article article = articleRepository.findById(new ArticleId(articleId))
                .orElseThrow(() -> new IllegalArgumentException("Article not found."));
        article.update(title, content, excerpt);
        articleRepository.save(article);
        return article;
    }

    public void delete(UUID articleId) {
        Article article = articleRepository.findById(new ArticleId(articleId))
                .orElseThrow(() -> new IllegalArgumentException("Article not found."));
        article.softDelete();
        articleRepository.save(article);
    }

    public Article publish(UUID articleId) {
        Article article = articleRepository.findById(new ArticleId(articleId))
                .orElseThrow(() -> new IllegalArgumentException("Article not found."));
        article.publish();
        articleRepository.save(article);
        return article;
    }

    public Article restore(UUID articleId) {
        Article article = articleRepository.findById(new ArticleId(articleId))
                .orElseThrow(() -> new IllegalArgumentException("Article not found."));
        article.restore();
        articleRepository.save(article);
        return article;
    }

    public Article archive(UUID articleId) {
        Article article = articleRepository.findById(new ArticleId(articleId))
                .orElseThrow(() -> new IllegalArgumentException("Article not found."));
        article.archive();
        articleRepository.save(article);
        return article;
    }

    public void incrementViewCount(UUID articleId) {
        articleRepository.incrementViewCountAtomic(new ArticleId(articleId));
    }
}
