package com.example.Document_analiser.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Extracts text from plain `.txt` files.
 */
@Component
public class TxtDocumentTextExtractor implements DocumentTextExtractor {

    @Override
    public String extract(MultipartFile file) throws IOException {
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public boolean supports(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name != null && name.toLowerCase().endsWith(".txt");
    }
}