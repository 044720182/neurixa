package com.neurixa.boot.blog;

import com.neurixa.application.blog.ArticleCommandService;
import com.neurixa.application.blog.ArticleQueryService;
import com.neurixa.boot.dto.response.BlogArticleResponse;
import com.neurixa.dto.response.PageResponse;
import com.neurixa.domain.blog.Article;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/blog/articles")
public class BlogArticleController {

    private final ArticleCommandService articleCommandService;
    private final ArticleQueryService articleQueryService;

    public BlogArticleController(ArticleCommandService articleCommandService,
                                 ArticleQueryService articleQueryService) {
        this.articleCommandService = articleCommandService;
        this.articleQueryService = articleQueryService;
    }

    @PostMapping
    public BlogArticleResponse createArticle(@RequestBody CreateArticleRequest request) {
        Article article = articleCommandService.createDraft(request.title(), request.content(), request.excerpt());
        return BlogArticleResponse.from(article);
    }

    @PutMapping("/{id}")
    public BlogArticleResponse updateArticle(@PathVariable UUID id, @RequestBody UpdateArticleRequest request) {
        Article article = articleCommandService.update(id, request.title(), request.content(), request.excerpt());
        return BlogArticleResponse.from(article);
    }

    @DeleteMapping("/{id}")
    public void deleteArticle(@PathVariable UUID id) {
        articleCommandService.delete(id);
    }

    @PostMapping("/{id}/publish")
    public BlogArticleResponse publishArticle(@PathVariable UUID id) {
        Article article = articleCommandService.publish(id);
        return BlogArticleResponse.from(article);
    }

    @PostMapping("/{id}/restore")
    public BlogArticleResponse restoreArticle(@PathVariable UUID id) {
        Article article = articleCommandService.restore(id);
        return BlogArticleResponse.from(article);
    }

    @GetMapping("/{slug}")
    public BlogArticleResponse getArticle(@PathVariable String slug) {
        Article article = articleQueryService.getBySlug(slug);
        return BlogArticleResponse.from(article);
    }

    @GetMapping
    public PageResponse<BlogArticleResponse> listArticles(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        List<BlogArticleResponse> items = articleQueryService.listPublished(page, size).stream()
                .map(BlogArticleResponse::from)
                .toList();
        long total = articleQueryService.countPublished();
        int totalPages = (int) Math.ceil((double) total / Math.max(size, 1));
        boolean hasPrevious = page > 0;
        boolean hasNext = page < totalPages - 1;
        return new PageResponse<>(items, page, size, total, totalPages, hasNext, hasPrevious);
    }

    public record CreateArticleRequest(String title, String content, String excerpt) {}
    public record UpdateArticleRequest(String title, String content, String excerpt) {}
}
