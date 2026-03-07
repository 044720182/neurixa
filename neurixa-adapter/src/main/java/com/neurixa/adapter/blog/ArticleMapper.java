package com.neurixa.adapter.blog;

import com.neurixa.domain.blog.Article;
import com.neurixa.domain.blog.ArticleId;
import com.neurixa.domain.blog.Category;
import com.neurixa.domain.blog.CategoryId;
import com.neurixa.domain.blog.Slug;
import com.neurixa.domain.blog.Tag;
import com.neurixa.domain.blog.TagId;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class ArticleMapper {

    public ArticleDocument toDocument(Article article) {
        ArticleDocument doc = new ArticleDocument();
        doc.setId(article.getArticleId().getValue());
        doc.setTitle(article.getTitle());
        doc.setSlug(article.getSlug().getValue());
        doc.setContent(article.getContent());
        doc.setExcerpt(article.getExcerpt());
        doc.setFeaturedImageId(article.getFeaturedImageId());
        doc.setStatus(article.getStatus());
        doc.setCreatedAt(article.getCreatedAt());
        doc.setUpdatedAt(article.getUpdatedAt());
        doc.setPublishedAt(article.getPublishedAt());
        doc.setViewCount(article.getViewCount());
        doc.setMetaTitle(article.getMetaTitle());
        doc.setMetaDescription(article.getMetaDescription());
        doc.setCategoryIds(article.getCategories().stream().map(CategoryId::getValue).collect(Collectors.toSet()));
        doc.setTagIds(article.getTags().stream().map(TagId::getValue).collect(Collectors.toSet()));
        doc.setDeleted(article.isDeleted());
        doc.setDeletedAt(article.getDeletedAt());
        return doc;
    }

    public Article toDomain(ArticleDocument doc) {
        return Article.fromState(
                new ArticleId(doc.getId()),
                doc.getTitle(),
                doc.getContent(),
                doc.getExcerpt(),
                new Slug(doc.getSlug()),
                doc.getFeaturedImageId(),
                doc.getStatus(),
                doc.getCreatedAt(),
                doc.getUpdatedAt(),
                doc.getPublishedAt(),
                doc.getViewCount(),
                doc.getMetaTitle(),
                doc.getMetaDescription(),
                doc.getCategoryIds() != null ? doc.getCategoryIds().stream().map(CategoryId::new).collect(Collectors.toSet()) : new HashSet<>(),
                doc.getTagIds() != null ? doc.getTagIds().stream().map(TagId::new).collect(Collectors.toSet()) : new HashSet<>(),
                doc.isDeleted(),
                doc.getDeletedAt()
        );
    }
}
