package com.neurixa.config.security;

import com.neurixa.core.domain.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (tokenBlacklistService.isBlacklisted(token)) {
                    log.warn("auth=rejected reason=blacklisted path={}", request.getRequestURI());
                } else if (!tokenProvider.validateToken(token)) {
                    log.warn("auth=rejected reason=invalid_or_expired path={}", request.getRequestURI());
                } else {
                    String username = tokenProvider.getUsername(token);
                    Role role = tokenProvider.getRole(token);

                    if (role == null) {
                        log.warn("auth=rejected reason=unknown_role username={} path={}", username, request.getRequestURI());
                    } else {
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("auth=accepted username={} role={} path={}", username, role, request.getRequestURI());
                    }
                }
            }
        } catch (Exception e) {
            log.error("auth=error reason=exception path={} message={}", request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
