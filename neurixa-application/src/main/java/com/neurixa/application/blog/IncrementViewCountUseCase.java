package com.neurixa.application.blog;

import com.neurixa.domain.blog.ArticleId;
import com.neurixa.domain.blog.ArticleRepository;
import java.util.Objects;
import java.util.UUID;

public class IncrementViewCountUseCase {

    private final ArticleRepository articleRepository;

    public IncrementViewCountUseCase(ArticleRepository articleRepository) {
        this.articleRepository = Objects.requireNonNull(articleRepository);
    }

    public void execute(UUID articleId) {
        articleRepository.incrementViewCountAtomic(new ArticleId(articleId));
    }
}
