package com.neurixa.core.port;

import com.neurixa.core.domain.User;
import com.neurixa.core.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void deleteById(UserId id);
}
