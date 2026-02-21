package com.neurixa.adapter.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserMongoRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByUsername(String username);
    Optional<UserDocument> findByEmail(String email);
}
