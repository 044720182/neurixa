package com.neurixa.application.blog;

import com.neurixa.domain.blog.Comment;
import com.neurixa.domain.blog.CommentRepository;
import com.neurixa.domain.blog.CommentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CommentQueryServiceTest {

    private CommentRepository commentRepository;
    private CommentQueryService service;

    @BeforeEach
    void setUp() {
        commentRepository = mock(CommentRepository.class);
        service = new CommentQueryService(commentRepository);
    }

    @Test
    void shouldListPendingCommentsByArticle() {
        UUID articleId = UUID.randomUUID();
        Comment c1 = Comment.create(articleId, "Alice", "alice@example.com", "Nice!", null);
        Comment c2 = Comment.create(articleId, "Bob", "bob@example.com", "Thanks!", null);
        when(commentRepository.findByArticleIdAndStatus(articleId, CommentStatus.PENDING))
                .thenReturn(List.of(c1, c2));

        List<Comment> result = service.listByArticleAndStatus(articleId, CommentStatus.PENDING);

        assertThat(result).hasSize(2);
        verify(commentRepository).findByArticleIdAndStatus(articleId, CommentStatus.PENDING);
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
