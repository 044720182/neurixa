# Quick Start Guide

## Prerequisites

1. **Java 21**
2. **MongoDB** running on `localhost:27017`
3. **Redis** running on `localhost:6379`

## Start Services

### Using Docker (Recommended)

```bash
# Start MongoDB
docker run -d -p 27017:27017 --name neurixa-mongodb mongo:latest

# Start Redis
docker run -d -p 6379:6379 --name neurixa-redis redis:latest
```

### Or Install Locally

**macOS:**
```bash
brew install mongodb-community redis
brew services start mongodb-community
brew services start redis
```

## Build and Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew :neurixa-boot:bootRun
```

Application will start on `http://localhost:8080`

## Test the API

### 1. Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": "65f1a2b3c4d5e6f7g8h9i0j1",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": "65f1a2b3c4d5e6f7g8h9i0j1",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

### 3. Use the Token

Save the token and use it in subsequent requests:

```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Logout

```bash
curl -X POST http://localhost:8080/api/auth/logout
```

## Verify MongoDB

```bash
# Connect to MongoDB
mongosh

# Switch to neurixa database
use neurixa

# View users
db.users.find().pretty()
```

## Run Tests

```bash
# Run all tests
./gradlew test

# Run only core tests (fast)
./gradlew :neurixa-core:test

# Run with coverage
./gradlew test jacocoTestReport
```

## Stop Services

```bash
# Stop application
Ctrl+C

# Stop Docker containers
docker stop neurixa-mongodb neurixa-redis
docker rm neurixa-mongodb neurixa-redis
```

## Troubleshooting

### Port Already in Use

If port 8080 is already in use, change it in `application.yml`:

```yaml
server:
  port: 8081
```

### MongoDB Connection Failed

Check if MongoDB is running:
```bash
docker ps | grep mongo
# or
brew services list | grep mongodb
```

### Redis Connection Failed

Check if Redis is running:
```bash
docker ps | grep redis
# or
brew services list | grep redis
```

## Next Steps

- Read `API-DOCUMENTATION.md` for complete API reference
- Read `ARCHITECTURE.md` to understand the project structure
- Read `PHASE-2-COMPLETE.md` for implementation details

## Available Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login user |
| POST | `/api/auth/logout` | Logout user |

## Project Structure

```
neurixa/
├── neurixa-core/          # Pure business logic (no frameworks)
├── neurixa-adapter/       # Infrastructure (MongoDB, Redis)
├── neurixa-config/        # Security configuration (JWT)
└── neurixa-boot/          # Spring Boot application
```

Happy coding! 🚀
