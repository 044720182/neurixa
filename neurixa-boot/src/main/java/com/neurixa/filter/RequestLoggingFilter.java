package com.neurixa.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Adds a correlation ID to every request via MDC, and logs
 * incoming method+path and outgoing status+duration.
 *
 * MDC keys available in all downstream log statements:
 *   correlationId  — unique per request (UUID)
 *   user           — authenticated username, or "anonymous"
 */
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_USER = "user";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String correlationId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        MDC.put(MDC_CORRELATION_ID, correlationId);

        // User is not yet set by Spring Security at this point — set after filter chain
        String method = request.getMethod();
        String path   = request.getRequestURI();
        long start    = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            String principal = request.getUserPrincipal() != null
                    ? request.getUserPrincipal().getName()
                    : "anonymous";
            MDC.put(MDC_USER, principal);

            int status = response.getStatus();
            if (status >= 500) {
                log.error("{} {} → {} ({}ms) user={}", method, path, status, duration, principal);
            } else if (status >= 400) {
                log.warn("{} {} → {} ({}ms) user={}", method, path, status, duration, principal);
            } else {
                log.info("{} {} → {} ({}ms) user={}", method, path, status, duration, principal);
            }

            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_USER);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip actuator health/info — too noisy
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") || path.startsWith("/actuator/info");
    }
}
