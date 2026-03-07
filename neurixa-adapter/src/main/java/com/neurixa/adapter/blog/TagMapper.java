package com.neurixa.adapter.blog;

import com.neurixa.domain.blog.Tag;
import com.neurixa.domain.blog.TagId;
import com.neurixa.domain.blog.Slug;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagDocument toDocument(Tag tag) {
        TagDocument doc = new TagDocument();
        doc.setId(tag.getId().getValue());
        doc.setName(tag.getName());
        doc.setSlug(tag.getSlug().getValue());
        doc.setDeleted(tag.isDeleted());
        doc.setDeletedAt(tag.getDeletedAt());
        doc.setCreatedAt(tag.getCreatedAt());
        doc.setUpdatedAt(tag.getUpdatedAt());
        return doc;
    }

    public Tag toDomain(TagDocument doc) {
        return Tag.fromState(
                new TagId(doc.getId()),
                doc.getName(),
                new Slug(doc.getSlug()),
                doc.isDeleted(),
                doc.getDeletedAt(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }
}
