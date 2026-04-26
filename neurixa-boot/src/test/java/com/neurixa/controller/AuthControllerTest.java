package com.neurixa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neurixa.config.security.JwtAuthenticationEntryPoint;
import com.neurixa.config.security.JwtAuthenticationFilter;
import com.neurixa.config.security.JwtTokenProvider;
import com.neurixa.config.security.SecurityConfig;
import com.neurixa.config.security.TokenBlacklistService;
import com.neurixa.core.domain.Role;
import com.neurixa.core.domain.User;
import com.neurixa.core.domain.UserId;
import com.neurixa.core.exception.InvalidCredentialsException;
import com.neurixa.core.exception.UserAlreadyExistsException;
import com.neurixa.core.usecase.LoginUserUseCase;
import com.neurixa.core.usecase.RegisterUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean RegisterUserUseCase registerUserUseCase;
    @MockBean LoginUserUseCase loginUserUseCase;
    @MockBean JwtTokenProvider jwtTokenProvider;
    @MockBean TokenBlacklistService tokenBlacklistService;
    @MockBean UserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        var ctor = User.class.getDeclaredConstructor(
                UserId.class, String.class, String.class, String.class,
                Role.class, boolean.class, boolean.class, int.class,
                Instant.class, Instant.class);
        ctor.setAccessible(true);
        testUser = ctor.newInstance(
                new UserId("user-1"), "john_doe", "john@example.com", "hash",
                Role.USER, false, true, 0, Instant.now(), Instant.now());
    }

    // ── POST /api/auth/register ───────────────────────────────────────────────

    @Test
    void register_success_returns201WithToken() throws Exception {
        when(registerUserUseCase.execute("john_doe", "john@example.com", "password123"))
                .thenReturn(testUser);
        when(jwtTokenProvider.createToken("john_doe", "USER")).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "john_doe",
                                "email", "john@example.com",
                                "password", "password123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("john_doe"))
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    @Test
    void register_blankUsername_returns400WithValidationDetails() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "",
                                "email", "john@example.com",
                                "password", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "john_doe",
                                "email", "not-an-email",
                                "password", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "john_doe",
                                "email", "john@example.com",
                                "password", "abc"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void register_duplicateUsername_returns409() throws Exception {
        when(registerUserUseCase.execute(anyString(), anyString(), anyString()))
                .thenThrow(new UserAlreadyExistsException(
                        UserAlreadyExistsException.Field.USERNAME, "Username already exists: john_doe"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "john_doe",
                                "email", "john@example.com",
                                "password", "password123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Username already exists: john_doe"));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(registerUserUseCase.execute(anyString(), anyString(), anyString()))
                .thenThrow(new UserAlreadyExistsException(
                        UserAlreadyExistsException.Field.EMAIL, "Email already exists: john@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "john_doe",
                                "email", "john@example.com",
                                "password", "password123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists: john@example.com"));
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────────

    @Test
    void login_success_returns200WithToken() throws Exception {
        when(loginUserUseCase.execute("john_doe", "password123")).thenReturn(testUser);
        when(jwtTokenProvider.createToken("john_doe", "USER")).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "john_doe",
                                "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.user.username").value("john_doe"));
    }

    @Test
    void login_withEmail_returns200() throws Exception {
        when(loginUserUseCase.execute("john@example.com", "password123")).thenReturn(testUser);
        when(jwtTokenProvider.createToken("john_doe", "USER")).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "john@example.com",
                                "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        when(loginUserUseCase.execute(anyString(), anyString()))
                .thenThrow(new InvalidCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "john_doe",
                                "password", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_blankPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "john_doe",
                                "password", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    // ── POST /api/auth/logout ─────────────────────────────────────────────────

    @Test
    void logout_validToken_returns200() throws Exception {
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getExpiration("valid-token")).thenReturn(new Date(System.currentTimeMillis() + 3600000));

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(tokenBlacklistService).blacklist(eq("valid-token"), any(Date.class));
    }

    @Test
    void logout_missingAuthHeader_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }

    @Test
    void logout_invalidToken_returns400() throws Exception {
        when(jwtTokenProvider.validateToken("bad-token")).thenReturn(false);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }
}
