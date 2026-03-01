package com.neurixa.dto.response;

import java.time.Instant;

public record FolderResponse(
        String id,
        String name,
        String parentId,
        String path,
        Instant createdAt,
        Instant updatedAt
) {}

