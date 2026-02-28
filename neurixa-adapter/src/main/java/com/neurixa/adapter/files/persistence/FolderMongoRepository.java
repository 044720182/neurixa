package com.neurixa.adapter.files.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FolderMongoRepository extends MongoRepository<FolderDocument, String> {
    List<FolderDocument> findByOwnerIdAndParentId(String ownerId, String parentId);
    List<FolderDocument> findByOwnerIdAndParentIdIsNull(String ownerId);
}

