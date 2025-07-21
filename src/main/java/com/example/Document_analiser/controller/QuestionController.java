package com.example.Document_analiser.controller;

import com.example.Document_analiser.dto.AnswerResponse;
import com.example.Document_analiser.dto.QuestionRequest;
import com.example.Document_analiser.dto.QuestionHistoryDto;
import com.example.Document_analiser.dto.QuestionAnswerDto;
import com.example.Document_analiser.dto.QuestionUpdateRequest;
import com.example.Document_analiser.entity.Question;
import com.example.Document_analiser.repository.QuestionRepository;
import com.example.Document_analiser.service.QuestionService;
import com.example.Document_analiser.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api")
public class QuestionController {
    private final QuestionService questionService;
    private final AuthService authService;

    public QuestionController(QuestionService questionService, AuthService authService) {
        this.questionService = questionService;
        this.authService = authService;
    }

    @Operation(summary = "Ask a question about a document")
    @PostMapping("/questions")
    public ResponseEntity<AnswerResponse> ask(@RequestBody QuestionRequest request) {
        AnswerResponse response = questionService.askQuestion(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/questions/history")
    public java.util.List<QuestionHistoryDto> getHistory(@RequestParam(value = "documentId", required = false) Long documentId) {
        return questionService.getHistory(documentId);
    }

    @Operation(summary = "Edit a question (owner or admin only)", description = "Edit a question if you are the owner or have ADMIN role.")
    @PutMapping("/questions/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateQuestion(@PathVariable Long id, @RequestBody QuestionUpdateRequest request) {
        Question question = questionService.getQuestionRepository().findById(id).orElseThrow();
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = authService.isCurrentUserAdmin();
        if (!question.getUser().getUsername().equals(currentUser) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        question.setText(request.getNewText());
        questionService.getQuestionRepository().save(question);
        return ResponseEntity.ok("Question updated.");
    }

    @Operation(summary = "Delete a question (owner or admin only)", description = "Delete a question if you are the owner or have ADMIN role.")
    @DeleteMapping("/questions/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        Question question = questionService.getQuestionRepository().findById(id).orElseThrow();
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = authService.isCurrentUserAdmin();
        if (!question.getUser().getUsername().equals(currentUser) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        questionService.getQuestionRepository().delete(question);
        return ResponseEntity.ok("Question deleted.");
    }
}

@Controller
@RequestMapping("/history")
class QuestionHistoryPageController {
    private final QuestionService questionService;
    public QuestionHistoryPageController(QuestionService questionService) {
        this.questionService = questionService;
    }
    @GetMapping
    public String historyPage(@RequestParam(value = "documentId", required = false) Long documentId, Model model) {
        model.addAttribute("historyList", questionService.getHistory(documentId));
        return "history";
    }
}

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
