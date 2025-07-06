package com.example.Document_analiser.dto;

import java.time.LocalDateTime;

public class AnswerResponse {
    private String answer;
    private LocalDateTime generatedAt;

    // Getters and Setters
    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
} 