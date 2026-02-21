# Phase 1: Project Foundation

## Overview
Established the foundational structure for Neurixa, a multi-module Spring Boot application using Hexagonal-lite architecture with clean separation of concerns.

## What We Built

### Project Structure
```
neurixa/
├── neurixa-core/          # Pure domain logic
├── neurixa-adapter/       # Infrastructure implementations
├── neurixa-config/        # Security & shared configuration
└── neurixa-boot/          # Spring Boot application entry
```

### Technology Stack
- Java 21
- Gradle 8.5 (Groovy DSL)
- Spring Boot 3.2.2
- MongoDB (Spring Data)
- Redis (Spring Data)
- JWT (jjwt 0.12.5)
- Lombok

### Module Dependencies
```
neurixa-boot
  ├─> neurixa-adapter
  │     └─> neurixa-core (no Spring deps)
  └─> neurixa-config
```

## Implemented Components

### neurixa-core (Domain Layer)
- `User` domain entity
- `UserRepository` port interface
- `RegisterUserUseCase` business logic
- Zero Spring dependencies (pure Java)

### neurixa-adapter (Infrastructure Layer)
- `MongoUserRepository` - UserRepository implementation
- `UserMongoRepository` - Spring Data MongoDB interface
- `UserDocument` - MongoDB entity mapping
- Adapters for MongoDB and Redis

### neurixa-config (Configuration Layer)
- `SecurityConfig` - Dual SecurityFilterChain
  - Admin chain: `/admin/**` (ROLE_ADMIN required)
  - API chain: `/api/**` (JWT authentication)
  - Public: `/api/auth/**`
- `JwtTokenProvider` - Token generation and validation
- `JwtAuthenticationFilter` - JWT request filter
- BCrypt password encoder

### neurixa-boot (Application Layer)
- `NeurixaApplication` - Spring Boot main class
- `UseCaseConfiguration` - Use case bean definitions
- `application.yml` - MongoDB, Redis, JWT configuration

## Architecture Principles

### Hexagonal-lite
- Core domain isolated from frameworks
- Ports define contracts (UserRepository)
- Adapters implement infrastructure (MongoUserRepository)
- Use cases orchestrate business logic

### Dependency Rules
- Core has NO external dependencies (except Lombok)
- Adapter depends on Core
- Config is independent (security concerns)
- Boot wires everything together

## Security Implementation

### Dual SecurityFilterChain
Two separate security contexts for different API segments:

1. **Admin Chain** (`/admin/**`)
   - Requires ROLE_ADMIN
   - JWT-based authentication
   - Stateless sessions

2. **API Chain** (`/api/**`)
   - Public auth endpoints: `/api/auth/**`
   - Protected endpoints require JWT
   - Stateless sessions

### JWT Configuration
- HMAC-SHA256 signing
- 1-hour token validity
- Bearer token authentication
- Username and role claims

## Build System

### Gradle Version Catalog
Centralized dependency management in `gradle/libs.versions.toml`:
- Spring Boot 3.2.2
- MongoDB & Redis starters
- JWT libraries
- Lombok

### Multi-Module Setup
- Root project coordinates submodules
- Shared Java 21 configuration
- Consistent dependency versions
- Independent module builds

## Configuration

### Application Properties
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/neurixa
    redis:
      host: localhost
      port: 6379

jwt:
  secret: neurixa-secret-key-change-in-production-minimum-256-bits
  validity: 3600000

server:
  port: 8080
```

## Build Verification

✅ Project builds successfully: `./gradlew build`
✅ All modules compile without errors
✅ Core module has no Spring dependencies
✅ Dependency graph is clean and acyclic
✅ Spring Boot JAR created (37MB)

## Next Steps (Phase 2 Suggestions)

1. **REST Controllers**
   - AuthController for registration/login
   - UserController for user management
   - Admin endpoints

2. **Service Layer**
   - UserService with business logic
   - Password encoding integration
   - Token generation on login

3. **Exception Handling**
   - Global exception handler
   - Custom domain exceptions
   - API error responses

4. **Validation**
   - Input validation (Bean Validation)
   - Custom validators
   - Error messages

5. **Testing**
   - Unit tests for use cases
   - Integration tests for repositories
   - Security tests for endpoints

6. **Additional Features**
   - User roles and permissions
   - Refresh tokens
   - Redis caching
   - Audit logging

## Commands

```bash
# Build project
./gradlew build

# Run application
./gradlew :neurixa-boot:bootRun

# Clean build
./gradlew clean build

# View dependencies
./gradlew :neurixa-core:dependencies
```

## Notes

- MongoDB and Redis must be running locally
- JWT secret should be changed in production
- Default port is 8080
- All endpoints use stateless sessions
