package com.neurixa.adapter.files.persistence;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.Folder;
import com.neurixa.core.files.domain.FolderId;
import com.neurixa.core.files.port.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoFolderRepository implements FolderRepository {
    private final FolderMongoRepository mongoRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public Folder save(Folder folder) {
        FolderDocument doc = toDocument(folder);
        FolderDocument saved = mongoRepository.save(doc);
        return toDomain(saved);
    }

    @Override
    public Optional<Folder> findByIdAndOwner(FolderId id, UserId ownerId) {
        return mongoRepository.findById(id.getValue())
                .filter(d -> d.getOwnerId().equals(ownerId.getValue()))
                .map(this::toDomain);
    }

    @Override
    public List<Folder> findChildren(UserId ownerId, FolderId parentId) {
        List<FolderDocument> docs = mongoRepository.findByOwnerIdAndParentId(ownerId.getValue(), parentId.getValue());
        return docs.stream().map(this::toDomain).toList();
    }

    @Override
    public List<Folder> findRoots(UserId ownerId) {
        List<FolderDocument> docs = mongoRepository.findByOwnerIdAndParentIdIsNull(ownerId.getValue());
        return docs.stream().map(this::toDomain).toList();
    }

    @Override
    public List<Folder> findChildren(UserId ownerId, FolderId parentId, int page, int size) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(ownerId.getValue()));
        query.addCriteria(Criteria.where("parentId").is(parentId.getValue()));
        query.skip((long) Math.max(page, 0) * Math.max(size, 1));
        query.limit(Math.max(size, 1));
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("updatedAt")));
        return mongoTemplate.find(query, FolderDocument.class).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Folder> findRoots(UserId ownerId, int page, int size) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(ownerId.getValue()));
        query.addCriteria(Criteria.where("parentId").isNull());
        query.skip((long) Math.max(page, 0) * Math.max(size, 1));
        query.limit(Math.max(size, 1));
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("updatedAt")));
        return mongoTemplate.find(query, FolderDocument.class).stream().map(this::toDomain).toList();
    }

    @Override
    public long countChildren(UserId ownerId, FolderId parentId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(ownerId.getValue()));
        query.addCriteria(Criteria.where("parentId").is(parentId.getValue()));
        return mongoTemplate.count(query, FolderDocument.class);
    }

    @Override
    public long countRoots(UserId ownerId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(ownerId.getValue()));
        query.addCriteria(Criteria.where("parentId").isNull());
        return mongoTemplate.count(query, FolderDocument.class);
    }

    private FolderDocument toDocument(Folder folder) {
        return FolderDocument.builder()
                .id(folder.getId().getValue())
                .ownerId(folder.getOwnerId().getValue())
                .name(folder.getName())
                .parentId(folder.getParentId() != null ? folder.getParentId().getValue() : null)
                .path(folder.getPath())
                .deleted(folder.isDeleted())
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }

    private Folder toDomain(FolderDocument d) {
        return Folder.from(
                new FolderId(d.getId()),
                new UserId(d.getOwnerId()),
                d.getName(),
                d.getParentId() != null ? new FolderId(d.getParentId()) : null,
                d.getPath(),
                d.isDeleted(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}
