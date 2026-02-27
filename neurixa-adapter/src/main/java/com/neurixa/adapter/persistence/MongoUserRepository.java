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
    private final org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

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

    @Override
    public com.neurixa.core.domain.Page<User> findAllWithFilters(
            String search,
            String role,
            Boolean locked,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        org.springframework.data.domain.Sort.Direction direction =
            "desc".equalsIgnoreCase(sortDirection)
                ? org.springframework.data.domain.Sort.Direction.DESC
                : org.springframework.data.domain.Sort.Direction.ASC;

        org.springframework.data.domain.Pageable pageable =
            org.springframework.data.domain.PageRequest.of(page, size, direction, sortBy);

        org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query();

        if (search != null && !search.isBlank()) {
            org.springframework.data.mongodb.core.query.Criteria searchCriteria =
                new org.springframework.data.mongodb.core.query.Criteria().orOperator(
                    org.springframework.data.mongodb.core.query.Criteria.where("username").regex(search, "i"),
                    org.springframework.data.mongodb.core.query.Criteria.where("email").regex(search, "i")
                );
            query.addCriteria(searchCriteria);
        }

        if (role != null && !role.isBlank()) {
            query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("role").is(role));
        }

        if (locked != null) {
            query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("locked").is(locked));
        }

        long total = mongoTemplate.count(query, UserDocument.class);

        query.with(pageable);
        List<UserDocument> documents = mongoTemplate.find(query, UserDocument.class);
        List<User> users = documents.stream().map(this::toDomain).toList();

        return new com.neurixa.core.domain.Page<>(users, page, size, total);
    }
}
