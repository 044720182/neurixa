package com.neurixa.adapter.blog;

import com.neurixa.domain.blog.Article;
import com.neurixa.domain.blog.ArticleId;
import com.neurixa.domain.blog.ArticleRepository;
import com.neurixa.domain.blog.ArticleStatus;
import com.neurixa.domain.blog.Slug;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ArticleRepositoryImpl implements ArticleRepository {

    private final MongoTemplate mongoTemplate;
    private final ArticleMapper articleMapper;

    public ArticleRepositoryImpl(MongoTemplate mongoTemplate, ArticleMapper articleMapper) {
        this.mongoTemplate = mongoTemplate;
        this.articleMapper = articleMapper;
    }

    @Override
    public void save(Article article) {
        mongoTemplate.save(articleMapper.toDocument(article));
    }

    @Override
    public Optional<Article> findById(ArticleId id) {
        ArticleDocument doc = mongoTemplate.findById(id.getValue(), ArticleDocument.class);
        return Optional.ofNullable(doc).map(articleMapper::toDomain);
    }

    @Override
    public Optional<Article> findBySlug(Slug slug) {
        Query query = new Query();
        query.addCriteria(Criteria.where("slug").is(slug.getValue()));
        query.addCriteria(Criteria.where("deleted").ne(true));
        ArticleDocument doc = mongoTemplate.findOne(query, ArticleDocument.class);
        return Optional.ofNullable(doc).map(articleMapper::toDomain);
    }

    @Override
    public void incrementViewCountAtomic(ArticleId id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id.getValue()));
        Update update = new Update().inc("viewCount", 1);
        mongoTemplate.updateFirst(query, update, ArticleDocument.class);
    }

    @Override
    public List<Article> findPublished(int page, int size) {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(ArticleStatus.PUBLISHED));
        query.addCriteria(Criteria.where("deleted").ne(true));
        query.skip((long) Math.max(page, 0) * Math.max(size, 1));
        query.limit(Math.max(size, 1));
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("publishedAt")));
        return mongoTemplate.find(query, ArticleDocument.class).stream().map(articleMapper::toDomain).toList();
    }

    @Override
    public long countPublished() {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(ArticleStatus.PUBLISHED));
        query.addCriteria(Criteria.where("deleted").ne(true));
        return mongoTemplate.count(query, ArticleDocument.class);
    }
}
