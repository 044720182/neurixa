package com.neurixa.adapter.files.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileVersionMongoRepository extends MongoRepository<FileVersionDocument, String> {
    List<FileVersionDocument> findByFileId(String fileId);
}

