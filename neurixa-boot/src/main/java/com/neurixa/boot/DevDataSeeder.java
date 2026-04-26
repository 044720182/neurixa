package com.neurixa.boot;

import com.neurixa.core.domain.Role;
import com.neurixa.core.domain.User;
import com.neurixa.core.port.PasswordEncoder;
import com.neurixa.core.port.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * DevDataSeeder — hanya aktif di profile "dev".
 *
 * Seed users default untuk keperluan development & testing:
 *
 *   Role        | Username    | Password      | Email
 *   ------------|-------------|---------------|---------------------
 *   SUPER_ADMIN | superadmin  | superadmin123 | superadmin@dev.local
 *   ADMIN       | admin       | admin123      | admin@dev.local
 *   USER        | user        | user123       | user@dev.local
 *
 * Konfigurasi di application-dev.yml:
 *   neurixa.seed.enabled        = true   → jalankan seeder saat startup
 *   neurixa.seed.reset-on-start = false  → true = hapus semua users dulu
 */
@Component
@Profile("dev")
public class DevDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${neurixa.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${neurixa.seed.reset-on-start:false}")
    private boolean resetOnStart;

    public DevDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("[DevSeeder] Seeder disabled. Skipping.");
            return;
        }

        if (resetOnStart) {
            userRepository.findAll().forEach(u -> userRepository.deleteById(u.getId()));
            log.warn("[DevSeeder] All users deleted (reset-on-start=true).");
        }

        seedUser("superadmin", "superadmin@dev.local", "superadmin123", Role.SUPER_ADMIN);
        seedUser("admin",      "admin@dev.local",      "admin123",      Role.ADMIN);
        seedUser("user",       "user@dev.local",       "user123",       Role.USER);

        log.info("[DevSeeder] ✓ Dev credentials:");
        log.info("[DevSeeder]   SUPER_ADMIN → superadmin / superadmin123");
        log.info("[DevSeeder]   ADMIN       → admin      / admin123");
        log.info("[DevSeeder]   USER        → user       / user123");
    }

    private void seedUser(String username, String email, String rawPassword, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            log.debug("[DevSeeder] '{}' already exists, skipping.", username);
            return;
        }
        String hash = passwordEncoder.encode(rawPassword);
        User user = User.createNew(username, email, hash, role);
        userRepository.save(user);
        log.info("[DevSeeder] Created {} → {}", role, username);
    }
}
