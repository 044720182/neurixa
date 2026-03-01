package com.neurixa.application.blog;

import com.neurixa.domain.blog.Article;
import com.neurixa.domain.blog.ArticleId;
import com.neurixa.domain.blog.ArticleRepository;
import com.neurixa.domain.blog.Slug;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleCommandServiceTest {

    static class FakeArticleRepository implements ArticleRepository {
        final List<ArticleId> incrementCalls = new ArrayList<>();

        @Override
        public void save(Article article) {}

        @Override
        public Optional<Article> findById(ArticleId id) {
            return Optional.empty();
        }

        @Override
        public Optional<Article> findBySlug(Slug slug) {
            return Optional.empty();
        }

        @Override
        public void incrementViewCountAtomic(ArticleId id) {
            incrementCalls.add(id);
        }

        @Override
        public List<Article> findPublished() {
            return List.of();
        }
    }

    @Test
    void shouldUseAtomicIncrementInRepository() {
        FakeArticleRepository repo = new FakeArticleRepository();
        ArticleCommandService service = new ArticleCommandService(repo);
        UUID id = UUID.randomUUID();
        service.incrementViewCount(id);
        assertThat(repo.incrementCalls).hasSize(1);
        assertThat(repo.incrementCalls.get(0).getValue()).isEqualTo(id);
    }
}
