package com.example.Document_analiser.controller;

import com.example.Document_analiser.dto.AnswerResponse;
import com.example.Document_analiser.dto.QuestionHistoryDto;
import com.example.Document_analiser.dto.QuestionRequest;
import com.example.Document_analiser.service.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class UiController {
    private final QuestionService questionService;
    public UiController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/ask")
    public String askForm(Model model) {
        model.addAttribute("questionRequest", new QuestionRequest());
        return "ask";
    }

    @PostMapping("/ask")
    public String submitQuestion(@ModelAttribute QuestionRequest questionRequest, Model model) {
        AnswerResponse response = questionService.askQuestion(questionRequest);
        model.addAttribute("answer", response.getAnswer());
        return "ask";
    }

    @GetMapping("/history")
    public String viewHistory(Model model) {
        List<QuestionHistoryDto> history = questionService.getHistory(null);
        model.addAttribute("history", history);
        return "history";
    }
} 