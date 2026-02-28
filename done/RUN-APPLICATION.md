# Running Neurixa: Step-by-Step Guide for Beginners

Welcome back! 🎉 You've got Neurixa set up—now let's get it running. This guide walks you through starting the app in **development mode** (easy for learning) and **production mode** (secure for real use). We'll cover everything from Docker containers to JWT secrets, with troubleshooting tips along the way.

If you're new, start with the Quick Start section. By the end, you'll have a live API ready to test!

---

## 📋 Prerequisites (Quick Check)

Before running, ensure you have:
- **Java 21** installed (`java -version`)
- **Gradle** (comes with project: `./gradlew --version`)
- **MongoDB** and **Redis** running (see below)

If not, check [README.md](README.md) for full setup instructions.

---

## 🚀 Quick Start: Run Neurixa in Development Mode (5 Minutes)

Perfect for beginners—relaxed settings, easy debugging.

### Step 1: Start Required Services (MongoDB & Redis)

Use Docker for simplicity (install Docker if needed: [Get Docker](https://docs.docker.com/get-docker/)).

```bash
# Start MongoDB (database)
docker run -d -p 27017:27017 --name neurixa-mongodb mongo:latest

# Start Redis (cache)
docker run -d -p 6379:6379 --name neurixa-redis redis:latest
```

- `-d`: Runs in background.
- Check they're running: `docker ps` (you should see `neurixa-mongodb` and `neurixa-redis`).

**No Docker?** Install MongoDB/Redis locally (see [README.md](README.md)).

### Step 2: Run the Application

```bash
# Using Gradle (recommended)
./gradlew :neurixa-boot:bootRun --args='--spring.profiles.active=dev'
```

- This builds and starts the app automatically.
- Uses `application-dev.yml` with dev-friendly configs (longer JWT tokens, debug logging).
- App runs on `http://localhost:8080`.

**Alternative: Using JAR**
```bash
# Build JAR first
./gradlew build

# Then run
java -jar neurixa-boot/build/libs/neurixa-boot-1.0.0.jar --spring.profiles.active=dev
```

### Step 3: Verify It's Working

Open in browser: `http://localhost:8080/actuator/health`

Expected: `{"status":"UP"}` ✅

**Test the API:**
- Swagger UI: `http://localhost:8080/swagger-ui.html` (interactive docs).
- Register a user via curl:
  ```bash
  curl -X POST http://localhost:8080/api/auth/register \
    -H "Content-Type: application/json" \
    -d '{
      "username": "testuser",
      "email": "test@example.com",
      "password": "password123"
    }'
  ```
  Expected: User created successfully!

Congrats! 🎊 Neurixa is live. Explore the API or check logs for "Started NeurixaApplication".

---

## 🏭 Production Deployment: Secure and Scalable

For real-world use—requires careful setup for security.

### ⚠️ Important Warnings
- **Never use dev mode in production!** It has weak secrets and debug logs.
- **Set strong JWT secrets** (minimum 32 bytes, change regularly).
- **Use HTTPS** and secure databases.
- **Monitor logs** for security issues.

### Step 1: Set Environment Variables

These override defaults for security.

```bash
# macOS/Linux
export JWT_SECRET="$(openssl rand -base64 32)"  # Generates a secure random secret
export JWT_VALIDITY=900000  # 15 minutes (short for security)
export MONGODB_URI="mongodb://prod-server:27017/neurixa"  # Your prod MongoDB
export REDIS_HOST="prod-redis-server"  # Your prod Redis
export REDIS_PORT=6379

# Windows PowerShell
$env:JWT_SECRET = [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
$env:JWT_VALIDITY = 900000
$env:MONGODB_URI = "mongodb://prod-server:27017/neurixa"
$env:REDIS_HOST = "prod-redis-server"
$env:REDIS_PORT = 6379

# Windows CMD
set JWT_SECRET=your-32-bytes-minimum-secret-here
set JWT_VALIDITY=900000
set MONGODB_URI=mongodb://prod-server:27017/neurixa
set REDIS_HOST=prod-redis-server
set REDIS_PORT=6379
```

**Why?** JWT_SECRET signs tokens—weak ones can be hacked. Validity limits token lifetime.

### Step 2: Build Production JAR

```bash
./gradlew clean build
```

This creates `neurixa-boot/build/libs/neurixa-boot-1.0.0.jar`.

### Step 3: Run the Application

```bash
java -jar neurixa-boot/build/libs/neurixa-boot-1.0.0.jar
```

- **No profile needed**—uses env vars.
- **Fails if JWT_SECRET missing** (intentional security feature).
- Runs on `http://localhost:8080` (change port in prod via `server.port=8081`).

**Deploy Tips:**
- Use a process manager like systemd or Docker Compose.
- Set up reverse proxy (e.g., Nginx) for HTTPS.
- Monitor with Spring Actuator endpoints.

---

## ⚙️ Configuration Profiles Explained

Neurixa uses **Spring Profiles** to switch configs. Think of them as "modes" for different environments.

### Development Profile (`--spring.profiles.active=dev`)
- **File:** `neurixa-boot/src/main/resources/application-dev.yml`
- **Settings:**
  ```yaml
  jwt:
    secret: neurixa-dev-secret-key-minimum-256-bits-for-development-only-change-in-production  # Weak for ease
    validity: 3600000  # 1 hour (long for testing)
  logging:
    level:
      com.neurixa: DEBUG  # Verbose logs
  ```
- **When to Use:** Learning, local testing. Fast to start, easy debugging.

### Production Profile (Default, via Env Vars)
- **No file needed**—uses environment variables.
- **Settings:** As set above (strong secret, short validity).
- **When to Use:** Real deployments. Secure, optimized.

**Switching:** Add `--spring.profiles.active=dev` to commands. Profiles can be combined (e.g., `dev,prod`).

---

## 🔍 Verify and Test

Once running, confirm everything works:

```bash
# Health check
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}

# Register test user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "email": "test@example.com", "password": "password123"}'

# Login to get JWT
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
# Returns: {"token": "your-jwt-here"}

# Use token for protected endpoints
curl -H "Authorization: Bearer your-jwt-here" http://localhost:8080/api/me
```

**Swagger UI:** `http://localhost:8080/swagger-ui.html` for interactive testing.

---

## 🛠️ Troubleshooting: Common Issues & Fixes

Stuck? Check these first—most problems are simple!

### Application Fails to Start

**Error:** `JWT secret must be configured` or `IllegalArgumentException`

**Cause:** Missing JWT_SECRET in production mode.

**Fix:**
- Dev: Add `--spring.profiles.active=dev` to use dev config.
- Prod: Set `JWT_SECRET` env var (see Production section).

**Check:** Run `./gradlew :neurixa-boot:bootRun --args='--spring.profiles.active=dev'` for dev.

### MongoDB Connection Failed

**Error:** `MongoSocketOpenException` or `com.mongodb.MongoTimeoutException`

**Cause:** MongoDB not running or wrong URI.

**Fix:**
- Check running: `docker ps | grep mongo`
- Start: `docker run -d -p 27017:27017 --name neurixa-mongodb mongo:latest`
- Test: `mongosh` (should connect to localhost:27017)
- Prod: Verify `MONGODB_URI` is correct.

### Redis Connection Failed

**Error:** `RedisConnectionException` or `Connection refused`

**Cause:** Redis not running.

**Fix:**
- Check running: `docker ps | grep redis`
- Start: `docker run -d -p 6379:6379 --name neurixa-redis redis:latest`
- Test: `redis-cli ping` (should return PONG)
- Prod: Check `REDIS_HOST` and `REDIS_PORT`.

### Build Fails

**Error:** Gradle errors or compilation issues.

**Fix:**
- Clean: `./gradlew clean`
- Rebuild: `./gradlew build`
- Check Java: `java -version` (must be 21)
- Dependencies: `./gradlew dependencies` (resolves issues)

### Port Already in Use

**Error:** `Port 8080 is already in use`

**Fix:** Change port in `application.yml` or run with `--server.port=8081`

### 403 Forbidden on API Calls

**Cause:** Missing or invalid JWT token, or wrong role.

**Fix:**
- Login first to get token.
- Add `Authorization: Bearer <token>` header.
- Check user role (e.g., ADMIN needed for `/admin/**`).

### Logs Show Errors

- Run with debug: Add `--logging.level.com.neurixa=DEBUG`
- Check console output for stack traces.
- Common: Typos in env vars or YAML.

**Still stuck?** Check [README.md](README.md) FAQ or open an issue.

---

## 🔐 Security Notes

Security is built-in—follow these to stay safe:

1. **JWT Secrets:** Never commit to code (use env vars). Minimum 32 bytes, rotate often.
2. **Token Validity:** Short (15 min) to limit damage if stolen.
3. **HTTPS:** Always in production (redirect HTTP).
4. **Environment:** Separate dev/prod configs—no dev secrets in prod.
5. **Monitoring:** Use `/actuator/**` endpoints for health/metrics.
6. **Updates:** Keep dependencies patched (run `./gradlew dependencyCheck`).

For more, see [SECURITY-HARDENING-SUMMARY.md](done/SECURITY-HARDENING-SUMMARY.md).

---

## 🛑 Stop and Clean Up

### Stop the App
- Press `Ctrl+C` in terminal.

### Stop Services
```bash
# Docker containers
docker stop neurixa-mongodb neurixa-redis
docker rm neurixa-mongodb neurixa-redis

# Local MongoDB/Redis (if not Docker)
# macOS: brew services stop mongodb/brew/mongodb-community redis
# Linux: sudo systemctl stop mongod redis
```

### Clean Build
```bash
./gradlew clean
```

---

## 🎯 Next Steps

- **Explore:** Visit Swagger UI and test endpoints.
- **Learn:** Read [ARCHITECTURE.md](done/ARCHITECTURE.md) for how it works.
- **Customize:** Change configs in `application.yml`.
- **Deploy:** Try Heroku, AWS, or Kubernetes.
- **Contribute:** Add features or fix bugs!

Happy running! 🚀 If you hit issues, the community is here to help.
