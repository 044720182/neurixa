package com.neurixa.application.blog;

import com.neurixa.domain.blog.Comment;
import com.neurixa.domain.blog.CommentRepository;
import com.neurixa.domain.blog.CommentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentQueryServiceTest {

    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    CommentQueryService service;

    @Test
    void shouldListPendingCommentsByArticle() {
        UUID articleId = UUID.randomUUID();
        Comment c1 = Comment.create(articleId, "Alice", "alice@example.com", "Nice!", null);
        Comment c2 = Comment.create(articleId, "Bob", "bob@example.com", "Thanks!", null);
        when(commentRepository.findByArticleIdAndStatus(articleId, CommentStatus.PENDING))
                .thenReturn(List.of(c1, c2));

        List<Comment> result = service.listByArticleAndStatus(articleId, CommentStatus.PENDING);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> c.getStatus() == CommentStatus.PENDING);
        verify(commentRepository).findByArticleIdAndStatus(articleId, CommentStatus.PENDING);
    }

    @Test
    void shouldListApprovedCommentsByArticle() {
        UUID articleId = UUID.randomUUID();
        Comment c = Comment.create(articleId, "Alice", "alice@example.com", "Great!", null);
        c.approve();
        when(commentRepository.findByArticleIdAndStatus(articleId, CommentStatus.APPROVED))
                .thenReturn(List.of(c));

        List<Comment> result = service.listByArticleAndStatus(articleId, CommentStatus.APPROVED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(CommentStatus.APPROVED);
    }

    @Test
    void shouldListRejectedCommentsByArticle() {
        UUID articleId = UUID.randomUUID();
        Comment c = Comment.create(articleId, "Spammer", "spam@example.com", "Buy now!", null);
        c.reject();
        when(commentRepository.findByArticleIdAndStatus(articleId, CommentStatus.REJECTED))
                .thenReturn(List.of(c));

        List<Comment> result = service.listByArticleAndStatus(articleId, CommentStatus.REJECTED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(CommentStatus.REJECTED);
    }

    @Test
    void shouldReturnEmptyListWhenNoCommentsFound() {
        UUID articleId = UUID.randomUUID();
        when(commentRepository.findByArticleIdAndStatus(articleId, CommentStatus.APPROVED))
                .thenReturn(List.of());

        List<Comment> result = service.listByArticleAndStatus(articleId, CommentStatus.APPROVED);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new CommentQueryService(null))
                .isInstanceOf(NullPointerException.class);
    }
}
