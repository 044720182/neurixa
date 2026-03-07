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
        // This is a simplified implementation. In a real application, you would
        // likely have a more efficient way to query for published articles.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
