# Neurixa Quick Start & Operations Guide

## Table of Contents
1. [Prerequisites](#1-prerequisites)
2. [Start Infrastructure Services](#2-start-infrastructure-services)
3. [Run the Application](#3-run-the-application)
4. [Verify & Test the API](#4-verify--test-the-api)
5. [Configuration Profiles](#5-configuration-profiles)
6. [Production Deployment](#6-production-deployment)
7. [Troubleshooting](#7-troubleshooting)
8. [Git & Remote Setup](#8-git--remote-setup)

---

## 1. Prerequisites

| Requirement | Version | Check |
|-------------|---------|-------|
| Java | 21+ | `java -version` |
| Gradle | Wrapper included | `./gradlew --version` |
| MongoDB | Any | see §2 |
| Redis | Any | see §2 |

---

## 2. Start Infrastructure Services

### Option A — Docker (Recommended)

```bash
docker run -d -p 27017:27017 --name neurixa-mongodb mongo:latest
docker run -d -p 6379:6379   --name neurixa-redis  redis:latest

# Verify both are running
docker ps
```

### Option B — Homebrew (macOS)

```bash
brew install mongodb-community redis
brew services start mongodb-community
brew services start redis
```

### Stop Services

```bash
docker stop neurixa-mongodb neurixa-redis
docker rm   neurixa-mongodb neurixa-redis
```

---

## 3. Run the Application

### Development Mode (with preset dev secret)

```bash
./gradlew :neurixa-boot:bootRun --args='--spring.profiles.active=dev'
```

Uses `application-dev.yml` — includes a pre-configured JWT secret for ease of local development.

### From a Built JAR

```bash
# Build
./gradlew build

# Run (dev)
java -jar neurixa-boot/build/libs/neurixa-boot-1.0.0.jar \
     --spring.profiles.active=dev

# Run (prod — requires JWT_SECRET env var)
export JWT_SECRET="$(openssl rand -base64 48)"
java -jar neurixa-boot/build/libs/neurixa-boot-1.0.0.jar
```

Application starts on `http://localhost:8080`.

### Build Commands Reference

```bash
./gradlew build                          # Full build + tests
./gradlew clean build                    # Clean then build
./gradlew :neurixa-core:test             # Run core unit tests only (fast)
./gradlew test jacocoTestReport          # Run all tests + coverage report
./gradlew :neurixa-core:dependencies     # Inspect core dependencies (should be empty)
```

---

## 4. Verify & Test the API

### Health Check

```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

### Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","email":"john@example.com","password":"password123"}'
```

**201 Created:**
```json
{
  "token": "eyJhbGci...",
  "type": "Bearer",
  "user": { "id": "...", "username": "john_doe", "email": "john@example.com", "role": "USER" }
}
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"password123"}'
```

### Use a Protected Endpoint

```bash
TOKEN="<paste token here>"

curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/users/me
```

### Logout

```bash
curl -X POST http://localhost:8080/api/auth/logout
# Stateless — the client should discard the token after this call
```

### MongoDB Inspection

```bash
mongosh
use neurixa
db.users.find().pretty()
```

---

## 5. Configuration Profiles

### Development Profile (`application-dev.yml`)

Activated with `--spring.profiles.active=dev`.

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/neurixa
    redis:
      host: 127.0.0.1
      port: 6379

jwt:
  secret: neurixa-dev-secret-key-minimum-256-bits-for-development-only  # pre-set, not for production
  validity: 3600000  # 1 hour

logging:
  level:
    com.neurixa: DEBUG
```

### Production (Environment Variables)

No profile flag needed — just set environment variables before running.

```bash
# Required
export JWT_SECRET="$(openssl rand -base64 48)"   # min 32 bytes

# Optional overrides
export JWT_VALIDITY=900000                        # 15 minutes (recommended)
export MONGODB_URI="mongodb://prod-server:27017/neurixa"
export REDIS_HOST="prod-redis-server"
export REDIS_PORT=6379
```

`application.yml` (production):
```yaml
jwt:
  secret: ${JWT_SECRET:}    # no fallback — app refuses to start without this
  validity: ${JWT_VALIDITY:3600000}
```

> **Never use the dev secret in production.** The application intentionally fails to start without a proper `JWT_SECRET` in production mode.

---

## 6. Production Deployment

### Checklist

- [ ] Set `JWT_SECRET` environment variable (minimum 32 bytes, rotate regularly)
- [ ] Set `MONGODB_URI` to a secure, authenticated URI
- [ ] Set `REDIS_HOST` and `REDIS_PORT`
- [ ] Enable HTTPS (via reverse proxy — Nginx, Caddy, or load balancer)
- [ ] Set `JWT_VALIDITY` to 900000 (15 minutes) or less
- [ ] Configure CORS for your frontend domains
- [ ] Set up monitoring (see Actuator endpoints in `API-DOCUMENTATION.md`)
- [ ] Set up log aggregation
- [ ] Review and implement Phase 3 security enhancements (refresh tokens, rate limiting)

### Reverse Proxy (Nginx Example)

```nginx
server {
    listen 443 ssl;
    server_name api.yourdomain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### Kubernetes Probes

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

---

## 7. Troubleshooting

| Error | Cause | Fix |
|-------|-------|-----|
| `JWT secret must be configured` | Missing JWT_SECRET in prod | Add `--spring.profiles.active=dev` locally, or set `JWT_SECRET` in prod |
| `MongoSocketOpenException` | MongoDB not running | `docker run -d -p 27017:27017 mongo:latest` |
| `RedisConnectionException` | Redis not running | `docker run -d -p 6379:6379 redis:latest` |
| `Port 8080 is already in use` | Port conflict | Add `--server.port=8081` or kill the process using port 8080 |
| `403 Forbidden` | Missing/invalid token or wrong role | Login first; include `Authorization: Bearer <token>` header |
| Gradle build fails | Wrong Java version | Confirm `java -version` shows Java 21+ |

### Diagnostic Commands

```bash
# Check MongoDB
docker ps | grep mongo
mongosh --eval "db.runCommand({ping:1})"

# Check Redis
docker ps | grep redis
redis-cli ping      # should return PONG

# Check running app
curl http://localhost:8080/actuator/health

# Clean build
./gradlew clean build

# Verbose logging
./gradlew :neurixa-boot:bootRun \
  --args='--spring.profiles.active=dev --logging.level.com.neurixa=DEBUG'
```

---

## 8. Git & Remote Setup

This repository uses a dedicated SSH key for the campusut GitHub account.

### SSH Configuration (`~/.ssh/config`)

```ssh
# Personal account
Host github.com
  HostName github.com
  User git
  IdentityFile ~/.ssh/id_ed25519

# CampusUT account (used by this repo)
Host github-campusut
  HostName github.com
  User git
  IdentityFile ~/.ssh/id_ed25519_campusut
  IdentitiesOnly yes
```

### Remote URL

```bash
git remote -v
# origin  git@github-campusut:044720182/neurixa.git (fetch)
# origin  git@github-campusut:044720182/neurixa.git (push)
```

### Common Commands

```bash
git add . && git commit -m "feat: your message" && git push origin main

git pull origin main
git log --oneline -10
git checkout -b feature/my-feature
```

### SSH Troubleshooting

```bash
# Test connection
ssh -T git@github-campusut
# Expected: Hi 044720182! You've successfully authenticated...

# If key not loaded
ssh-add ~/.ssh/id_ed25519_campusut
ssh-add -l   # list loaded keys
```
