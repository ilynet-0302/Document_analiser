package com.example.Document_analiser.controller;

import com.example.Document_analiser.entity.Document;
import com.example.Document_analiser.service.DocumentService;
import com.example.Document_analiser.service.QuestionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/history")
public class QuestionHistoryPageController {
    private final QuestionService questionService;
    private final DocumentService documentService;

    public QuestionHistoryPageController(QuestionService questionService, DocumentService documentService) {
        this.questionService = questionService;
        this.documentService = documentService;
    }

    @GetMapping
    public String historyPage(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "documentId", required = false) Long documentId,
            @RequestParam(value = "order", defaultValue = "desc") String order,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        boolean asc = !"desc".equalsIgnoreCase(order);
        var historyPage = questionService.getHistory(user.getUsername(), documentId, page, 10, asc);
        List<Document> docs = documentService.getAllDocuments();
        model.addAttribute("historyPage", historyPage);
        model.addAttribute("documents", docs);
        model.addAttribute("currentOrder", asc ? "asc" : "desc");
        model.addAttribute("selectedDocumentId", documentId);
        return "history";
    }
}