package com.neurixa.application.blog;

import com.neurixa.domain.blog.Comment;
import com.neurixa.domain.blog.CommentId;
import com.neurixa.domain.blog.CommentRepository;
import com.neurixa.domain.blog.CommentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentCommandServiceTest {

    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    CommentCommandService service;

    // ── add ───────────────────────────────────────────────────────────────────

    @Test
    void shouldAddComment() {
        UUID articleId = UUID.randomUUID();

        Comment result = service.add(articleId, "Alice", "alice@example.com", "Great post!", null);

        assertThat(result.getAuthorName()).isEqualTo("Alice");
        assertThat(result.getStatus()).isEqualTo(CommentStatus.PENDING);
        verify(commentRepository).save(argThat(c -> c.getStatus() == CommentStatus.PENDING));
    }

    @Test
    void shouldThrowWhenAddingCommentWithBlankAuthorName() {
        UUID articleId = UUID.randomUUID();

        assertThatThrownBy(() -> service.add(articleId, "", "alice@example.com", "Content", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Author name cannot be empty");
    }

    @Test
    void shouldThrowWhenAddingCommentWithBlankContent() {
        UUID articleId = UUID.randomUUID();

        assertThatThrownBy(() -> service.add(articleId, "Alice", "alice@example.com", "", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment content cannot be empty");
    }

    @Test
    void shouldAddReplyComment() {
        UUID articleId = UUID.randomUUID();
        UUID replyToId = UUID.randomUUID();

        Comment result = service.add(articleId, "Bob", "bob@example.com", "I agree!", replyToId);

        assertThat(result.getReplyTo()).isNotNull();
        assertThat(result.getReplyTo().getValue()).isEqualTo(replyToId);
    }

    // ── approve ───────────────────────────────────────────────────────────────

    @Test
    void shouldApproveComment() {
        Comment comment = Comment.create(UUID.randomUUID(), "Alice", "alice@example.com", "Great!", null);
        UUID id = comment.getCommentId().getValue();
        when(commentRepository.findById(new CommentId(id))).thenReturn(Optional.of(comment));

        Comment result = service.approve(id);

        assertThat(result.getStatus()).isEqualTo(CommentStatus.APPROVED);
        verify(commentRepository).save(argThat(c -> c.getStatus() == CommentStatus.APPROVED));
    }

    @Test
    void shouldThrowWhenApprovingNonExistentComment() {
        UUID id = UUID.randomUUID();
        when(commentRepository.findById(new CommentId(id))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approve(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment not found");
    }

    @Test
    void shouldThrowWhenApprovingAlreadyApprovedComment() {
        Comment comment = Comment.create(UUID.randomUUID(), "Alice", "alice@example.com", "Great!", null);
        comment.approve();
        UUID id = comment.getCommentId().getValue();
        when(commentRepository.findById(new CommentId(id))).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> service.approve(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only a pending comment can be approved");
    }

    // ── reject ────────────────────────────────────────────────────────────────

    @Test
    void shouldRejectComment() {
        Comment comment = Comment.create(UUID.randomUUID(), "Alice", "alice@example.com", "Spam!", null);
        UUID id = comment.getCommentId().getValue();
        when(commentRepository.findById(new CommentId(id))).thenReturn(Optional.of(comment));

        Comment result = service.reject(id);

        assertThat(result.getStatus()).isEqualTo(CommentStatus.REJECTED);
        verify(commentRepository).save(argThat(c -> c.getStatus() == CommentStatus.REJECTED));
    }

    @Test
    void shouldThrowWhenRejectingNonExistentComment() {
        UUID id = UUID.randomUUID();
        when(commentRepository.findById(new CommentId(id))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reject(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment not found");
    }

    @Test
    void shouldThrowWhenRejectingAlreadyRejectedComment() {
        Comment comment = Comment.create(UUID.randomUUID(), "Alice", "alice@example.com", "Spam!", null);
        comment.reject();
        UUID id = comment.getCommentId().getValue();
        when(commentRepository.findById(new CommentId(id))).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> service.reject(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only a pending comment can be rejected");
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void shouldSoftDeleteComment() {
        Comment comment = Comment.create(UUID.randomUUID(), "Alice", "alice@example.com", "Content", null);
        UUID id = comment.getCommentId().getValue();
        when(commentRepository.findById(new CommentId(id))).thenReturn(Optional.of(comment));

        service.delete(id);

        verify(commentRepository).save(argThat(c -> c.isDeleted() && c.getStatus() == CommentStatus.DELETED));
    }

    @Test
    void shouldThrowWhenDeletingNonExistentComment() {
        UUID id = UUID.randomUUID();
        when(commentRepository.findById(new CommentId(id))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment not found");
    }

    @Test
    void shouldThrowWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new CommentCommandService(null))
                .isInstanceOf(NullPointerException.class);
    }
}
