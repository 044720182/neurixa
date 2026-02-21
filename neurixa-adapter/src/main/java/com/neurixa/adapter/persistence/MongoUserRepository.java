package com.neurixa.adapter.persistence;

import com.neurixa.core.domain.User;
import com.neurixa.core.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoUserRepository implements UserRepository {
    private final UserMongoRepository mongoRepository;

    @Override
    public User save(User user) {
        UserDocument document = toDocument(user);
        UserDocument saved = mongoRepository.save(document);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(String id) {
        return mongoRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return mongoRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return mongoRepository.findByEmail(email).map(this::toDomain);
    }

    private UserDocument toDocument(User user) {
        return UserDocument.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .role(user.getRole())
                .build();
    }

    private User toDomain(UserDocument document) {
        return new User(
                document.getId(),
                document.getUsername(),
                document.getEmail(),
                document.getPassword(),
                document.getRole()
        );
    }
}
