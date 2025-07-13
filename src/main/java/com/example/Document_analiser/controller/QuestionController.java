package com.example.Document_analiser.controller;

import com.example.Document_analiser.dto.AnswerResponse;
import com.example.Document_analiser.dto.QuestionRequest;
import com.example.Document_analiser.dto.QuestionHistoryDto;
import com.example.Document_analiser.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@RestController
@RequestMapping("/api")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
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
