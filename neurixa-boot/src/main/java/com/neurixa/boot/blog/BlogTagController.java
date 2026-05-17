package com.neurixa.boot.blog;

import com.neurixa.application.blog.TagCommandService;
import com.neurixa.boot.dto.response.BlogTagResponse;
import com.neurixa.domain.blog.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/blog/tags")
public class BlogTagController {

    private final TagCommandService tagCommandService;

    public BlogTagController(TagCommandService tagCommandService) {
        this.tagCommandService = tagCommandService;
    }

    @PostMapping
    public BlogTagResponse createTag(@Valid @RequestBody CreateTagRequest request) {
        Tag tag = tagCommandService.create(request.name());
        return BlogTagResponse.from(tag);
    }

    public record CreateTagRequest(
            @NotBlank(message = "Tag name is required")
            @Size(min = 1, max = 50, message = "Tag name must be between 1 and 50 characters")
            String name
    ) {}
}
