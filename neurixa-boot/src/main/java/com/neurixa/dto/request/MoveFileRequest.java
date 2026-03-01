package com.neurixa.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MoveFileRequest(
        String targetFolderId
) {}

