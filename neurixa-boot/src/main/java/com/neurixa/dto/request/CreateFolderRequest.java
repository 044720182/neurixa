package com.neurixa.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateFolderRequest(
        @NotBlank(message = "Name is required")
        String name,
        String parentId
) {}

