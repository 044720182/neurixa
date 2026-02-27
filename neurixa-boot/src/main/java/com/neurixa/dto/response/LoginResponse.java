package com.neurixa.dto.response;

public record LoginResponse(
        String token,
        String type,
        UserResponse user
) {}
