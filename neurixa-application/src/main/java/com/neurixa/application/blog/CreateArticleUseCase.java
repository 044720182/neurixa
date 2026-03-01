package com.neurixa.application.blog;

import com.neurixa.domain.blog.Article;
import com.neurixa.domain.blog.ArticleRepository;
import java.util.Objects;

public class CreateArticleUseCase {

    private final ArticleRepository articleRepository;

    public CreateArticleUseCase(ArticleRepository articleRepository) {
        this.articleRepository = Objects.requireNonNull(articleRepository);
    }

    public Article execute(String title, String content, String excerpt) {
        Article article = Article.createDraft(title, content, excerpt);
        articleRepository.save(article);
        return article;
    }
}
