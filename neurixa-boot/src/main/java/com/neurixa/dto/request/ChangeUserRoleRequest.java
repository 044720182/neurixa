package com.neurixa.dto.request;

import com.neurixa.core.domain.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleRequest(
        @NotNull(message = "Role cannot be null")
        Role role
) {}
