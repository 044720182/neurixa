package com.neurixa.application.blog;

import com.neurixa.domain.blog.Article;
import com.neurixa.domain.blog.ArticleId;
import com.neurixa.domain.blog.ArticleRepository;
import com.neurixa.domain.blog.ArticleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ArticleCommandServiceTest {

    private ArticleRepository articleRepository;
    private ArticleCommandService service;

    @BeforeEach
    void setUp() {
        articleRepository = mock(ArticleRepository.class);
        service = new ArticleCommandService(articleRepository);
    }

    // ── createDraft ──────────────────────────────────────────────────────────

    @Test
    void shouldCreateDraftArticle() {
        Article result = service.createDraft("Hello World", "Content here", "Short excerpt");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Hello World");
        assertThat(result.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        verify(articleRepository).save(result);
    }

    @Test
    void shouldThrowWhenCreatingDraftWithBlankTitle() {
        assertThatThrownBy(() -> service.createDraft("", "Content", "Excerpt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Title cannot be empty");
    }

    @Test
    void shouldThrowWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new ArticleCommandService(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ── publish ───────────────────────────────────────────────────────────────

    @Test
    void shouldPublishDraftArticle() {
        Article draft = Article.createDraft("My Title", "Some content", "excerpt");
        UUID id = draft.getArticleId().getValue();
        when(articleRepository.findById(new ArticleId(id))).thenReturn(Optional.of(draft));

        Article result = service.publish(id);

        assertThat(result.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(result.getPublishedAt()).isNotNull();
        verify(articleRepository).save(draft);
    }

    @Test
    void shouldThrowWhenPublishingNonExistentArticle() {
        UUID id = UUID.randomUUID();
        when(articleRepository.findById(new ArticleId(id))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.publish(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Article not found");
    }

    @Test
    void shouldThrowWhenPublishingDeletedArticle() {
        Article draft = Article.createDraft("Title", "Content", "excerpt");
        draft.softDelete();
        UUID id = draft.getArticleId().getValue();
        when(articleRepository.findById(new ArticleId(id))).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.publish(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot publish a deleted article");
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void shouldUpdateArticle() {
        Article draft = Article.createDraft("Old Title", "Old content", "old excerpt");
        UUID id = draft.getArticleId().getValue();
        when(articleRepository.findById(new ArticleId(id))).thenReturn(Optional.of(draft));

        Article result = service.update(id, "New Title", "New content", "new excerpt");

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getContent()).isEqualTo("New content");
        verify(articleRepository).save(draft);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentArticle() {
        UUID id = UUID.randomUUID();
        when(articleRepository.findById(new ArticleId(id))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, "Title", "Content", "excerpt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Article not found");
    }

    @Test
    void shouldThrowWhenUpdatingDeletedArticle() {
        Article draft = Article.createDraft("Title", "Content", "excerpt");
        draft.softDelete();
        UUID id = draft.getArticleId().getValue();
        when(articleRepository.findById(new ArticleId(id))).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.update(id, "New Title", "New content", "new excerpt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot update a deleted article");
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void shouldSoftDeleteDraftArticle() {
        Article draft = Article.createDraft("Title", "Content", "excerpt");
        UUID id = draft.getArticleId().getValue();
        when(articleRepository.findById(new ArticleId(id))).thenReturn(Optional.of(draft));

        service.delete(id);

        assertThat(draft.isDeleted()).isTrue();
        assertThat(draft.getStatus()).isEqualTo(ArticleStatus.DELETED);
        verify(articleRepository).save(draft);
    }

    @Test
    void shouldThrowWhenDeletingPublishedArticle() {
        Article article = Article.createDraft("Title", "Content", "excerpt");
        article.publish();
        UUID id = article.getArticleId().getValue();
        when(articleRepository.findById(new ArticleId(id))).thenReturn(Optional.of(article));

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete a published article");
    }

    @Test
    void shouldThrowWhenDeletingNonExistentArticle() {
        UUID id = UUID.randomUUID();
        when(articleRepository.findById(new ArticleId(id))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Article not found");
    }

    // ── archive ───────────────────────────────────────────────────────────────

    @Test
    void shouldArchivePublishedArticle() {
        Article article = Article.createDraft("Title", "Content", "excerpt");
        article.publish();
        UUID id = article.getArticleId().getValue();
        when(articleRepository.findById(new ArticleId(id))).thenReturn(Optional.of(article));

        Article result = service.archive(id);

        assertThat(result.getStatus()).isEqualTo(ArticleStatus.ARCHIVED);
        verify(articleRepository).save(article);
    }

    @Test
    void shouldThrowWhenArchivingDraftArticle() {
        Article draft = Article.createDraft("Title", "Content", "excerpt");
        UUID id = draft.getArticleId().getValue();
        when(articleRepository.findById(new ArticleId(id))).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.archive(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only a published article can be archived");
    }

    // ── restore ───────────────────────────────────────────────────────────────

    @Test
    void shouldRestoreDeletedArticle() {
        Article draft = Article.createDraft("Title", "Content", "excerpt");
        draft.softDelete();
        UUID id = draft.getArticleId().getValue();
        when(articleRepository.findById(new ArticleId(id))).thenReturn(Optional.of(draft));

        Article result = service.restore(id);

        assertThat(result.isDeleted()).isFalse();
        assertThat(result.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        verify(articleRepository).save(draft);
    }

    @Test
    void shouldThrowWhenRestoringNonDeletedArticle() {
        Article draft = Article.createDraft("Title", "Content", "excerpt");
        UUID id = draft.getArticleId().getValue();
        when(articleRepository.findById(new ArticleId(id))).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.restore(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only a deleted article can be restored");
    }
}
