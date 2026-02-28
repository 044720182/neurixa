# Neurixa File Management Module

This guide explains the file upload and management capability added to Neurixa using clean, layered architecture. It is suitable for students and engineers learning how to build production-ready file storage with Spring Boot, Java, MongoDB, a pluggable StorageProvider (local filesystem now), and how to switch to AWS S3 later without refactoring business logic.

---

## What You Will Learn
- How files and folders are modeled in a clean Domain (no frameworks)
- How use cases orchestrate uploads, moves, renames, deletes, and listings
- How MongoDB stores metadata while binaries go to a StorageProvider
- How to switch from local filesystem to AWS S3 by implementing one interface
- How to call the REST endpoints with cURL/Postman

---

## Architecture Overview

### Layers and Responsibilities
- Domain (Core)
  - Aggregates: `StoredFile` (file), `Folder`
  - Entity: `FileVersion` (version history under a file)
  - Value Objects: `FileId`, `FolderId`, `FileVersionId`, `Checksum`
  - Enum: `FileStatus` = `UPLOADING | ACTIVE | DELETED`
  - No Spring or Mongo annotations; only pure business rules and invariants.
- Application (Core Use Cases)
  - `UploadFileUseCase`, `CreateFolderUseCase`, `MoveFileUseCase`, `RenameFileUseCase`, `DeleteFileUseCase`, `ListFolderContentUseCase`
  - Validates ownership and constraints, calls the storage abstraction, persists aggregates, and returns results.
- Adapter (Infrastructure)
  - MongoDB documents/repositories for `folders`, `files`, `file_versions`
  - `LocalStorageProvider` implementation of `StorageProvider` (filesystem)
- Boot (Delivery)
  - REST controller for files/folders
  - DTOs for requests and responses
  - Bean configuration wiring use cases

### Storage Abstraction
```
interface StorageProvider {
  String store(InputStream data, String filename);
  InputStream retrieve(String storageKey);
  void delete(String storageKey);
}
```
Business logic talks only to `StorageProvider`. This allows swapping to S3/MinIO later without touching use cases.

---

## Data Model (MongoDB)

- Collections
  - `folders`: ownerId, name, parentId, path (materialized path), deleted, timestamps
  - `files`: ownerId, folderId, name, mimeType, size, status, currentVersion, deleted, timestamps
  - `file_versions`: fileId, versionNumber, storageKey, size, checksum, createdAt
- Rules
  - Binary data is never stored in MongoDB
  - Soft delete files (status `DELETED`, flag) to keep history and allow async cleanup
  - `path` uses materialized paths to support nested folders efficiently

### Recommended Indexes
- folders: `ownerId`, `parentId`, `path`, compound `{ ownerId, parentId }`
- files: `ownerId`, `folderId`, `status`, compound `{ ownerId, folderId }`
- file_versions: `fileId`

---

## REST Endpoints (JWT-protected)

Set Postman variables:
- `base` = `http://localhost:8080`
- `token` = your JWT (string)

### Upload File
Upload to root:
```bash
curl -X POST "{{base}}/api/files/upload" \
  -H "Authorization: Bearer {{token}}" \
  -F "file=@/absolute/path/to/file.ext"
```
Upload to a folder:
```bash
curl -X POST "{{base}}/api/files/upload?folderId=FOLDER_ID" \
  -H "Authorization: Bearer {{token}}" \
  -F "file=@/absolute/path/to/file.ext"
```

### Create Folder
Root:
```bash
curl -X POST "{{base}}/api/folders" \
  -H "Authorization: Bearer {{token}}" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Folder","parentId":null}'
```
Nested:
```bash
curl -X POST "{{base}}/api/folders" \
  -H "Authorization: Bearer {{token}}" \
  -H "Content-Type: application/json" \
  -d '{"name":"Nested Folder","parentId":"PARENT_FOLDER_ID"}'
```

### List Folder Contents
Root:
```bash
curl -X GET "{{base}}/api/folders/contents" \
  -H "Authorization: Bearer {{token}}"
```
Specific folder:
```bash
curl -X GET "{{base}}/api/folders/contents?parentId=FOLDER_ID" \
  -H "Authorization: Bearer {{token}}"
```

### Rename File
```bash
curl -X PUT "{{base}}/api/files/FILE_ID/rename" \
  -H "Authorization: Bearer {{token}}" \
  -H "Content-Type: application/json" \
  -d '{"name":"new-name.ext"}'
```

