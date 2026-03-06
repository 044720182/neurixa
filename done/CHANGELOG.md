# Neurixa Changelog

A summary of every development phase: what was built, why decisions were made, and what each commit delivered.

---

## Phase 1 — Project Foundation

**Commit:** `e718097 be7ca18`  
**Goal:** Establish a clean multi-module Spring Boot skeleton with strict Hexagonal-lite architecture.

### Technology Stack

| Technology | Version |
|------------|---------|
| Java | 21 |
| Spring Boot | 3.2.2 |
| Gradle | 8.5 (Groovy DSL) |
| MongoDB | Spring Data |
| Redis | Spring Data |
| JWT | jjwt 0.12.5 |

### Module Structure Created

```
neurixa/
├── neurixa-core/     # Pure domain logic — ZERO external dependencies
├── neurixa-adapter/  # MongoDB and Redis implementations
├── neurixa-config/   # Spring Security + JWT
└── neurixa-boot/     # Spring Boot entry point
```

### What Was Built

**Core (`neurixa-core`):**
- `User` domain entity — immutable, final fields, constructor validation
- `UserRepository` port interface
- `RegisterUserUseCase` — pure business logic
- Domain exception hierarchy: `DomainException` → `UserAlreadyExistsException`, `InvalidUserStateException`
- **Zero Spring/Mongo/Lombok dependencies** — verified via `./gradlew :neurixa-core:dependencies`

**Adapter (`neurixa-adapter`):**
- `MongoUserRepository` — implements `UserRepository` port
- `UserDocument` — MongoDB entity mapping
- `UserMongoRepository` — Spring Data interface

**Config (`neurixa-config`):**
- `SecurityConfig` — dual `SecurityFilterChain` (admin chain + api chain)
- `JwtTokenProvider` — token creation and validation (HMAC-SHA256)
- `JwtAuthenticationFilter` — JWT request filter
- BCrypt password encoder

**Boot (`neurixa-boot`):**
- `NeurixaApplication` — Spring Boot entry
- `UseCaseConfiguration` — use case bean definitions
- `application.yml` — base configuration

**Tests:** 14 unit tests, all passing (pure JUnit/AssertJ/Mockito — no Spring context)

---

## Phase 1.5 — Core Hardening

**Commit:** `e718097`  
**Goal:** Enforce strict Hexagonal-lite principles — zero external dependencies in core.

### Key Changes

**Removed Lombok from `neurixa-core`:**
- ❌ `@Data`, `@Builder`, `@Getter` removed
- ✅ Replaced with explicit constructors, getters, `equals()`, `hashCode()`, `toString()`

```java
// Before (Lombok)
@Data @Builder
public class User {
    private String password;
}

// After (pure Java)
public final class User {
    private final String passwordHash;   // renamed for clarity

    public User(String id, String username, String email,
                String passwordHash, String role) {
        // validation in constructor
        this.passwordHash = passwordHash;
    }

    public String getPasswordHash() { return passwordHash; }
}
```

**Improved domain model:**
- All fields `final` — immutable entity
- Constructor-enforced validation (3–50 chars username, `@` in email, etc.)
- Renamed `password` → `passwordHash` to make intent explicit

**Replaced test stack in core:**
- ❌ Removed `spring-boot-starter-test` (pulls in Spring context)
- ✅ Added `junit-jupiter`, `assertj-core`, `mockito-core` only

**Verified dependency isolation:**
```bash
./gradlew :neurixa-core:dependencies --configuration compileClasspath
# Result: No dependencies ✅
```

---

## Phase 2 — Authentication REST API

**Commit:** `726f9a8`  
**Goal:** Implement complete auth API while strictly respecting architectural boundaries.

### What Was Added

**Core additions:**
- `PasswordEncoder` port interface — use case depends on abstraction, not BCrypt directly
- `LoginUserUseCase` — authenticates user via username + password
- Updated `RegisterUserUseCase` — uses `PasswordEncoder` port
- `InvalidCredentialsException` — domain exception for failed login

**Adapter additions:**
- `BcryptPasswordEncoderAdapter` — implements `PasswordEncoder` using Spring's BCrypt

