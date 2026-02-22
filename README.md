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

- Ensure MongoDB and Redis are running locally.

### Development profile (uses application-dev.yml)
```bash
./gradlew :neurixa-boot:bootRun --args='--spring.profiles.active=dev'
```

### Run with environment variable (default profile)
macOS/Linux:
```bash
export JWT_SECRET="$(openssl rand -base64 32)"
./gradlew :neurixa-boot:bootRun
```

One‑liner:
```bash
JWT_SECRET="$(openssl rand -base64 32)" ./gradlew :neurixa-boot:bootRun
```

Windows PowerShell:
```powershell
$env:JWT_SECRET = [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
./gradlew :neurixa-boot:bootRun
```

Windows CMD:
```cmd
set JWT_SECRET=your-32-bytes-minimum-secret
gradlew :neurixa-boot:bootRun
```

### Alternative: pass as JVM argument
```bash
./gradlew :neurixa-boot:bootRun -Dspring-boot.run.jvmArguments="-Djwt.secret=$(openssl rand -base64 32)"
```

### Verify
```bash
curl http://localhost:8080/actuator/health
```

## Security

Dual SecurityFilterChain:
- `/admin/**` - Admin endpoints (requires ROLE_ADMIN)
- `/api/**` - API endpoints (JWT authentication)
- `/api/auth/**` - Public authentication endpoints
