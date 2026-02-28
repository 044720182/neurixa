package com.neurixa.core.files.domain;

import java.time.Instant;
import java.util.Objects;

public final class FileVersion {
    private final FileVersionId id;
    private final FileId fileId;
    private final int versionNumber;
    private final String storageKey;
    private final long size;
    private final Checksum checksum;
    private final Instant createdAt;

    private FileVersion(FileVersionId id, FileId fileId, int versionNumber, String storageKey, long size, Checksum checksum, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.fileId = Objects.requireNonNull(fileId);
        if (versionNumber <= 0) throw new IllegalArgumentException("versionNumber must be > 0");
        this.versionNumber = versionNumber;
        if (storageKey == null || storageKey.isBlank()) throw new IllegalArgumentException("storageKey is required");
        this.storageKey = storageKey;
        if (size < 0) throw new IllegalArgumentException("size must be >= 0");
        this.size = size;
        this.checksum = checksum;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static FileVersion createNew(FileId fileId, int versionNumber, String storageKey, long size, Checksum checksum) {
        return new FileVersion(new FileVersionId(java.util.UUID.randomUUID().toString()), fileId, versionNumber, storageKey, size, checksum, Instant.now());
        }

    public static FileVersion from(FileVersionId id, FileId fileId, int versionNumber, String storageKey, long size, Checksum checksum, Instant createdAt) {
        return new FileVersion(id, fileId, versionNumber, storageKey, size, checksum, createdAt);
    }

    public FileVersionId getId() {
        return id;
    }

    public FileId getFileId() {
        return fileId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public long getSize() {
        return size;
    }

    public Checksum getChecksum() {
        return checksum;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
