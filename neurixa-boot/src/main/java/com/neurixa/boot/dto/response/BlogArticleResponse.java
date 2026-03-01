package com.neurixa.boot.dto.response;

import com.neurixa.domain.blog.Article;
import com.neurixa.domain.blog.ArticleStatus;
import com.neurixa.domain.blog.CategoryId;
import com.neurixa.domain.blog.TagId;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class BlogArticleResponse {
    public UUID id;
    public String title;
    public String slug;
    public String content;
    public String excerpt;
    public UUID featuredImageId;
    public ArticleStatus status;
    public Instant createdAt;
    public Instant updatedAt;
    public Instant publishedAt;
    public int viewCount;
    public String metaTitle;
    public String metaDescription;
    public Set<UUID> categoryIds;
    public Set<UUID> tagIds;

    public static BlogArticleResponse from(Article article) {
        BlogArticleResponse r = new BlogArticleResponse();
        r.id = article.getArticleId().getValue();
        r.title = article.getTitle();
        r.slug = article.getSlug().getValue();
        r.content = article.getContent();
        r.excerpt = article.getExcerpt();
        r.featuredImageId = article.getFeaturedImageId();
        r.status = article.getStatus();
        r.createdAt = article.getCreatedAt();
        r.updatedAt = article.getUpdatedAt();
        r.publishedAt = article.getPublishedAt();
        r.viewCount = article.getViewCount();
        r.metaTitle = article.getMetaTitle();
        r.metaDescription = article.getMetaDescription();
        r.categoryIds = article.getCategories().stream().map(CategoryId::getValue).collect(Collectors.toSet());
        r.tagIds = article.getTags().stream().map(TagId::getValue).collect(Collectors.toSet());
        return r;
    }
}
