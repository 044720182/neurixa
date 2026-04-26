# Neurixa API Documentation

**Base URL:** `http://localhost:8080`  
**Authentication:** JWT Bearer Token (except public `auth` endpoints)

---

## Table of Contents
- [Authentication](#authentication)
- [Users](#users)
- [Admin — User Management](#admin--user-management)
- [Blog](#blog)
- [Files & Folders](#files--folders)
- [Monitoring (Actuator)](#monitoring-actuator)
- [Error Response Format](#error-response-format)

---

## Authentication

All auth endpoints are public — no token required.

### POST `/api/auth/register`

Register a new user account.

**Request:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Validation:**
- `username`: required, 3–50 characters
- `email`: required, valid email format
- `password`: required, minimum 6 characters

**201 Created:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "user": {
    "id": "65f1a2b3c4d5e6f7g8h9i0j1",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

**Error codes:** `400` validation failed · `409` username or email already exists

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","email":"john@example.com","password":"securePassword123"}'
```

---

### POST `/api/auth/login`

Authenticate and receive a JWT. Accepts either username or email.

**Request:**
```json
{ "username": "john_doe", "password": "securePassword123" }
```
or
```json
{ "username": "john@example.com", "password": "securePassword123" }
```

**200 OK:** Same response shape as `/register`

**Error codes:** `400` validation failed · `401` invalid credentials

```bash
# Login with username
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"securePassword123"}'

# Login with email
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john@example.com","password":"securePassword123"}'
```

---

### POST `/api/auth/logout`

Stateless logout — the server returns a confirmation; the **client must discard the token**.

> For production, consider token blacklisting with Redis (see `SECURITY.md`).

**200 OK:**
```json
{ "message": "Logged out successfully" }
```

```bash
curl -X POST http://localhost:8080/api/auth/logout
```

---

## Users

All endpoints require a valid JWT (`Authorization: Bearer <token>`).

### GET `/api/users/me`

Returns the profile of the currently authenticated user.

**200 OK:**
```json
{
  "id": "65f1a2b3c4d5e6f7g8h9i0j1",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER"
}
```

**Error codes:** `401` missing or invalid token

```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/users/me
```

---

### GET `/api/users`

List users with optional filters and pagination. Requires authentication.

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `search` | string | — | Filter by username or email |
| `role` | string | — | Filter by role: `USER`, `ADMIN`, `SUPER_ADMIN` |
| `locked` | boolean | — | Filter by account lock status |
| `page` | int | `0` | Page number |
| `size` | int | `10` | Page size |
| `sortBy` | string | `createdAt` | Sort field |
| `sortDirection` | string | `desc` | `asc` or `desc` |

**200 OK:**
```json
{
  "content": [{ "id": "...", "username": "john_doe", "email": "john@example.com", "role": "USER" }],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 50,
  "totalPages": 5,
  "hasNext": true,
  "hasPrevious": false
}
```

**Error codes:** `401` missing/invalid token · `403` insufficient permissions

```bash
curl -H "Authorization: Bearer $TOKEN" \
     "http://localhost:8080/api/users?page=0&size=10&sortBy=createdAt&sortDirection=desc"
```

---

## Admin — User Management

All `/api/admin/**` endpoints require `ROLE_ADMIN`.

### GET `/api/admin/users`

Paginated user list with extended admin fields.

Same query parameters as `GET /api/users`, but returns richer user objects:

```json
{
  "content": [{
    "id": "...",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER",
    "locked": false,
    "emailVerified": true,
    "failedLoginAttempts": 0,
    "createdAt": "2026-02-22T10:00:00",
    "updatedAt": "2026-02-22T10:00:00"
  }],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 50,
  "totalPages": 3,
  "hasNext": true,
  "hasPrevious": false
}
```

Default page size is `20`.

```bash
curl -H "Authorization: Bearer $ADMIN_TOKEN" \
     "http://localhost:8080/api/admin/users?page=0&size=20"
```

---

### GET `/api/admin/users/me`

Admin's own profile (extended format).

**200 OK:**
```json
{
  "id": "...", "username": "admin_user", "email": "admin@example.com",
  "role": "ADMIN", "locked": false, "emailVerified": true,
  "failedLoginAttempts": 0,
  "createdAt": "2026-02-22T10:00:00", "updatedAt": "2026-02-22T10:00:00"
}
```

---

### PUT `/api/admin/users/{id}`

Update a user's email or role.

**Request:**
```json
{ "email": "new@example.com", "role": "ADMIN" }
```

**204 No Content** on success.

**Error codes:** `400` stale session · `401` · `403` · `404` user not found

```bash
curl -X PUT http://localhost:8080/api/admin/users/{userId} \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"email":"new@example.com","role":"ADMIN"}'
```

> **Note:** After changing a user's role, their existing token still reflects the old role until they log in again. The server will return `400` if a stale token is detected.

---

### PUT `/api/admin/users/{id}/role`

Change a user's role specifically.

**Authorization Rules:**
- `SUPER_ADMIN` can assign any role (except demoting other SUPER_ADMINs)
- `ADMIN` can only set `USER` or `ADMIN`
- Cannot promote locked users
- Cannot demote a `SUPER_ADMIN`

```bash
# Promote to ADMIN
curl -X PUT "http://localhost:8080/api/admin/users/{userId}/role" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role":"ADMIN"}'

# Demote to USER
curl -X PUT "http://localhost:8080/api/admin/users/{userId}/role" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role":"USER"}'
```

**Error codes:** `400` invalid role or locked user · `403` permission denied · `404` user not found · `409` cannot demote SUPER_ADMIN

---

### DELETE `/api/admin/users/{id}`

Delete a user by ID.

**204 No Content** on success.

**Error codes:** `401` · `403` · `404` user not found

```bash
curl -X DELETE http://localhost:8080/api/admin/users/{userId} \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## Blog

### Articles

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `POST` | `/api/blog/articles` | Admin | Create draft article |
| `PUT` | `/api/blog/articles/{id}` | Admin | Update article |
| `DELETE` | `/api/blog/articles/{id}` | Admin | Delete article → `204` |
| `POST` | `/api/blog/articles/{id}/publish` | Admin | Publish article |
| `POST` | `/api/blog/articles/{id}/restore` | Admin | Restore article |
| `GET` | `/api/blog/articles/{slug}` | Public | Get article by slug |
| `GET` | `/api/blog/articles` | Public | List published articles (paginated) |

**Create Article — Request:**
```json
{ "title": "My First Post", "content": "Post content", "excerpt": "Short summary" }
```

**201 Created:**
```json
{
  "id": "c1a2b3c4-d5e6-7890-...",
  "title": "My First Post",
  "slug": "my-first-post",
  "content": "Post content",
  "excerpt": "Short summary",
  "status": "DRAFT",
  "featuredImageId": null,
  "createdAt": "2026-03-02T12:00:00Z",
  "updatedAt": "2026-03-02T12:00:00Z",
  "publishedAt": null,
  "viewCount": 0,
  "metaTitle": null,
  "metaDescription": null,
  "categoryIds": [],
  "tagIds": []
}
```

**List Published Articles:**
```json
{
  "content": [{ "id": "...", "title": "Post Title", "slug": "post-title", "excerpt": "...", "status": "PUBLISHED", "publishedAt": "...", "viewCount": 10 }],
  "pageNumber": 0, "pageSize": 10, "totalElements": 42, "totalPages": 5,
  "hasNext": true, "hasPrevious": false
}
```

```bash
curl -X POST http://localhost:8080/api/blog/articles \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"My First Post","content":"Content here","excerpt":"Summary"}'

curl "http://localhost:8080/api/blog/articles?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

### Comments

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/blog/comments` | Submit a comment |
| `POST` | `/api/blog/comments/{id}/approve` | Approve comment (admin) |
| `POST` | `/api/blog/comments/{id}/reject` | Reject comment (admin) |

**Submit Comment — Request:**
```json
{
  "articleId": "<ARTICLE_ID>",
  "authorName": "Alice",
  "authorEmail": "alice@example.com",
  "content": "Great post!",
  "replyTo": null
}
```

### Categories & Tags

```bash
# Create category
curl -X POST http://localhost:8080/api/blog/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Technology","parentId":null}'

# Create tag
curl -X POST http://localhost:8080/api/blog/tags \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"java"}'
```

---

## Files & Folders

See `FILE-MANAGEMENT.md` for full architecture and S3 migration details.

All endpoints require JWT. Set `base=http://localhost:8080` and `token=<your-jwt>`.

### Folders

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/folders` | Create folder |
| `GET` | `/api/folders/contents` | List contents (non-paged) |
| `GET` | `/api/folders/contents/paged` | List contents (paginated) |

**Create Folder:**
```bash
# Root folder
curl -X POST "$base/api/folders" \
  -H "Authorization: Bearer $token" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Folder","parentId":null}'

# Nested folder
curl -X POST "$base/api/folders" \
  -H "Authorization: Bearer $token" \
  -H "Content-Type: application/json" \
  -d '{"name":"Nested Folder","parentId":"PARENT_FOLDER_ID"}'
```

**List Contents (non-paged):**
```bash
curl "$base/api/folders/contents" -H "Authorization: Bearer $token"
curl "$base/api/folders/contents?parentId=FOLDER_ID" -H "Authorization: Bearer $token"
```

```json
{
  "folders": [{ "id": "folder-1", "name": "Docs", "parentId": null, "path": "/Docs", "createdAt": "...", "updatedAt": "..." }],
  "files":   [{ "id": "file-1", "name": "readme.md", "mimeType": "text/markdown", "size": 1024, "folderId": null, "status": "ACTIVE", "createdAt": "...", "updatedAt": "..." }]
}
```

**List Contents (paged):**

Query parameters: `parentId`, `pageFolders` (default 0), `sizeFolders` (default 20), `pageFiles` (default 0), `sizeFiles` (default 20)

```bash
curl "$base/api/folders/contents/paged?pageFolders=0&sizeFolders=20&pageFiles=0&sizeFiles=20" \
  -H "Authorization: Bearer $token"
```

### Files

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/files/upload` | Upload file (multipart) |
| `PUT` | `/api/files/{id}/rename` | Rename file |
| `PUT` | `/api/files/{id}/move` | Move file |
| `DELETE` | `/api/files/{id}` | Soft delete file |

```bash
# Upload to root
curl -X POST "$base/api/files/upload" \
  -H "Authorization: Bearer $token" \
  -F "file=@/path/to/file.ext"

# Upload to folder
curl -X POST "$base/api/files/upload?folderId=FOLDER_ID" \
  -H "Authorization: Bearer $token" \
  -F "file=@/path/to/file.ext"

# Rename
curl -X PUT "$base/api/files/FILE_ID/rename" \
  -H "Authorization: Bearer $token" \
  -H "Content-Type: application/json" \
  -d '{"name":"new-name.ext"}'

# Move to folder
curl -X PUT "$base/api/files/FILE_ID/move" \
  -H "Authorization: Bearer $token" \
  -H "Content-Type: application/json" \
  -d '{"targetFolderId":"TARGET_FOLDER_ID"}'

# Move to root
curl -X PUT "$base/api/files/FILE_ID/move" \
  -H "Authorization: Bearer $token" \
  -H "Content-Type: application/json" \
  -d '{"targetFolderId":null}'

# Delete (soft)
curl -X DELETE "$base/api/files/FILE_ID" \
  -H "Authorization: Bearer $token"
```

---

## Monitoring (Actuator)

Spring Boot Actuator is configured with a dedicated security chain.

### Endpoint Access Summary

| Endpoint | Access | Purpose |
|----------|--------|---------|
| `GET /actuator/health` | Public | Application health (MongoDB, Redis, disk) |
| `GET /actuator/health/liveness` | Public | Kubernetes liveness probe |
| `GET /actuator/health/readiness` | Public | Kubernetes readiness probe |
| `GET /actuator/info` | Public | Application metadata |
| `GET /actuator/metrics` | ADMIN | JVM, HTTP, DB metrics |
| `GET /actuator/prometheus` | ADMIN | Prometheus-format metrics |
| `GET /actuator/env` | ADMIN (dev) | Environment properties |
| `GET /actuator/beans` | ADMIN (dev) | Spring bean listing |

### Health Response

```json
{
  "status": "UP",
  "components": {
    "mongo":  { "status": "UP", "details": { "maxWireVersion": 21 } },
    "redis":  { "status": "UP", "details": { "version": "7.4.7" } },
    "diskSpace": { "status": "UP" },
    "livenessState":  { "status": "UP" },
    "readinessState": { "status": "UP" }
  }
}
```

### Accessing Protected Actuator Endpoints

```bash
# Get admin token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/actuator/metrics
```

### Prometheus Integration

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'neurixa'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
    bearer_token: '<admin-jwt-token>'
```

### Custom Metrics

```java
@Component
public class CustomMetrics {
    private final Counter userRegistrations;

    public CustomMetrics(MeterRegistry registry) {
        this.userRegistrations = Counter.builder("user.registrations")
            .description("Total user registrations")
            .register(registry);
    }

    public void recordRegistration() {
        userRegistrations.increment();
    }
}
```

### application.yml — Actuator Config

```yaml
# Production
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  health:
    mongo:
      enabled: true
    redis:
      enabled: true

# Development (application-dev.yml adds)
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env,beans
  endpoint:
    health:
      show-details: always
```

---

## Error Response Format

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2026-02-22T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Username already exists: john_doe",
  "path": "/api/auth/register",
  "details": null
}
```

For validation errors, `details` is an array:

```json
{
  "timestamp": "2026-02-22T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input parameters",
  "path": "/api/auth/register",
  "details": [
    "Username must be between 3 and 50 characters",
    "Email must be valid",
    "Password must be at least 6 characters"
  ]
}
```

### HTTP Status Code Summary

| Code | Meaning | When |
|------|---------|------|
| `200` | OK | Successful GET or POST (login, logout) |
| `201` | Created | Successful registration, article/file/folder creation |
| `204` | No Content | Successful DELETE or PUT update |
| `400` | Bad Request | Validation failed, stale token/session, domain invariant violated |
| `401` | Unauthorized | Missing or invalid JWT |
| `403` | Forbidden | Valid JWT but insufficient role |
| `404` | Not Found | Resource doesn't exist |
| `409` | Conflict | Duplicate username/email, role change conflict |
