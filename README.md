# Neurixa

Multi-module Spring Boot project with Hexagonal-lite architecture.

## Architecture

```
neurixa-boot (Spring Boot entry point)
    ├── neurixa-adapter (Infrastructure)
    │   └── neurixa-core (Pure domain)
    └── neurixa-config (Security & JWT)
```

- **neurixa-core**: Pure domain logic (no Spring dependencies)
  - Domain entities (User)
  - Use cases (RegisterUserUseCase)
  - Port interfaces (UserRepository)
  
- **neurixa-adapter**: Infrastructure adapters
  - MongoDB implementation (MongoUserRepository)
  - Spring Data repositories
  - Redis support
  
- **neurixa-config**: Security configuration
  - JWT token provider
  - Dual SecurityFilterChain (admin + api)
  - Authentication filter
  
- **neurixa-boot**: Spring Boot application
  - Main application class
  - Use case bean configuration

## Requirements

- Java 21
- Gradle 8.5+
- MongoDB (localhost:27017)
- Redis (localhost:6379)

## Build

```bash
./gradlew build
```

## Run

```bash
./gradlew :neurixa-boot:bootRun
```

## Security

Dual SecurityFilterChain:
- `/admin/**` - Admin endpoints (requires ROLE_ADMIN)
- `/api/**` - API endpoints (JWT authentication)
- `/api/auth/**` - Public authentication endpoints
