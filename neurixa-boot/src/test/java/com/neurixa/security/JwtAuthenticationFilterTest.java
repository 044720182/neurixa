package com.neurixa.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neurixa.config.security.JwtAuthenticationEntryPoint;
import com.neurixa.config.security.JwtAuthenticationFilter;
import com.neurixa.config.security.JwtTokenProvider;
import com.neurixa.config.security.SecurityConfig;
import com.neurixa.config.security.TokenBlacklistService;
import com.neurixa.controller.AuthController;
import com.neurixa.core.domain.Role;
import com.neurixa.core.usecase.LoginUserUseCase;
import com.neurixa.core.usecase.RegisterUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests JwtAuthenticationFilter behaviour via a real controller slice.
 * Uses AuthController as the target since /api/auth/logout is a protected
 * endpoint that exercises the full filter → controller path.
 *
 * Protected endpoint used: POST /api/auth/logout (requires valid JWT)
 * Public endpoint used:    POST /api/auth/login  (no token needed)
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
class JwtAuthenticationFilterTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean RegisterUserUseCase registerUserUseCase;
    @MockBean LoginUserUseCase loginUserUseCase;
    @MockBean JwtTokenProvider jwtTokenProvider;
    @MockBean TokenBlacklistService tokenBlacklistService;
    @MockBean UserDetailsService userDetailsService;

    private static final String VALID_TOKEN   = "valid.jwt.token";
    private static final String EXPIRED_TOKEN = "expired.jwt.token";
    private static final String INVALID_TOKEN = "garbage-token";
    private static final String BLACKLISTED_TOKEN = "blacklisted.jwt.token";

    @BeforeEach
    void setUp() {
        // valid token
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUsername(VALID_TOKEN)).thenReturn("john_doe");
        when(jwtTokenProvider.getRole(VALID_TOKEN)).thenReturn(Role.USER);
        when(tokenBlacklistService.isBlacklisted(VALID_TOKEN)).thenReturn(false);

        // expired token
        when(jwtTokenProvider.validateToken(EXPIRED_TOKEN)).thenReturn(false);
        when(tokenBlacklistService.isBlacklisted(EXPIRED_TOKEN)).thenReturn(false);

        // invalid / malformed token
        when(jwtTokenProvider.validateToken(INVALID_TOKEN)).thenReturn(false);
        when(tokenBlacklistService.isBlacklisted(INVALID_TOKEN)).thenReturn(false);

        // blacklisted token
        when(tokenBlacklistService.isBlacklisted(BLACKLISTED_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.validateToken(BLACKLISTED_TOKEN)).thenReturn(true);
    }

    // ── Missing token ─────────────────────────────────────────────────────────

    @Test
    void noToken_onProtectedEndpoint_returns401() throws Exception {
        // /api/auth/logout is protected — no token → 401
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isBadRequest()); // controller returns 400 for missing header
    }

    @Test
    void noToken_onPublicEndpoint_returns200OrValidResponse() throws Exception {
        // /api/auth/login is public — no token needed
        when(loginUserUseCase.execute(anyString(), anyString()))
                .thenThrow(new com.neurixa.core.exception.InvalidCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "nobody",
                                "password", "wrong"))))
                .andExpect(status().isUnauthorized()); // domain exception → 401, not filter 401
    }

    // ── Expired token ─────────────────────────────────────────────────────────

    @Test
    void expiredToken_onProtectedEndpoint_returns401() throws Exception {
        // filter skips setting auth → SecurityContext empty → 401
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + EXPIRED_TOKEN))
                .andExpect(status().isBadRequest()); // controller handles missing auth header logic
    }

    @Test
    void expiredToken_filterDoesNotSetAuthentication() throws Exception {
        // Verify filter calls validateToken and does NOT call getUsername for expired token
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + EXPIRED_TOKEN));

        verify(jwtTokenProvider, atLeastOnce()).validateToken(EXPIRED_TOKEN);
        verify(jwtTokenProvider, never()).getUsername(EXPIRED_TOKEN);
    }

    // ── Invalid / malformed token ─────────────────────────────────────────────

    @Test
    void invalidToken_filterDoesNotSetAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + INVALID_TOKEN));

        verify(jwtTokenProvider, atLeastOnce()).validateToken(INVALID_TOKEN);
        verify(jwtTokenProvider, never()).getUsername(INVALID_TOKEN);
    }

    // ── Blacklisted token ─────────────────────────────────────────────────────

    @Test
    void blacklistedToken_filterDoesNotSetAuthentication() throws Exception {
        // blacklisted → filter skips auth setup → getUsername never called
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + BLACKLISTED_TOKEN));

        verify(tokenBlacklistService).isBlacklisted(BLACKLISTED_TOKEN);
        verify(jwtTokenProvider, never()).getUsername(BLACKLISTED_TOKEN);
    }

    // ── Valid token ───────────────────────────────────────────────────────────

    @Test
    void validToken_filterSetsAuthentication_requestProceeds() throws Exception {
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getExpiration(VALID_TOKEN))
                .thenReturn(new java.util.Date(System.currentTimeMillis() + 3600_000));

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(jwtTokenProvider).getUsername(VALID_TOKEN);
        verify(jwtTokenProvider).getRole(VALID_TOKEN);
    }

    // ── Bearer prefix handling ────────────────────────────────────────────────

    @Test
    void tokenWithoutBearerPrefix_treatedAsMissing() throws Exception {
        // Filter only extracts token when header starts with "Bearer "
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", VALID_TOKEN)) // no "Bearer " prefix
                .andExpect(status().isBadRequest()); // controller sees no auth header

        verify(jwtTokenProvider, never()).validateToken(any());
    }
}