**Boot additions:**
- `AuthController` — `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`
- Request DTOs: `RegisterRequest`, `LoginRequest` (Jakarta Bean Validation)
- Response DTOs: `AuthResponse`, `UserResponse`, `MessageResponse`, `ErrorResponse`
- `GlobalExceptionHandler` — maps domain exceptions to HTTP status codes

### Architectural Compliance

```
Controller  →  Use Case  →  Port  →  Adapter  →  MongoDB
   (boot)       (core)     (core)   (adapter)
```

Controllers call use cases. Use cases call ports. Adapters implement ports. No business logic leaks into controllers or adapters.

**Tests:** 20 unit tests, all passing

### HTTP Status Codes

| Endpoint | Success | Errors |
|----------|---------|--------|
| `POST /api/auth/register` | 201 | 400 (validation), 409 (duplicate) |
| `POST /api/auth/login` | 200 | 400 (validation), 401 (invalid creds) |
| `POST /api/auth/logout` | 200 | — |

---

## Phase 2.5 — Security Hardening

**Commit:** `2e092a1`  
**Goal:** Harden JWT implementation to production-grade security.

### Problems Found & Fixed

| Issue | Before | After |
|-------|--------|-------|
| Default secret | `@Value("${jwt.secret:fallback-key}")` | No fallback — app fails to start |
| Secret validation | None | Minimum 32 bytes enforced |
| Signing algorithm | Implicit | Explicit `Jwts.SIG.HS256` |
| Issuer claim | Missing | `.issuer("neurixa")` |
| Role extraction | Not implemented | `getRole(token)` method added |
| Authorities | `emptyList()` | `SimpleGrantedAuthority("ROLE_" + role)` |
| 401 responses | Default Spring error | `JwtAuthenticationEntryPoint` returns JSON |
| Filter error handling | None | `clearContext()` + always continues filter chain |

**Security audit:** 60/60 checklist items passed. Score: **9/10**

The 1-point gap: refresh tokens, token blacklist, and rate limiting are not yet implemented (documented in `SECURITY.md §6`).

---

## Phase 3 — Monitoring (Spring Boot Actuator)

**Commit:** `5ec9072`  
**Goal:** Add production-ready observability without exposing sensitive data.

### What Was Added

**Dependency:**
```groovy
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

**Endpoints exposed:**

| Endpoint | Access | Purpose |
|----------|--------|---------|
| `/actuator/health` | Public | MongoDB, Redis, disk health |
| `/actuator/health/liveness` | Public | Kubernetes liveness probe |
| `/actuator/health/readiness` | Public | Kubernetes readiness probe |
| `/actuator/info` | Public | Application metadata |
| `/actuator/metrics` | ADMIN | JVM, HTTP, DB metrics |
| `/actuator/prometheus` | ADMIN | Prometheus scrape endpoint |
| `/actuator/env` | ADMIN (dev only) | Environment properties |
| `/actuator/beans` | ADMIN (dev only) | Spring bean listing |

**Security:** A dedicated `SecurityFilterChain` (Order 2) secures actuator endpoints — health and info are public, everything else requires `ROLE_ADMIN`.

**See:** `API-DOCUMENTATION.md` → Monitoring section for full config and cURL examples.

---

## Commit History

```
5ec9072  feat(monitoring): add Spring Boot Actuator with secure configuration
d7b261a  docs: add comprehensive API documentation and test results
2e092a1  security: harden JWT authentication to production-grade standards
726f9a8  feat(api): implement authentication REST API with strict architectural boundaries
e718097  refactor(core): harden neurixa-core to strict hexagonal-lite principles
be7ca18  refactor(core): harden neurixa-core to strict hexagonal-lite principles (initial)
```

All commits follow **Conventional Commits** format:  
`<type>(<scope>): <description>`

Types used: `refactor`, `feat`, `security`, `docs`

---

## Stats

| Metric | Value |
|--------|-------|
| Total commits | 5 major |
| Files changed | ~38 |
| Lines added | ~5,800 |
| Core tests | 20 (all passing) |
| Security audit items | 60/60 passing |
| Security score | 9/10 |
| Core dependencies | 0 (verified) |
