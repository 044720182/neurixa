package com.neurixa.core.files.domain;

import com.neurixa.core.domain.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Folder {
    private final FolderId id;
    private final UserId ownerId;
    private final String name;
    private final FolderId parentId;
    private final String path;
    private final boolean deleted;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Folder(FolderId id, UserId ownerId, String name, FolderId parentId, String path, boolean deleted, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.ownerId = Objects.requireNonNull(ownerId);
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        this.name = name;
        this.parentId = parentId;
        if (path == null || path.isBlank()) throw new IllegalArgumentException("path is required");
        this.path = path;
        this.deleted = deleted;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Folder createRoot(UserId ownerId, String name) {
        FolderId id = new FolderId(UUID.randomUUID().toString());
        String path = "/" + id.getValue();
        Instant now = Instant.now();
        return new Folder(id, ownerId, name, null, path, false, now, now);
    }

    public static Folder createChild(UserId ownerId, String name, Folder parent) {
        FolderId id = new FolderId(UUID.randomUUID().toString());
        String path = parent.getPath() + "/" + id.getValue();
        Instant now = Instant.now();
        return new Folder(id, ownerId, name, parent.getId(), path, false, now, now);
    }

    public Folder rename(String newName) {
        if (newName == null || newName.isBlank()) throw new IllegalArgumentException("newName required");
        return new Folder(id, ownerId, newName, parentId, path, deleted, createdAt, Instant.now());
    }

    public Folder move(Folder newParent) {
        String newPath = newParent.getPath() + "/" + id.getValue();
        return new Folder(id, ownerId, name, newParent.getId(), newPath, deleted, createdAt, Instant.now());
    }

    public Folder markDeleted() {
        return new Folder(id, ownerId, name, parentId, path, true, createdAt, Instant.now());
    }

    public static Folder from(FolderId id, UserId ownerId, String name, FolderId parentId, String path, boolean deleted, Instant createdAt, Instant updatedAt) {
        return new Folder(id, ownerId, name, parentId, path, deleted, createdAt, updatedAt);
    }

    public FolderId getId() {
        return id;
    }

    public UserId getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public FolderId getParentId() {
        return parentId;
    }

    public String getPath() {
        return path;
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
