package com.neurixa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameFileRequest(
        @NotBlank(message = "File name is required")
        @Size(min = 1, max = 255, message = "File name must be between 1 and 255 characters")
        String name
) {}
