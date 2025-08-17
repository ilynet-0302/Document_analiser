package com.example.Document_analiser.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Strategy for extracting raw text from an uploaded document.
 * Allows different implementations for various file formats
 * such as plain text, PDF, or DOCX.
 */
public interface DocumentTextExtractor {

    /**
     * @param file uploaded document
     * @return extracted textual content
     */
    String extract(MultipartFile file) throws IOException;

    /**
     * Determine whether this extractor can handle the provided file.
     */
    boolean supports(MultipartFile file);
}
