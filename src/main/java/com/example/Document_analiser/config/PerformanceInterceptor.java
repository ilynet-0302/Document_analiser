package com.example.Document_analiser.config;

import com.example.Document_analiser.service.ResponseTimeAnalyzer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.time.Instant;

/**
 * Interceptor to automatically track response times for all endpoints.
 */
@Component
public class PerformanceInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(PerformanceInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "startTime";
    
    private final ResponseTimeAnalyzer responseTimeAnalyzer;

    public PerformanceInterceptor(ResponseTimeAnalyzer responseTimeAnalyzer) {
        this.responseTimeAnalyzer = responseTimeAnalyzer;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, Instant.now());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) {
        Instant startTime = (Instant) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            Duration duration = Duration.between(startTime, Instant.now());
            String endpoint = getEndpointName(request);
            
            responseTimeAnalyzer.recordResponseTime(endpoint, duration);
            
            // Log slow requests
            long durationMs = duration.toMillis();
            if (durationMs > 2000) {
                log.warn("Slow request detected: {} {} took {} ms", 
                    request.getMethod(), endpoint, durationMs);
            } else if (durationMs > 1000) {
                log.info("Request: {} {} took {} ms", 
                    request.getMethod(), endpoint, durationMs);
            } else {
                log.debug("Request: {} {} took {} ms", 
                    request.getMethod(), endpoint, durationMs);
            }
        }
    }

    private String getEndpointName(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        // Normalize URIs with IDs to avoid creating too many metrics
        uri = normalizeUri(uri);
        
        return method + " " + uri;
    }

    private String normalizeUri(String uri) {
        // Replace numeric IDs with placeholder
        uri = uri.replaceAll("/\\d+", "/{id}");
        
        // Replace UUIDs with placeholder
        uri = uri.replaceAll("/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", "/{uuid}");
        
        return uri;
    }
}