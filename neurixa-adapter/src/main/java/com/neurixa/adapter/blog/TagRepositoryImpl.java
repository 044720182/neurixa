package com.neurixa.adapter.blog;

import com.neurixa.domain.blog.Tag;
import com.neurixa.domain.blog.TagId;
import com.neurixa.domain.blog.TagRepository;
import com.neurixa.domain.blog.Slug;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TagRepositoryImpl implements TagRepository {

    private final MongoTemplate mongoTemplate;
    private final TagMapper tagMapper;

    public TagRepositoryImpl(MongoTemplate mongoTemplate, TagMapper tagMapper) {
        this.mongoTemplate = mongoTemplate;
        this.tagMapper = tagMapper;
    }

    @Override
    public void save(Tag tag) {
        mongoTemplate.save(tagMapper.toDocument(tag));
    }

    @Override
    public Optional<Tag> findById(TagId id) {
        TagDocument doc = mongoTemplate.findById(id.getValue(), TagDocument.class);
        return Optional.ofNullable(doc).map(tagMapper::toDomain);
    }

    @Override
    public Optional<Tag> findBySlug(Slug slug) {
        Query query = new Query();
        query.addCriteria(Criteria.where("slug").is(slug.getValue()));
        query.addCriteria(Criteria.where("deleted").ne(true));
        TagDocument doc = mongoTemplate.findOne(query, TagDocument.class);
        return Optional.ofNullable(doc).map(tagMapper::toDomain);
    }
}
