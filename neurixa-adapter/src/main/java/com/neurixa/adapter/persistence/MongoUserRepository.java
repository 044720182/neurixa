package com.neurixa.adapter.persistence;

import com.neurixa.core.domain.User;
import com.neurixa.core.domain.UserId;
import com.neurixa.core.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    public Optional<User> findById(UserId id) {
        return mongoRepository.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return mongoRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return mongoRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return mongoRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(UserId id) {
        mongoRepository.deleteById(id.getValue());
    }

    private UserDocument toDocument(User user) {
        return UserDocument.builder()
                .id(user.getId().getValue())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .role(user.getRole())
                .locked(user.isLocked())
                .emailVerified(user.isEmailVerified())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private User toDomain(UserDocument document) {
        return User.from(
                new UserId(document.getId()),
                document.getUsername(),
                document.getEmail(),
                document.getPassword(),
                document.getRole(),
                document.isLocked(),
                document.isEmailVerified(),
                document.getFailedLoginAttempts(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
