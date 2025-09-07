package com.example.Document_analiser.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Traces each HTTP request early in the chain with a correlation id.
 * Logs start/end, method, URI, status, auth user, and latency.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTracingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestTracingFilter.class);
    private static final String CID = "cid";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String correlationId = getOrCreateCorrelationId(request);
        MDC.put(CID, correlationId);
        response.setHeader("X-Correlation-Id", correlationId);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String fullUri = query == null ? uri : uri + "?" + query;
        String remote = request.getRemoteAddr();

        Instant start = Instant.now();
        log.debug("--> {} {} from {}", method, fullUri, remote);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long ms = Duration.between(start, Instant.now()).toMillis();
            int status = response.getStatus();
            String user = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";

            if (status >= 500) {
                log.error("<-- {} {} {} in {} ms user={}", method, fullUri, status, ms, user);
            } else if (status >= 400) {
                log.warn("<-- {} {} {} in {} ms user={}", method, fullUri, status, ms, user);
            } else {
                log.info("<-- {} {} {} in {} ms user={}", method, fullUri, status, ms, user);
            }
            MDC.remove(CID);
        }
    }

    private String getOrCreateCorrelationId(HttpServletRequest request) {
        String fromHeader = request.getHeader("X-Correlation-Id");
        if (fromHeader != null && !fromHeader.isBlank()) {
            return fromHeader.trim();
        }
        return UUID.randomUUID().toString();
    }
}

