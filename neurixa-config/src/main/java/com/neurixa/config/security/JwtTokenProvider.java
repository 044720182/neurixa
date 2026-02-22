package com.neurixa.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final String ISSUER = "neurixa";
    private static final int MINIMUM_SECRET_LENGTH = 32; // 256 bits
    
    private final SecretKey key;
    private final long validityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.validity:3600000}") long validityInMilliseconds) {
        
        // Validate secret is provided
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("JWT secret must be configured. Set jwt.secret property.");
        }
        
        // Validate secret length (minimum 256 bits / 32 bytes)
        if (secret.getBytes(StandardCharsets.UTF_8).length < MINIMUM_SECRET_LENGTH) {
            throw new IllegalStateException(
                String.format("JWT secret must be at least %d bytes (256 bits). Current length: %d bytes",
                    MINIMUM_SECRET_LENGTH, secret.getBytes(StandardCharsets.UTF_8).length)
            );
        }
        
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String createToken(String username, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuer(ISSUER)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(ISSUER)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // Additional validation: check expiration explicitly
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            // Do not log token or exception details (security)
            return false;
        }
    }

    public Date getExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
