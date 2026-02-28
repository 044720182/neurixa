package com.neurixa.dto.response;

import com.neurixa.core.files.domain.FileStatus;

import java.time.Instant;

public record FileResponse(
        String id,
        String name,
        String mimeType,
        long size,
        String folderId,
        FileStatus status,
        int currentVersion,
        Instant createdAt,
        Instant updatedAt
) {}

