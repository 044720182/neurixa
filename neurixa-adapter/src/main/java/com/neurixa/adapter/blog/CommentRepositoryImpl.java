package com.neurixa.adapter.blog;

import com.neurixa.domain.blog.Comment;
import com.neurixa.domain.blog.CommentId;
import com.neurixa.domain.blog.CommentRepository;
import com.neurixa.domain.blog.CommentStatus;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private final MongoTemplate mongoTemplate;
    private final CommentMapper commentMapper;

    public CommentRepositoryImpl(MongoTemplate mongoTemplate, CommentMapper commentMapper) {
        this.mongoTemplate = mongoTemplate;
        this.commentMapper = commentMapper;
    }

    @Override
    public void save(Comment comment) {
        mongoTemplate.save(commentMapper.toDocument(comment));
    }

    @Override
    public Optional<Comment> findById(CommentId id) {
        CommentDocument doc = mongoTemplate.findById(id.getValue(), CommentDocument.class);
        return Optional.ofNullable(doc).map(commentMapper::toDomain);
    }

    @Override
    public List<Comment> findByArticleIdAndStatus(java.util.UUID articleId, CommentStatus status) {
        Query query = new Query();
        query.addCriteria(Criteria.where("articleId").is(articleId));
        query.addCriteria(Criteria.where("status").is(status));
        query.addCriteria(Criteria.where("deleted").ne(true));
        return mongoTemplate.find(query, CommentDocument.class).stream().map(commentMapper::toDomain).toList();
    }
}
