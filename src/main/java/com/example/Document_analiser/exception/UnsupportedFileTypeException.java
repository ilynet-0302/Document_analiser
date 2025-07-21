package com.example.Document_analiser.exception;

public class UnsupportedFileTypeException extends RuntimeException {
    public UnsupportedFileTypeException(String mimeType) {
        super("Unsupported file type: " + mimeType);
    }
} 