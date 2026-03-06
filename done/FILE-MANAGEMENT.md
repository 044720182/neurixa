# Neurixa File Management Module

## Table of Contents
1. [Architecture Overview](#1-architecture-overview)
2. [Data Model](#2-data-model)
3. [REST API](#3-rest-api)
4. [Local Storage](#4-local-storage)
5. [Switching to AWS S3](#5-switching-to-aws-s3)
6. [Security & Ownership](#6-security--ownership)
7. [Troubleshooting](#7-troubleshooting)

---

## 1. Architecture Overview

The File Management module follows the same Hexagonal Architecture as the rest of Neurixa. Business logic lives in the core with zero framework dependencies; infrastructure concerns (MongoDB, filesystem, S3) are adapters.

### Layers

| Layer | Module | Contents |
|-------|--------|----------|
| Domain | `neurixa-core` | `StoredFile`, `Folder`, `FileVersion`, `FileStatus`, value objects (`FileId`, `FolderId`, `Checksum`) |
| Use Cases | `neurixa-core` | `UploadFileUseCase`, `CreateFolderUseCase`, `MoveFileUseCase`, `RenameFileUseCase`, `DeleteFileUseCase`, `ListFolderContentUseCase` |
| Ports | `neurixa-core` | `FileRepository`, `FolderRepository`, `StorageProvider` |
| Adapters | `neurixa-adapter` | `MongoFileRepository`, `MongoFolderRepository`, `LocalStorageProvider` |
| Delivery | `neurixa-boot` | `FileController`, `FolderController`, request/response DTOs |

### Storage Abstraction

The core only talks to one interface. This makes storage backends completely interchangeable.

```java
// In neurixa-core (port)
public interface StorageProvider {
    String store(InputStream data, String filename);    // returns storageKey
    InputStream retrieve(String storageKey);
    void delete(String storageKey);
}
```

- **Now:** `LocalStorageProvider` writes to `~/neurixa-storage`
- **Later:** `S3StorageProvider` writes to AWS S3 — **no use case changes required**

### Upload Flow

```
Client sends multipart file to POST /api/files/upload
  ↓
FileController extracts authenticated user + optional folderId
  ↓
UploadFileUseCase:
  1. Validates folder ownership (if folderId given)
  2. Calls storageProvider.store() → returns storageKey
  3. Creates StoredFile entity (status: UPLOADING → ACTIVE)
  4. Creates FileVersion entity (version 1)
  5. Saves both to MongoDB
  ↓
Controller returns 201 with FileResponse DTO
```

---

## 2. Data Model

Binary data is **never stored in MongoDB**. MongoDB holds only metadata; binary files live in the storage backend (filesystem or S3).

### Collections

#### `folders`

| Field | Type | Description |
|-------|------|-------------|
| `ownerId` | String | User ID — scoped to owner |
| `name` | String | Folder display name |
| `parentId` | String (nullable) | Parent folder ID; null = root |
| `path` | String | Materialized path (e.g., `/Docs/Reports`) |
| `deleted` | boolean | Soft delete flag |
| `createdAt` | DateTime | |
| `updatedAt` | DateTime | |

#### `files`

| Field | Type | Description |
|-------|------|-------------|
| `ownerId` | String | User ID |
| `folderId` | String (nullable) | Containing folder; null = root |
| `name` | String | File display name |
| `mimeType` | String | e.g., `text/markdown` |
| `size` | long | Bytes |
| `status` | Enum | `UPLOADING`, `ACTIVE`, `DELETED` |
| `currentVersion` | int | Latest version number |
| `deleted` | boolean | Soft delete flag |
| `createdAt` | DateTime | |
| `updatedAt` | DateTime | |

#### `file_versions`

| Field | Type | Description |
|-------|------|-------------|
| `fileId` | String | Parent file ID |
| `versionNumber` | int | Incrementing version |
| `storageKey` | String | Key used to locate the binary in storage |
| `size` | long | Bytes |
| `checksum` | String | Content hash for integrity |
| `createdAt` | DateTime | |

### Rules

- Binary data never stored in MongoDB — only the `storageKey` pointer
- Files are **soft-deleted** (status → `DELETED`, `deleted = true`) to preserve version history and allow async cleanup
- `path` uses a materialized path pattern for efficient nested folder queries
- All operations are scoped by `ownerId` — cross-user access is blocked at the use case level

### Recommended Indexes

```javascript
// folders
db.folders.createIndex({ ownerId: 1 })
db.folders.createIndex({ parentId: 1 })
db.folders.createIndex({ path: 1 })
db.folders.createIndex({ ownerId: 1, parentId: 1 })

// files
db.files.createIndex({ ownerId: 1 })
db.files.createIndex({ folderId: 1 })
db.files.createIndex({ status: 1 })
db.files.createIndex({ ownerId: 1, folderId: 1 })

// file_versions
db.file_versions.createIndex({ fileId: 1 })
```

---

## 3. REST API

All endpoints require JWT: `Authorization: Bearer <token>`.

### Quick Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/files/upload` | Upload a file |
| `PUT` | `/api/files/{id}/rename` | Rename a file |
| `PUT` | `/api/files/{id}/move` | Move a file |
| `DELETE` | `/api/files/{id}` | Soft delete a file |
| `POST` | `/api/folders` | Create a folder |
| `GET` | `/api/folders/contents` | List folder contents |
| `GET` | `/api/folders/contents/paged` | List folder contents (paginated) |

See `API-DOCUMENTATION.md` for full request/response examples and cURL commands.

---

## 4. Local Storage

By default, binaries are stored on the local filesystem.

- **Root path:** `~/neurixa-storage` (override with `storage.local.root` in `application.yml`)
- **Key format:** `YYYY/MM/DD/<uuid>-<filename>` (date-partitioned to avoid huge flat directories)
- **MongoDB** stores only the `storageKey` string — the actual file lives at `<root>/<storageKey>`

```yaml
# application.yml
storage:
  local:
    root: /absolute/path/to/storage   # optional override
```

---

## 5. Switching to AWS S3

When you're ready to move to S3, you only need to implement `StorageProvider`. No use case code changes.

### Step 1 — Add AWS SDK dependency (`neurixa-adapter/build.gradle`)

```groovy
implementation platform("software.amazon.awssdk:bom:2.25.58")
implementation "software.amazon.awssdk:s3"
```

### Step 2 — Configure credentials and bucket

Use AWS's default credential chain (env vars, instance profile, IAM role — no hardcoded keys).

```yaml
# application.yml
s3:
  bucket: your-bucket-name
  region: us-east-1
```

### Step 3 — Implement `S3StorageProvider`

```java
@Component
@Primary  // overrides LocalStorageProvider when present
public class S3StorageProvider implements StorageProvider {

    private final S3Client s3;
    private final String bucket;

    public S3StorageProvider(
            @Value("${s3.bucket}") String bucket,
            @Value("${s3.region}") String region) {
        this.bucket = bucket;
        this.s3 = S3Client.builder()
            .region(Region.of(region))
            .build();  // uses default credential provider chain
    }

    @Override
    public String store(InputStream data, String filename) {
        String key = buildKey(filename);
        s3.putObject(
            b -> b.bucket(bucket).key(key).build(),
            RequestBody.fromInputStream(data, data.available())
        );
        return key;
    }

    @Override
    public InputStream retrieve(String storageKey) {
        return s3.getObject(b -> b.bucket(bucket).key(storageKey));
    }

    @Override
    public void delete(String storageKey) {
        s3.deleteObject(b -> b.bucket(bucket).key(storageKey));
    }

    private String buildKey(String filename) {
        String safe = (filename == null || filename.isBlank())
            ? "file"
            : filename.replaceAll("[\\r\\n]", "_");
        LocalDate d = LocalDate.now();
        return String.format("%d/%02d/%02d/%s-%s",
            d.getYear(), d.getMonthValue(), d.getDayOfMonth(),
            UUID.randomUUID(), safe);
    }
}
```

### Step 4 — Activate

Option A — `@Primary` (always uses S3 when the bean is present):
```java
@Component
@Primary
public class S3StorageProvider implements StorageProvider { ... }
```

Option B — Spring Profile (explicit activation):
```java
@Component
@Profile("s3")
public class S3StorageProvider implements StorageProvider { ... }
```
```bash
java -jar neurixa-boot.jar --spring.profiles.active=s3
```

### S3 Security & Cost Notes

- **Credentials:** Use IAM roles (EC2/ECS instance profiles) in production — never hardcode access keys
- **Bucket policy:** Apply least-privilege bucket policies; block public access
- **Lifecycle rules:** Configure S3 storage classes and lifecycle rules to manage costs (e.g., move old versions to Glacier)
- **Encryption:** Enable SSE-S3 or SSE-KMS for server-side encryption

---

## 6. Security & Ownership

- Every file and folder operation is scoped to the authenticated user's `ownerId`
- Controllers resolve the current user from the JWT principal and pass a `UserId` to use cases
- Cross-user access (e.g., accessing another user's file by guessing an ID) is rejected at the use case level — ownership is validated before any operation
- File APIs inherit the `/api/**` JWT protection from the main security configuration — no extra auth setup needed

---

## 7. Troubleshooting

| Problem | Likely Cause | Fix |
|---------|-------------|-----|
| `401 Unauthorized` | Missing or expired JWT | Ensure `Authorization: Bearer <token>` is set |
| `404` on folder upload | Wrong `folderId` or not owned by user | Verify folder exists and belongs to your account |
| Files not appearing | Upload in `UPLOADING` status | Check that use case completed; look for errors in logs |
| `Cannot write to storage` | Local path not writable | Set `storage.local.root` to a writable directory |
| S3 `NoSuchBucket` | Wrong bucket name or region | Verify `s3.bucket` and `s3.region` in config |
| S3 `AccessDenied` | IAM permissions missing | Grant `s3:PutObject`, `s3:GetObject`, `s3:DeleteObject` on the bucket |
| MongoDB connection error | DB not running | Check `spring.data.mongodb.uri` and verify DB is reachable |
