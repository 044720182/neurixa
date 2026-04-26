package com.neurixa.application.blog;

import com.neurixa.domain.blog.Article;
import com.neurixa.domain.blog.ArticleRepository;
import com.neurixa.domain.blog.Slug;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleQueryServiceTest {

    @Mock
    ArticleRepository articleRepository;

    @InjectMocks
    ArticleQueryService service;

    @Test
    void shouldGetArticleBySlug() {
        Article article = Article.createDraft("Hello World", "Content", "excerpt");
        // derive slug from domain — don't hardcode the slug generation logic
        Slug slug = article.getSlug();
        when(articleRepository.findBySlug(slug)).thenReturn(Optional.of(article));

        Article result = service.getBySlug(slug.getValue());

        assertThat(result.getTitle()).isEqualTo("Hello World");
        verify(articleRepository).findBySlug(slug);
    }

    @Test
    void shouldThrowWhenArticleNotFoundBySlug() {
        Slug slug = new Slug("not-found");
        when(articleRepository.findBySlug(slug)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBySlug("not-found"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Article not found");
    }

    @Test
    void shouldListPublishedArticles() {
        Article a1 = Article.createDraft("Post 1", "Content 1", "excerpt 1");
        Article a2 = Article.createDraft("Post 2", "Content 2", "excerpt 2");
        when(articleRepository.findPublished(0, 10)).thenReturn(List.of(a1, a2));

        List<Article> result = service.listPublished(0, 10);

        assertThat(result).hasSize(2);
        verify(articleRepository).findPublished(0, 10);
    }

    @Test
    void shouldReturnEmptyListWhenNoPublishedArticles() {
        when(articleRepository.findPublished(0, 10)).thenReturn(List.of());

        List<Article> result = service.listPublished(0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldCountPublishedArticles() {
        when(articleRepository.countPublished()).thenReturn(42L);

        long count = service.countPublished();

        assertThat(count).isEqualTo(42L);
    }

    @Test
    void shouldReturnZeroWhenNoPublishedArticles() {
        when(articleRepository.countPublished()).thenReturn(0L);

        assertThat(service.countPublished()).isZero();
    }

    @Test
    void shouldThrowWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new ArticleQueryService(null))
                .isInstanceOf(NullPointerException.class);
    }
}
