package com.neurixa.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RenameFileRequest(
        @NotBlank(message = "Name is required")
        String name
) {}

