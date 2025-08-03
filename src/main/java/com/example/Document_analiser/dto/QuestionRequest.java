package com.example.Document_analiser.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class QuestionRequest {

    @NotBlank(message = "Question cannot be empty")
    @Size(max = 500, message = "Question must be at most 500 characters")
    private String text;

    @NotNull(message = "Please select a document")
    private Long documentId;

    // Getters and Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
}