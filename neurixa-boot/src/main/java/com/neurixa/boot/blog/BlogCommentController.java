package com.neurixa.boot.blog;

import com.neurixa.application.blog.CommentCommandService;
import com.neurixa.boot.dto.response.BlogCommentResponse;
import com.neurixa.domain.blog.Comment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/blog/comments")
public class BlogCommentController {

    private final CommentCommandService commentCommandService;

    public BlogCommentController(CommentCommandService commentCommandService) {
        this.commentCommandService = commentCommandService;
    }

    @PostMapping
    public BlogCommentResponse addComment(@Valid @RequestBody AddCommentRequest request) {
        Comment comment = commentCommandService.add(request.articleId(), request.authorName(), request.authorEmail(), request.content(), request.replyTo());
        return BlogCommentResponse.from(comment);
    }

    @PostMapping("/{id}/approve")
    public BlogCommentResponse approveComment(@PathVariable UUID id) {
        Comment comment = commentCommandService.approve(id);
        return BlogCommentResponse.from(comment);
    }

    @PostMapping("/{id}/reject")
    public BlogCommentResponse rejectComment(@PathVariable UUID id) {
        Comment comment = commentCommandService.reject(id);
        return BlogCommentResponse.from(comment);
    }

    public record AddCommentRequest(
            @NotNull(message = "Article ID is required")
            UUID articleId,

            @NotBlank(message = "Author name is required")
            @Size(min = 1, max = 100, message = "Author name must be between 1 and 100 characters")
            String authorName,

            @NotBlank(message = "Author email is required")
            @Email(message = "Author email must be valid")
            @Size(max = 255, message = "Author email must not exceed 255 characters")
            String authorEmail,

            @NotBlank(message = "Comment content is required")
            @Size(min = 1, max = 5000, message = "Comment must be between 1 and 5,000 characters")
            String content,

            UUID replyTo
    ) {}
}
