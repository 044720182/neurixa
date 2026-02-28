package com.neurixa.adapter.files.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileMongoRepository extends MongoRepository<FileDocument, String> {
    List<FileDocument> findByOwnerIdAndFolderId(String ownerId, String folderId);
    List<FileDocument> findByOwnerIdAndFolderIdIsNull(String ownerId);
}

