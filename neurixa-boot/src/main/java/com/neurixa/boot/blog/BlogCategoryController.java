package com.neurixa.boot.blog;

import com.neurixa.application.blog.CategoryCommandService;
import com.neurixa.boot.dto.response.BlogCategoryResponse;
import com.neurixa.domain.blog.Category;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/blog/categories")
public class BlogCategoryController {

    private final CategoryCommandService categoryCommandService;

    public BlogCategoryController(CategoryCommandService categoryCommandService) {
        this.categoryCommandService = categoryCommandService;
    }

    @PostMapping
    public BlogCategoryResponse createCategory(@RequestBody CreateCategoryRequest request) {
        Category category = categoryCommandService.create(request.name(), request.parentId());
        return BlogCategoryResponse.from(category);
    }

    public record CreateCategoryRequest(String name, UUID parentId) {}
}
