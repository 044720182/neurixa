package com.neurixa.domain.blog;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArticleTest {

    @Test
    void shouldCreateDraftArticle() {
        Article article = Article.createDraft("Test Title", "Test Content", "Test Excerpt");
        assertThat(article.getTitle()).isEqualTo("Test Title");
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        assertThat(article.getSlug().getValue()).isEqualTo("test-title");
    }

    @Test
    void shouldNotPublishArticleWithoutTitle() {
        assertThatThrownBy(() -> Article.createDraft("", "Test Content", "Test Excerpt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title cannot be empty.");
    }

    @Test
    void shouldNotPublishArticleWithoutContent() {
        Article article = Article.createDraft("Test Title", "", "Test Excerpt");
        assertThatThrownBy(article::publish)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Article must have a title and content to be published.");
    }

    @Test
    void shouldPublishArticle() {
        Article article = Article.createDraft("Test Title", "Test Content", "Test Excerpt");
        article.publish();
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(article.getPublishedAt()).isNotNull();
    }

    @Test
    void shouldNotDeletePublishedArticle() {
        Article article = Article.createDraft("Test Title", "Test Content", "Test Excerpt");
        article.publish();
        assertThatThrownBy(article::softDelete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot delete a published article directly (must archive first).");
    }

    @Test
    void shouldArchivePublishedArticle() {
        Article article = Article.createDraft("Test Title", "Test Content", "Test Excerpt");
        article.publish();
        article.archive();
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.ARCHIVED);
    }

    @Test
    void shouldDeleteArchivedArticle() {
        Article article = Article.createDraft("Test Title", "Test Content", "Test Excerpt");
        article.publish();
        article.archive();
        article.softDelete();
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.DELETED);
    }

    @Test
    void shouldUpdateContent() {
        Article article = Article.createDraft("Title", "Content", "Excerpt");
        article.updateContent("New Content", "New Excerpt");
        assertThat(article.getContent()).isEqualTo("New Content");
    }
}