### Move File
To a folder:
```bash
curl -X PUT "{{base}}/api/files/FILE_ID/move" \
  -H "Authorization: Bearer {{token}}" \
  -H "Content-Type: application/json" \
  -d '{"targetFolderId":"TARGET_FOLDER_ID"}'
```
Back to root:
```bash
curl -X PUT "{{base}}/api/files/FILE_ID/move" \
  -H "Authorization: Bearer {{token}}" \
  -H "Content-Type: application/json" \
  -d '{"targetFolderId":null}'
```

### Delete File (Soft Delete)
```bash
curl -X DELETE "{{base}}/api/files/FILE_ID" \
  -H "Authorization: Bearer {{token}}"
```

---

## Where Files Are Stored Now
By default, binaries are written to the local filesystem via `LocalStorageProvider`:
- Base folder: `~/neurixa-storage`
- Config override: set `storage.local.root=/absolute/path` in application.yml
- Storage keys are date-partitioned: `YYYY/MM/DD/<uuid>-<filename>`
MongoDB stores only metadata and the `storageKey` pointing to the binary’s path.

---

## Switching to AWS S3 Later

You can swap the storage backend by implementing `StorageProvider` for S3 and wiring it as a Spring bean in the adapter layer.

### 1) Add Dependencies (adapter module)
Gradle (Kotlin or Groovy syntax depending on your build):
```groovy
implementation platform("software.amazon.awssdk:bom:2.25.58")
implementation "software.amazon.awssdk:s3"
```

### 2) Configure Credentials and Bucket
Prefer AWS’ default credential chain (env vars, profile, IAM role). Minimal properties:
```yaml
s3:
  bucket: your-bucket-name
  region: us-east-1
```

### 3) Implement S3StorageProvider
```java
@Component
@Primary // ensure this overrides LocalStorageProvider when present
public class S3StorageProvider implements StorageProvider {
  private final S3Client s3;
  private final String bucket;

  public S3StorageProvider(@Value("${s3.bucket}") String bucket,
                           @Value("${s3.region}") String region) {
    this.bucket = bucket;
    this.s3 = S3Client.builder()
        .region(Region.of(region))
        .build(); // uses default credential provider chain
  }

  @Override
  public String store(InputStream data, String filename) {
    String key = buildKey(filename);
    s3.putObject(builder -> builder.bucket(bucket).key(key).build(),
                 RequestBody.fromInputStream(data, data.available()));
    return key;
  }

  @Override
  public InputStream retrieve(String storageKey) {
    ResponseInputStream<GetObjectResponse> in =
        s3.getObject(b -> b.bucket(bucket).key(storageKey));
    return in;
  }

  @Override
  public void delete(String storageKey) {
    s3.deleteObject(b -> b.bucket(bucket).key(storageKey));
  }

  private String buildKey(String filename) {
    String safe = filename == null || filename.isBlank() ? "file" : filename.replaceAll("[\\r\\n]", "_");
    LocalDate d = LocalDate.now();
    return d.getYear() + "/" + String.format("%02d", d.getMonthValue()) + "/" +
           String.format("%02d", d.getDayOfMonth()) + "/" + UUID.randomUUID() + "-" + safe;
  }
}
```

### 4) Activate S3
- Ensure the S3 bean is picked up (e.g., `@Primary` on `S3StorageProvider`) or use a profile-based configuration:
  - Add `@Profile("s3")` to S3 provider and run with `--spring.profiles.active=s3`
- Remove or keep `LocalStorageProvider`; Spring will prefer the `@Primary` bean.

### 5) Security & Cost Notes
- Rely on IAM roles for credentials in production
- Enable bucket policies for least privilege
- Consider lifecycle rules and S3 storage classes to manage costs

---

## Ownership & Security
- All file and folder requests are scoped by the authenticated user (ownerId)
- Controllers resolve the current user and pass `UserId` to use cases
- Cross-user access is not allowed; folders are validated for ownership before uploads or moves
- Authentication and RBAC stay untouched; file APIs inherit `/api/**` protections

---

## Troubleshooting
- 401 Unauthorized: Ensure you pass a valid `Authorization: Bearer <token>`
- Mongo connection: verify `spring.data.mongodb.uri`
- Local filesystem: set `storage.local.root` if default path is not writable
- S3 errors: verify bucket name/region, IAM permissions, and network access

---

## Learning Takeaways
- Clean Architecture makes storage pluggable and testable
- Keep domain pure and externalize IO to adapters
- Use materialized paths for nested folders at scale
- Soft delete and version entities to support future recovery and auditing

