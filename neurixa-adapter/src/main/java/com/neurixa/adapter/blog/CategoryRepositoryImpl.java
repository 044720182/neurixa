package com.neurixa.adapter.blog;

import com.neurixa.domain.blog.Category;
import com.neurixa.domain.blog.CategoryId;
import com.neurixa.domain.blog.CategoryRepository;
import com.neurixa.domain.blog.Slug;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {

    private final MongoTemplate mongoTemplate;
    private final CategoryMapper categoryMapper;

    public CategoryRepositoryImpl(MongoTemplate mongoTemplate, CategoryMapper categoryMapper) {
        this.mongoTemplate = mongoTemplate;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public void save(Category category) {
        mongoTemplate.save(categoryMapper.toDocument(category));
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        CategoryDocument doc = mongoTemplate.findById(id.getValue(), CategoryDocument.class);
        return Optional.ofNullable(doc).map(categoryMapper::toDomain);
    }

    @Override
    public Optional<Category> findBySlug(Slug slug) {
        Query query = new Query();
        query.addCriteria(Criteria.where("slug").is(slug.getValue()));
        query.addCriteria(Criteria.where("deleted").ne(true));
        CategoryDocument doc = mongoTemplate.findOne(query, CategoryDocument.class);
        return Optional.ofNullable(doc).map(categoryMapper::toDomain);
    }
}
