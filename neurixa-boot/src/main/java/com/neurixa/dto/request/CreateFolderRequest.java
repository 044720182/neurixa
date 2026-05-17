package com.neurixa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFolderRequest(
        @NotBlank(message = "Folder name is required")
        @Size(min = 1, max = 255, message = "Folder name must be between 1 and 255 characters")
        String name,

        String parentId
) {}
