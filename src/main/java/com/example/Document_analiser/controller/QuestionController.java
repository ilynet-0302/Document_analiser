package com.example.Document_analiser.controller;

import com.example.Document_analiser.dto.AnswerResponse;
import com.example.Document_analiser.dto.QuestionRequest;
import com.example.Document_analiser.dto.QuestionHistoryDto;
import com.example.Document_analiser.dto.QuestionUpdateRequest;
import com.example.Document_analiser.entity.Question;
import com.example.Document_analiser.service.QuestionService;
import com.example.Document_analiser.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public java.util.List<QuestionHistoryDto> getHistory(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
            @RequestParam(value = "documentId", required = false) Long documentId,
            @RequestParam(value = "order", defaultValue = "asc") String order) {
        boolean asc = !"desc".equalsIgnoreCase(order);
        return questionService.getHistory(user.getUsername(), documentId, asc);
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