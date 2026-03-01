package com.neurixa.domain.blog;

import com.neurixa.domain.blog.event.ArticlePublishedEvent;
import com.neurixa.domain.blog.shared.BaseAggregateRoot;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Article extends BaseAggregateRoot<ArticleId> {

    private final ArticleId id;
    private String title;
    private Slug slug;
    private String content;
    private String excerpt;
    private UUID featuredImageId;
    private ArticleStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant publishedAt;
    private int viewCount;
    private String metaTitle;
    private String metaDescription;
    private final Set<CategoryId> categories = new HashSet<>();
    private final Set<TagId> tags = new HashSet<>();
    private boolean deleted;
    private Instant deletedAt;

    private Article(ArticleId id, String title, String content, String excerpt, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.setTitle(title);
        this.content = content;
        this.excerpt = excerpt;
        this.status = ArticleStatus.DRAFT;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = this.createdAt;
        this.deleted = false;
    }

    public static Article createDraft(String title, String content, String excerpt) {
        return new Article(ArticleId.generate(), title, content, excerpt, Instant.now());
    }

    public static Article fromState(ArticleId id,
                                    String title,
                                    String content,
                                    String excerpt,
                                    Slug slug,
                                    UUID featuredImageId,
                                    ArticleStatus status,
                                    Instant createdAt,
                                    Instant updatedAt,
                                    Instant publishedAt,
                                    int viewCount,
                                    String metaTitle,
                                    String metaDescription,
                                    Set<CategoryId> categories,
                                    Set<TagId> tags,
                                    boolean deleted,
                                    Instant deletedAt) {
        Article article = new Article(id, title, content, excerpt, createdAt);
        article.slug = Objects.requireNonNull(slug);
        article.featuredImageId = featuredImageId;
        article.status = Objects.requireNonNull(status);
        article.updatedAt = updatedAt != null ? updatedAt : createdAt;
        article.publishedAt = publishedAt;
        article.viewCount = viewCount;
        article.metaTitle = metaTitle;
        article.metaDescription = metaDescription;
        if (categories != null) {
            article.categories.addAll(categories);
        }
        if (tags != null) {
            article.tags.addAll(tags);
        }
        article.deleted = deleted;
        article.deletedAt = deletedAt;
        return article;
    }

    public void update(String title, String content, String excerpt) {
        if (this.deleted || this.status == ArticleStatus.DELETED) {
            throw new IllegalStateException("Cannot update a deleted article.");
        }
        this.setTitle(title);
        this.content = content;
        this.excerpt = excerpt;
        this.updatedAt = Instant.now();
    }

    public void updateContent(String content, String excerpt) {
        if (this.deleted || this.status == ArticleStatus.DELETED) {
            throw new IllegalStateException("Cannot update a deleted article.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be empty.");
        }
        this.content = content;
        this.excerpt = excerpt;
        this.updatedAt = Instant.now();
    }

    public void publish() {
        if (this.deleted || this.status == ArticleStatus.DELETED) {
            throw new IllegalStateException("Cannot publish a deleted article.");
        }
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            throw new IllegalStateException("Article must have a title and content to be published.");
        }
        this.status = ArticleStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.updatedAt = this.publishedAt;
        registerEvent(new ArticlePublishedEvent(this.id));
    }

    public void unpublish() {
        if (this.status != ArticleStatus.PUBLISHED) {
            throw new IllegalStateException("Only a published article can be unpublished.");
        }
        this.status = ArticleStatus.DRAFT;
        this.publishedAt = null;
        this.updatedAt = Instant.now();
    }

    public void archive() {
        if (this.status != ArticleStatus.PUBLISHED) {
            throw new IllegalStateException("Only a published article can be archived.");
        }
        this.status = ArticleStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        if (this.status == ArticleStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot delete a published article directly (must archive first).");
        }
        if (this.status == ArticleStatus.DELETED || this.deleted) {
            return;
        }
        this.status = ArticleStatus.DELETED;
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.updatedAt = this.deletedAt;
    }

    public void restore() {
        if (!this.deleted && this.status != ArticleStatus.DELETED) {
            throw new IllegalStateException("Only a deleted article can be restored.");
        }
        this.status = ArticleStatus.DRAFT;
        this.deleted = false;
        this.deletedAt = null;
        this.updatedAt = Instant.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void updateSeo(String metaTitle, String metaDescription) {
        this.metaTitle = metaTitle;
        this.metaDescription = metaDescription;
        this.updatedAt = Instant.now();
    }

    public void changeFeaturedImage(UUID featuredImageId) {
        this.featuredImageId = featuredImageId;
        this.updatedAt = Instant.now();
    }

    public void assignCategory(CategoryId categoryId) {
        this.categories.add(Objects.requireNonNull(categoryId));
        this.updatedAt = Instant.now();
    }

    public void removeCategory(CategoryId categoryId) {
        this.categories.remove(Objects.requireNonNull(categoryId));
        this.updatedAt = Instant.now();
    }

    public void assignTag(TagId tagId) {
        this.tags.add(Objects.requireNonNull(tagId));
        this.updatedAt = Instant.now();
    }

    public void removeTag(TagId tagId) {
        this.tags.remove(Objects.requireNonNull(tagId));
        this.updatedAt = Instant.now();
    }

    private void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        this.title = title;
        this.slug = new Slug(title);
    }

    @Override
    protected ArticleId getId() {
        return id;
    }

    public ArticleId getArticleId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Slug getSlug() {
        return slug;
    }

    public String getContent() {
        return content;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public UUID getFeaturedImageId() {
        return featuredImageId;
    }

    public ArticleStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public int getViewCount() {
        return viewCount;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public Set<CategoryId> getCategories() {
        return Collections.unmodifiableSet(categories);
    }

    public Set<TagId> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
