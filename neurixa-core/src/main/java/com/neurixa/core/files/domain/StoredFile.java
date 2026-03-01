package com.neurixa.core.files.domain;

import com.neurixa.core.domain.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class StoredFile {
    private final FileId id;
    private final UserId ownerId;
    private final String name;
    private final String mimeType;
    private final long size;
    private final FolderId folderId;
    private final FileStatus status;
    private final int currentVersion;
    private final boolean deleted;
    private final Instant createdAt;
    private final Instant updatedAt;

    private StoredFile(FileId id,
                       UserId ownerId,
                       String name,
                       String mimeType,
                       long size,
                       FolderId folderId,
                       FileStatus status,
                       int currentVersion,
                       boolean deleted,
                       Instant createdAt,
                       Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.ownerId = Objects.requireNonNull(ownerId);
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        this.name = name;
        this.mimeType = mimeType;
        if (size < 0) throw new IllegalArgumentException("size must be >= 0");
        this.size = size;
        this.folderId = folderId;
        this.status = Objects.requireNonNull(status);
        if (currentVersion <= 0) throw new IllegalArgumentException("currentVersion must be > 0");
        this.currentVersion = currentVersion;
        this.deleted = deleted;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static StoredFile createNew(UserId ownerId, String name, String mimeType, long size, FolderId folderId) {
        Instant now = Instant.now();
        return new StoredFile(
                new FileId(UUID.randomUUID().toString()),
                ownerId,
                name,
                mimeType,
                size,
                folderId,
                FileStatus.UPLOADING,
                1,
                false,
                now,
                now
        );
    }

    public StoredFile markActive() {
        return new StoredFile(id, ownerId, name, mimeType, size, folderId, FileStatus.ACTIVE, currentVersion, deleted, createdAt, Instant.now());
    }

    public StoredFile rename(String newName) {
        if (newName == null || newName.isBlank()) throw new IllegalArgumentException("newName required");
        return new StoredFile(id, ownerId, newName, mimeType, size, folderId, status, currentVersion, deleted, createdAt, Instant.now());
    }

    public StoredFile move(FolderId newFolderId) {
        return new StoredFile(id, ownerId, name, mimeType, size, newFolderId, status, currentVersion, deleted, createdAt, Instant.now());
    }

    public StoredFile markDeleted() {
        return new StoredFile(id, ownerId, name, mimeType, size, folderId, FileStatus.DELETED, currentVersion, true, createdAt, Instant.now());
    }

    public StoredFile incrementVersion() {
        return new StoredFile(id, ownerId, name, mimeType, size, folderId, status, currentVersion + 1, deleted, createdAt, Instant.now());
    }

    public static StoredFile from(FileId id,
                                  UserId ownerId,
                                  String name,
                                  String mimeType,
                                  long size,
                                  FolderId folderId,
                                  FileStatus status,
                                  int currentVersion,
                                  boolean deleted,
                                  Instant createdAt,
                                  Instant updatedAt) {
        return new StoredFile(id, ownerId, name, mimeType, size, folderId, status, currentVersion, deleted, createdAt, updatedAt);
    }

    public FileId getId() {
        return id;
    }

    public UserId getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getSize() {
        return size;
    }

    public FolderId getFolderId() {
        return folderId;
    }

    public FileStatus getStatus() {
        return status;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
