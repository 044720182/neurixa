package com.neurixa.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class TokenBlacklistService {
    private static final String KEY_PREFIX = "blacklist:jwt:";
    private static final String BLACKLISTED_VALUE = "1";
    private final StringRedisTemplate redisTemplate;

    public void blacklist(String token, Date expiration) {
        if (!StringUtils.hasText(token) || expiration == null) {
            return; // Nothing to do when token or expiration is missing
        }

        long ttlMillis = expiration.getTime() - System.currentTimeMillis();
        if (ttlMillis <= 0) {
            ttlMillis = 1000; // minimal TTL to ensure key expires shortly
        }

        redisTemplate.opsForValue().set(buildKey(token), BLACKLISTED_VALUE, Duration.ofMillis(ttlMillis));
    }

    public boolean isBlacklisted(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        Boolean exists = redisTemplate.hasKey(buildKey(token));
        return Boolean.TRUE.equals(exists);
    }

    private String buildKey(String token) {
        return KEY_PREFIX + sha256(token);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}

