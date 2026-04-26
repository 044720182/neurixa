package com.neurixa.application.blog;

import com.neurixa.domain.blog.Article;
import com.neurixa.domain.blog.ArticleRepository;
import com.neurixa.domain.blog.Slug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ArticleQueryServiceTest {

    private ArticleRepository articleRepository;
    private ArticleQueryService service;

    @BeforeEach
    void setUp() {
        articleRepository = mock(ArticleRepository.class);
        service = new ArticleQueryService(articleRepository);
    }

    @Test
    void shouldGetArticleBySlug() {
        Article article = Article.createDraft("Hello World", "Content", "excerpt");
        when(articleRepository.findBySlug(new Slug("hello-world"))).thenReturn(Optional.of(article));

        Article result = service.getBySlug("hello-world");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Hello World");
    }

    @Test
    void shouldThrowWhenArticleNotFoundBySlug() {
        when(articleRepository.findBySlug(new Slug("not-found"))).thenReturn(Optional.empty());

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
    void shouldThrowWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new ArticleQueryService(null))
                .isInstanceOf(NullPointerException.class);
    }
}
