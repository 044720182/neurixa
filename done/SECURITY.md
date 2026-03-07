# Neurixa Security Guide

## Table of Contents
1. [JWT Architecture](#1-jwt-architecture)
2. [Security Filter Chains](#2-security-filter-chains)
3. [Hardening Applied](#3-hardening-applied)
4. [Security Verification Matrix](#4-security-verification-matrix)
5. [Production Checklist](#5-production-checklist)
6. [Recommended Enhancements](#6-recommended-enhancements)

---

## 1. JWT Architecture

### Token Structure

```json
{
  "sub": "john_doe",
  "role": "USER",
  "iss": "neurixa",
  "iat": 1708588800,
  "exp": 1708592400
}
```

| Claim | Value | Purpose |
|-------|-------|---------|
| `sub` | username | Identity |
| `role` | `USER` / `ADMIN` / `SUPER_ADMIN` | Authorization |
| `iss` | `"neurixa"` | Issuer verification |
| `iat` | timestamp | Issued at |
| `exp` | timestamp | Expiration |

### Token Flow

```
1. User logs in  →  POST /api/auth/login
2. Server validates credentials, generates JWT
3. Client stores token (memory or sessionStorage — avoid localStorage in sensitive apps)
4. Client sends token on every request:
       Authorization: Bearer <token>
5. JwtAuthenticationFilter intercepts request
6. Filter validates signature, expiration, and issuer
7. Filter extracts username + role, populates SecurityContext
8. Request proceeds to controller
```

### Key Configuration

```java
// JwtTokenProvider.java
public String createToken(String username, String role) {
    return Jwts.builder()
        .subject(username)
        .claim("role", role)
        .issuer("neurixa")           // issuer claim
        .issuedAt(now)
        .expiration(validity)
        .signWith(key, Jwts.SIG.HS256)  // explicit algorithm
        .compact();
}

public boolean validateToken(String token) {
    try {
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .requireIssuer("neurixa")
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.getExpiration().after(new Date());
    } catch (Exception e) {
        return false;  // no logging — avoids leaking token info
    }
}
```

---

## 2. Security Filter Chains

Three separate Spring Security filter chains handle different URL patterns.

```
Order 1: /admin/**    →  Requires ROLE_ADMIN
Order 2: /actuator/** →  /health and /info public; rest requires ROLE_ADMIN
Order 3: /api/**      →  /api/auth/** public; all other /api/** requires JWT
```

### Route Authorization Summary

| Pattern | Access |
|---------|--------|
| `/api/auth/**` | Public |
| `/api/**` | Any authenticated user (valid JWT) |
| `/admin/**` | `ROLE_ADMIN` required |
| `/actuator/health` | Public |
| `/actuator/info` | Public |
| `/actuator/**` | `ROLE_ADMIN` required |
| Everything else | Denied |

### JwtAuthenticationFilter

```java
protected void doFilterInternal(HttpServletRequest request, ...) {
    try {
        String token = resolveToken(request);   // extracts from "Bearer " header

        if (token != null
                && tokenProvider.validateToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String username  = tokenProvider.getUsername(token);
            String role      = tokenProvider.getRole(token);
            SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + role);  // Spring convention

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null,
                    Collections.singletonList(authority));

            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    } catch (Exception e) {
        SecurityContextHolder.clearContext();   // clear on any error
    }
    filterChain.doFilter(request, response);    // always continue
}
```

---

## 3. Hardening Applied

These changes were made to bring JWT security to production-grade standard.

### Secret Management

**Before:**
```java
@Value("${jwt.secret:neurixa-secret-key-change-in-production}")  // ❌ fallback secret
```

**After:**
```java
@Value("${jwt.secret}")  // no fallback — app refuses to start without it
// Constructor validates:
if (!StringUtils.hasText(secret)) {
    throw new IllegalStateException("JWT secret must be configured");
}
if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
    throw new IllegalStateException("JWT secret must be at least 32 bytes (256 bits)");
}
```

### Role-Based Authorization

**Before:**
```java
Collections.emptyList()  // ❌ no authorities — ROLE_ADMIN checks always failed
```

**After:**
```java
new SimpleGrantedAuthority("ROLE_" + role)  // ✅ correct Spring Security convention
```

### Proper 401 Responses

Added `JwtAuthenticationEntryPoint` so unauthenticated requests get a JSON `401` instead of a redirect or default error page:

```java
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("""
            {"status":401,"error":"Unauthorized","message":"Missing or invalid JWT token"}
        """);
    }
}
```

### Files Modified

| File | Change |
|------|--------|
| `neurixa-config/.../JwtTokenProvider.java` | Added secret validation, explicit HS256, issuer claim, `getRole()` |
| `neurixa-config/.../JwtAuthenticationFilter.java` | Added role extraction, `ROLE_` prefix, error handling |
| `neurixa-config/.../SecurityConfig.java` | Added `JwtAuthenticationEntryPoint` |
| `neurixa-config/.../JwtAuthenticationEntryPoint.java` | New — returns 401 JSON |
| `neurixa-boot/.../application.yml` | Removed hardcoded secret → `${JWT_SECRET:}` |
| `neurixa-boot/.../application-dev.yml` | New — dev-only preset secret |

---

## 4. Security Verification Matrix

### Runtime Behavior

| Scenario | Expected | Status |
|----------|----------|--------|
| Valid ADMIN token → `/admin/**` | 200 | ✅ |
| Valid USER token → `/admin/**` | 403 | ✅ |
| Valid USER token → `/api/**` | 200 | ✅ |
| Expired token | 401 | ✅ |
| Invalid signature | 401 | ✅ |
| Missing token | 401 | ✅ |
| Random string as token | 401 | ✅ |

### Checklist Summary

| Category | Items | Result |
|----------|-------|--------|
| Secret Handling | No fallback, length validation, env var injection, never logged | ✅ All pass |
| Signing Algorithm | Explicit HS256, signature verified via `verifyWith()` | ✅ All pass |
| Claims Structure | `sub`, `role`, `iss`, `iat`, `exp` all present | ✅ All pass |
| Expiration Behavior | Expired → 401, malformed → 401, no stack trace leak | ✅ All pass |
| SecurityContext | Auth set only when absent, ROLE_ prefix, cleared on error | ✅ All pass |
| Filter Flow | `doFilter()` always called, no premature returns | ✅ All pass |
| Route Authorization | Admin/API chains correct, `anyRequest().denyAll()` catch-all | ✅ All pass |
| Stateless Behavior | `STATELESS` session policy, CSRF disabled | ✅ All pass |
| Architecture | No Spring Security in core, JWT isolated in config module | ✅ All pass |

**Overall Security Score: 9/10**

Deduction: Refresh tokens, token blacklist, and rate limiting are not yet implemented (see §6).

---

## 5. Production Checklist

```bash
# Generate a secure secret
openssl rand -base64 48

# Set required environment variables
export JWT_SECRET="<generated-secret>"
export JWT_VALIDITY=900000           # 15 minutes (recommended for production)
export MONGODB_URI="mongodb://prod-server:27017/neurixa"
export REDIS_HOST="prod-redis-server"
```

- [ ] `JWT_SECRET` is at least 32 bytes, never committed to version control
- [ ] `JWT_VALIDITY` is set to 900000ms (15 min) or less
- [ ] HTTPS is enforced (redirect HTTP → HTTPS)
- [ ] CORS is configured for your specific frontend origins only
- [ ] MongoDB uses authentication credentials (not open access)
- [ ] Redis uses `requirepass` or ACL list
- [ ] Actuator endpoints not reachable from public internet
- [ ] Log aggregation is set up (but no tokens or passwords in logs)
- [ ] JWT secret rotation plan in place

---

## 6. Recommended Enhancements

These are not yet implemented. Add them in the next security phase.

### Refresh Tokens (High Priority)

Short-lived access tokens (15 min) + long-lived refresh tokens (7 days). Reduces exposure if a token is stolen.

```
POST /api/auth/refresh
Body: { "refreshToken": "<refresh-token>" }
Response: { "token": "<new-access-token>" }
```

### Token Blacklist with Redis (High Priority)

Needed for true logout. Store revoked token JTI (JWT ID) in Redis with TTL = token expiry.

```java
// On logout:
redisTemplate.opsForValue().set("blacklist:" + jti, "1", expiry, TimeUnit.MILLISECONDS);

// In validateToken():
if (redisTemplate.hasKey("blacklist:" + claims.getId())) return false;
```

### Rate Limiting (High Priority)

Prevent brute-force attacks on `/api/auth/login`.

```yaml
# application.yml with bucket4j or resilience4j
resilience4j:
  ratelimiter:
    instances:
      login:
        limitForPeriod: 5
        limitRefreshPeriod: 1m
        timeoutDuration: 0
```

### Security Headers (Medium Priority)

Add via Spring Security or Nginx:
- `Strict-Transport-Security` (HSTS)
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `Content-Security-Policy`

### Password Policy (Medium Priority)

- Minimum length and complexity
- Password history (prevent reuse)
- Account lockout after N failed attempts
