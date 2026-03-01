# Neurixa File Upload and Management APIs

This document provides a comprehensive evaluation of the file upload and management features in Neurixa. It covers the architecture, code implementation, REST APIs, cURL examples, and storage mechanisms. Designed for developers to learn and users to understand the system.

---

## Overview
Neurixa implements file upload and management using Hexagonal Architecture (Ports and Adapters). Files are uploaded via REST APIs, stored securely, and managed with operations like create, list, rename, move, and delete. Binaries are kept separate from metadata for scalability.

Key Features:
- JWT-protected APIs
- Folder-based organization
- Versioning support
- Pluggable storage (local filesystem or S3)
- Ownership-based access control

---

## Architecture Overview

### Layers
- **Domain (Core)**: Business entities like `StoredFile`, `Folder`, `FileVersion`. Pure Java, no frameworks.
- **Application (Use Cases)**: Orchestrates operations (e.g., `UploadFileUseCase`). Validates rules and calls ports.
- **Infrastructure (Adapter)**: Implements ports (e.g., `LocalStorageProvider`, MongoDB repositories).
- **Delivery (Boot)**: REST controllers, DTOs, and configuration.

### Key Interfaces and Classes
- **StorageProvider** (Port): Abstracts storage backend.
  ```java
  interface StorageProvider {
      String store(InputStream data, String filename);
      InputStream retrieve(String storageKey);
      void delete(String storageKey);
  }
  ```
- **UploadFileUseCase** (Core): Handles upload logic.
  ```java
  public StoredFile execute(UserId ownerId, String originalFilename, String mimeType, long size, FolderId targetFolderId, InputStream data) {
      // Validate folder ownership
      // Store binary via StorageProvider
      // Save metadata to repositories
      // Create version
  }
  ```
- **FileController** (Boot): REST endpoints.
- **LocalStorageProvider** (Adapter): Filesystem implementation, stores in `~/neurixa-storage/YYYY/MM/DD/<uuid>-<filename>`.

### Data Flow for Upload
1. Client sends multipart file to `/api/files/upload`.
2. Controller extracts user, validates folder (if provided).
3. Use case calls `StorageProvider.store()` to save binary.
4. Metadata saved to MongoDB (`files`, `file_versions`).
5. Response includes file details.

---

## Data Model (MongoDB)
- **folders**: `ownerId`, `name`, `parentId`, `path` (materialized), `deleted`, timestamps.
- **files**: `ownerId`, `folderId`, `name`, `mimeType`, `size`, `status` (UPLOADING/ACTIVE/DELETED), `currentVersion`, timestamps.
- **file_versions**: `fileId`, `versionNumber`, `storageKey`, `size`, `checksum`, `createdAt`.
- Rules: Binaries not in DB; soft deletes; ownership enforced.

Recommended Indexes: `ownerId`, `folderId`, `status`, etc.

---

## REST APIs
All endpoints require JWT: `Authorization: Bearer <token>`. Base URL: `http://localhost:8080`.

### 1. Upload File
- **Endpoint**: `POST /api/files/upload`
- **Description**: Uploads a file to root or a folder. Creates metadata and stores binary.
- **Request**: Multipart form-data with `file` (required), `folderId` (optional).
- **Response**: `201 Created` with file details.
- **Code Snippet** (Controller):
  ```java
  @PostMapping(path = "/files/upload", consumes = {"multipart/form-data"})
  public ResponseEntity<FileResponse> upload(@RequestPart("file") MultipartFile file,
                                             @RequestParam(value = "folderId", required = false) String folderId,
                                             Principal principal) throws IOException {
      User user = getUserByUsernameUseCase.execute(principal.getName());
      // ... validate and execute use case
  }
  ```

### 2. Create Folder
- **Endpoint**: `POST /api/folders`
- **Description**: Creates a folder in root or under a parent.
- **Request**: JSON `{"name": "FolderName", "parentId": "optional"}`.
- **Response**: `201 Created` with folder details.

### 3. List Folder Contents
- **Endpoint**: `GET /api/folders/contents`
- **Description**: Lists files and subfolders in root or a folder.
- **Request**: Query param `parentId` (optional).
- **Response**: JSON with folders and files.

### 4. Rename File
- **Endpoint**: `PUT /api/files/{id}/rename`
- **Description**: Renames a file.
- **Request**: JSON `{"name": "newName.ext"}`.
- **Response**: Updated file details.

### 5. Move File
- **Endpoint**: `PUT /api/files/{id}/move`
- **Description**: Moves file to another folder or root.
- **Request**: JSON `{"targetFolderId": "folderId"}` (null for root).
- **Response**: Updated file details.

### 6. Delete File
- **Endpoint**: `DELETE /api/files/{id}`
- **Description**: Soft deletes a file (marks as DELETED).
- **Response**: `204 No Content`.

---

## cURL Examples
Set variables: `base=http://localhost:8080`, `token=<your-jwt>`.

### Upload to Root
```bash
curl -X POST "{{base}}/api/files/upload" \
  -H "Authorization: Bearer {{token}}" \
  -F "file=@/path/to/file.ext"
```

### Upload to Folder
```bash
curl -X POST "{{base}}/api/files/upload?folderId=123" \
  -H "Authorization: Bearer {{token}}" \
  -F "file=@/path/to/file.ext"
```

### Create Folder
```bash
curl -X POST "{{base}}/api/folders" \
  -H "Authorization: Bearer {{token}}" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Folder","parentId":null}'
```

### List Root Contents
```bash
curl -X GET "{{base}}/api/folders/contents" \
  -H "Authorization: Bearer {{token}}"
```

### Rename File
```bash
curl -X PUT "{{base}}/api/files/456/rename" \
  -H "Authorization: Bearer {{token}}" \
  -H "Content-Type: application/json" \
  -d '{"name":"new-name.ext"}'
```

### Move File
```bash
curl -X PUT "{{base}}/api/files/456/move" \
  -H "Authorization: Bearer {{token}}" \
  -H "Content-Type: application/json" \
  -d '{"targetFolderId":"789"}'
```

### Delete File
```bash
curl -X DELETE "{{base}}/api/files/456" \
  -H "Authorization: Bearer {{token}}"
```

---

## Storage Details
- **Current**: Local filesystem via `LocalStorageProvider`.
- **Path**: `~/neurixa-storage` (or `storage.local.root` in config).
- **Structure**: `YYYY/MM/DD/<uuid>-<filename>`.
- **Switching to S3**: Implement `S3StorageProvider` with `@Primary`, add AWS SDK deps, configure bucket/region.
  ```java
  @Component
  @Primary
  public class S3StorageProvider implements StorageProvider {
      // AWS S3 implementation
  }
  ```

---

## Security and Ownership
- All operations scoped by authenticated user (`UserId`).
- Folders validated for ownership.
- No cross-user access.
- Inherits `/api/**` protections (JWT, RBAC).

---

## Troubleshooting
- **401 Unauthorized**: Check JWT token.
- **Folder Not Found**: Verify `folderId` ownership.
- **Storage Errors**: Ensure write permissions or S3 credentials.
- **Mongo Issues**: Check `spring.data.mongodb.uri`.

---

## Learning Takeaways
- Hexagonal Architecture enables easy storage swaps.
- Separate concerns: metadata in DB, binaries in storage.
- Use cases enforce business rules.
- Soft deletes preserve history.</content>
<parameter name="filePath">/Users/yusuf.ibrahim/Projects/neurixa/done/File-Management-Upload.md
