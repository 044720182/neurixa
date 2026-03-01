package com.neurixa.adapter.blog;

import com.neurixa.domain.blog.Category;
import com.neurixa.domain.blog.CategoryId;
import com.neurixa.domain.blog.Slug;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDocument toDocument(Category category) {
        CategoryDocument doc = new CategoryDocument();
        doc.setId(category.getId().getValue());
        doc.setName(category.getName());
        doc.setSlug(category.getSlug().getValue());
        doc.setParentId(category.getParentId() != null ? category.getParentId().getValue() : null);
        doc.setDeleted(category.isDeleted());
        doc.setDeletedAt(category.getDeletedAt());
        doc.setCreatedAt(category.getCreatedAt());
        doc.setUpdatedAt(category.getUpdatedAt());
        return doc;
    }

    public Category toDomain(CategoryDocument doc) {
        return Category.fromState(
                new CategoryId(doc.getId()),
                doc.getName(),
                new Slug(doc.getSlug()),
                doc.getParentId() != null ? new CategoryId(doc.getParentId()) : null,
                doc.isDeleted(),
                doc.getDeletedAt(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }
}
