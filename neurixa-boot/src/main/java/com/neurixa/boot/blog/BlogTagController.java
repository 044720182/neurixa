package com.neurixa.boot.blog;

import com.neurixa.application.blog.TagCommandService;
import com.neurixa.boot.dto.response.BlogTagResponse;
import com.neurixa.domain.blog.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog/tags")
public class BlogTagController {

    private final TagCommandService tagCommandService;

    public BlogTagController(TagCommandService tagCommandService) {
        this.tagCommandService = tagCommandService;
    }

    @PostMapping
    public BlogTagResponse createTag(@RequestBody CreateTagRequest request) {
        Tag tag = tagCommandService.create(request.name());
        return BlogTagResponse.from(tag);
    }

    public record CreateTagRequest(String name) {}
}
