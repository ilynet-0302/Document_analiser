package com.example.Document_analiser.controller;

import com.example.Document_analiser.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST controller for managing document uploads.
 */
@RestController
@RequestMapping("/api")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Uploads a document and stores its contents.
     *
     * @param file text document to upload
     * @return confirmation message
     * @throws IOException if storing the document fails
     */
    @PostMapping("/documents")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        documentService.store(file);
        return ResponseEntity.ok("Document uploaded.");
    }
}