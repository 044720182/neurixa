# Running Neurixa Application

## Quick Start (Development)

### 1. Start Required Services

```bash
# MongoDB
docker run -d -p 27017:27017 --name neurixa-mongodb mongo:latest

# Redis
docker run -d -p 6379:6379 --name neurixa-redis redis:latest
```

### 2. Run with Development Profile

```bash
# Using Gradle
./gradlew :neurixa-boot:bootRun --args='--spring.profiles.active=dev'

# Or using JAR
java -jar neurixa-boot/build/libs/neurixa-boot-1.0.0.jar --spring.profiles.active=dev
```

The application will start on `http://localhost:8080`

---

## Production Deployment

### 1. Set Environment Variables

```bash
export JWT_SECRET="your-production-secret-minimum-256-bits-change-regularly"
export JWT_VALIDITY=900000  # 15 minutes
export MONGODB_URI="mongodb://prod-server:27017/neurixa"
export REDIS_HOST="prod-redis-server"
export REDIS_PORT=6379
```

### 2. Build Production JAR

```bash
./gradlew clean build
```

### 3. Run Application

```bash
java -jar neurixa-boot/build/libs/neurixa-boot-1.0.0.jar
```

**Note:** Application will FAIL to start if `JWT_SECRET` is not set. This is intentional for security.

---

## Configuration Profiles

### Development Profile (`application-dev.yml`)

```yaml
jwt:
  secret: neurixa-dev-secret-key-minimum-256-bits-for-development-only-change-in-production
  validity: 3600000  # 1 hour

logging:
  level:
    com.neurixa: DEBUG
```

**Usage:**
```bash
--spring.profiles.active=dev
```

### Production Profile (Environment Variables)

```bash
JWT_SECRET=<your-secret>
JWT_VALIDITY=900000
```

**Usage:**
```bash
# No profile needed, uses environment variables
java -jar neurixa-boot-1.0.0.jar
```

---

## Verify Application Started

```bash
# Check health
curl http://localhost:8080/actuator/health

# Test registration
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

---

## Troubleshooting

### Application Fails to Start

**Error:** `JWT secret must be configured`

**Solution:**
```bash
# Development
./gradlew :neurixa-boot:bootRun --args='--spring.profiles.active=dev'

# Production
export JWT_SECRET="your-secret-minimum-32-bytes"
java -jar neurixa-boot-1.0.0.jar
```

### MongoDB Connection Failed

**Error:** `MongoSocketOpenException`

**Solution:**
```bash
# Check MongoDB is running
docker ps | grep mongo

# Start MongoDB
docker run -d -p 27017:27017 --name neurixa-mongodb mongo:latest
```

### Redis Connection Failed

**Error:** `RedisConnectionException`

**Solution:**
```bash
# Check Redis is running
docker ps | grep redis

# Start Redis
docker run -d -p 6379:6379 --name neurixa-redis redis:latest
```

---

## Security Notes

1. **Never commit JWT_SECRET to repository**
2. **Use strong secrets (minimum 32 bytes)**
3. **Rotate secrets regularly in production**
4. **Use HTTPS in production**
5. **Set short token validity (15 minutes recommended)**

---

## Stop Services

```bash
# Stop application
Ctrl+C

# Stop Docker containers
docker stop neurixa-mongodb neurixa-redis
docker rm neurixa-mongodb neurixa-redis
```
