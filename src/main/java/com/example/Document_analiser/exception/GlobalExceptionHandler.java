package com.example.Document_analiser.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<String> handleAuthException(AuthenticationCredentialsNotFoundException ex) {
        String cid = MDC.get("cid");
        log.warn("401 Unauthorized [cid={}] {}", cid, ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        if (cid != null) headers.add("X-Correlation-Id", cid);
        return new ResponseEntity<>("Unauthorized: " + ex.getMessage(), headers, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        String cid = MDC.get("cid");
        log.warn("403 Forbidden [cid={}] {}", cid, ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        if (cid != null) headers.add("X-Correlation-Id", cid);
        return new ResponseEntity<>("Forbidden: " + ex.getMessage(), headers, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception ex) {
        String cid = MDC.get("cid");
        log.error("500 Error [cid={}]", cid, ex);
        HttpHeaders headers = new HttpHeaders();
        if (cid != null) headers.add("X-Correlation-Id", cid);
        return new ResponseEntity<>("Error: " + ex.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResource(NoResourceFoundException ex) {
        String cid = MDC.get("cid");
        log.info("404 Not Found [cid={}] {}", cid, ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        if (cid != null) headers.add("X-Correlation-Id", cid);
        return new ResponseEntity<>("Not Found", headers, HttpStatus.NOT_FOUND);
    }
} 
