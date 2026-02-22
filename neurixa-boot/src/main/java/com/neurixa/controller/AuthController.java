package com.neurixa.controller;

import com.neurixa.config.security.JwtTokenProvider;
import com.neurixa.config.security.TokenBlacklistService;
import com.neurixa.core.domain.User;
import com.neurixa.core.usecase.LoginUserUseCase;
import com.neurixa.core.usecase.RegisterUserUseCase;
import com.neurixa.dto.request.LoginRequest;
import com.neurixa.dto.request.RegisterRequest;
import com.neurixa.dto.response.AuthResponse;
import com.neurixa.dto.response.MessageResponse;
import com.neurixa.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Invoke use case (business logic in core)
        User user = registerUserUseCase.execute(
                request.username(),
                request.email(),
                request.password(),
                "USER"  // Default role
        );

        // Generate JWT token after successful registration
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole());

        // Convert domain to DTO
        UserResponse userResponse = toUserResponse(user);
        AuthResponse response = new AuthResponse(token, userResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Invoke use case (business logic in core)
        User user = loginUserUseCase.execute(request.username(), request.password());

        // Generate JWT token after successful authentication
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole());

        // Convert domain to DTO
        UserResponse userResponse = toUserResponse(user);
        AuthResponse response = new AuthResponse(token, userResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Invalid or expired token"));
        }

        Date expiration = jwtTokenProvider.getExpiration(token);
        tokenBlacklistService.blacklist(token, expiration);
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
