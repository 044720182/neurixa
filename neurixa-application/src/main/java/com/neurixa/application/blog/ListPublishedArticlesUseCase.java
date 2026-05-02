package com.neurixa.application.blog;

import com.neurixa.domain.blog.Article;
import com.neurixa.domain.blog.ArticleRepository;
import com.neurixa.domain.blog.ArticleStatus;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListPublishedArticlesUseCase {

    private final ArticleRepository articleRepository;

    public ListPublishedArticlesUseCase(ArticleRepository articleRepository) {
        this.articleRepository = Objects.requireNonNull(articleRepository);
    }

    public List<Article> execute() {
        return articleRepository.findPublished(0, Integer.MAX_VALUE);
    }
}
