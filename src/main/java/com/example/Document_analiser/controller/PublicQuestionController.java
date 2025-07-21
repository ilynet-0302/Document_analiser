package com.example.Document_analiser.controller;

import com.example.Document_analiser.dto.QuestionAnswerDto;
import com.example.Document_analiser.entity.Question;
import com.example.Document_analiser.repository.QuestionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Public Q&A API", description = "Public endpoints for searching questions and answers")
@RestController
@RequestMapping("/public")
public class PublicQuestionController {
    private final QuestionRepository questionRepository;
    public PublicQuestionController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Operation(summary = "Get question and answer by ID", description = "Returns a specific Q&A entry by ID without authentication.")
    @GetMapping("/question/{id}")
    public ResponseEntity<QuestionAnswerDto> getById(@PathVariable Long id) {
        Question q = questionRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(mapToDto(q));
    }

    @Operation(summary = "Search questions and answers by text", description = "Returns a list of Q&A entries matching the query in question or answer text.")
    @GetMapping("/search")
    public List<QuestionAnswerDto> search(@RequestParam String query) {
        return questionRepository
            .findByTextContainingIgnoreCaseOrAnswer_TextContainingIgnoreCase(query, query)
            .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Operation(summary = "Get questions and answers by topic", description = "Returns a list of Q&A entries for a given topic.")
    @GetMapping("/topic/{topic}")
    public List<QuestionAnswerDto> getByTopic(@PathVariable String topic) {
        return questionRepository.findByTopicIgnoreCase(topic)
            .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private QuestionAnswerDto mapToDto(Question q) {
        QuestionAnswerDto dto = new QuestionAnswerDto();
        dto.setId(q.getId());
        dto.setQuestion(q.getText());
        dto.setAskedAt(q.getAskedAt());
        dto.setTopic(q.getTopic());
        if (q.getDocument() != null) {
            dto.setType(q.getDocument().getType());
        }
        if (q.getAnswer() != null) {
            dto.setAnswer(q.getAnswer().getText());
            dto.setAnsweredAt(q.getAnswer().getGeneratedAt());
        }
        return dto;
    }
} 