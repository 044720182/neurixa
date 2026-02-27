package com.neurixa.dto.request;

import com.neurixa.core.domain.Role;
import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
        @Email(message = "Email must be a valid email address")
        String email,
        Role role
) {}
