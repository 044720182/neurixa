package com.neurixa.boot.blog;

import com.neurixa.application.blog.CommentCommandService;
import com.neurixa.boot.dto.response.BlogCommentResponse;
import com.neurixa.domain.blog.Comment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/blog/comments")
public class BlogCommentController {

    private final CommentCommandService commentCommandService;

    public BlogCommentController(CommentCommandService commentCommandService) {
        this.commentCommandService = commentCommandService;
    }

    @PostMapping
    public BlogCommentResponse addComment(@RequestBody AddCommentRequest request) {
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

    public record AddCommentRequest(UUID articleId, String authorName, String authorEmail, String content, UUID replyTo) {}
}
