package com.example.Document_analiser.dto;

import java.time.LocalDateTime;

public class QuestionAnswerDto {
    private Long id;
    private String question;
    private String answer;
    private LocalDateTime askedAt;
    private LocalDateTime answeredAt;
    private String topic;
    private String type;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public LocalDateTime getAskedAt() { return askedAt; }
    public void setAskedAt(LocalDateTime askedAt) { this.askedAt = askedAt; }
    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
} 