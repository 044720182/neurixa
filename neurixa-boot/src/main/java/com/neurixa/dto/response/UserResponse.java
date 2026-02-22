package com.neurixa.dto.response;

public record UserResponse(
        String id,
        String username,
        String email,
        String role
) {}
