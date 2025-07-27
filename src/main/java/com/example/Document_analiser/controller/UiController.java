package com.example.Document_analiser.controller;

import com.example.Document_analiser.dto.AnswerResponse;
import com.example.Document_analiser.dto.QuestionRequest;
import com.example.Document_analiser.service.DocumentService;
import com.example.Document_analiser.service.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class UiController {
    private final QuestionService questionService;
    private final DocumentService documentService;
    public UiController(QuestionService questionService, DocumentService documentService) {
        this.questionService = questionService;
        this.documentService = documentService;
    }

    @GetMapping("/ask")
    public String askForm(Model model) {
        model.addAttribute("questionRequest", new QuestionRequest());
        model.addAttribute("documents", documentService.getAllDocuments());
        return "ask";
    }

    @PostMapping("/ask")
    public String submitQuestion(@ModelAttribute QuestionRequest questionRequest, Model model) {
        AnswerResponse response = questionService.askQuestion(questionRequest);
        model.addAttribute("answer", response.getAnswer());
        model.addAttribute("documents", documentService.getAllDocuments());
        return "ask";
    }

} 