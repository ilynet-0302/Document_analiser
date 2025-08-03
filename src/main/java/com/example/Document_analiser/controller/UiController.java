package com.example.Document_analiser.controller;

import com.example.Document_analiser.dto.DocumentUploadRequest;
import com.example.Document_analiser.dto.QuestionRequest;
import com.example.Document_analiser.service.DocumentService;
import com.example.Document_analiser.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UiController {
    private final QuestionService questionService;
    private final DocumentService documentService;
    public UiController(QuestionService questionService, DocumentService documentService) {
        this.questionService = questionService;
        this.documentService = documentService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/ask")
    public String askForm(Model model, @AuthenticationPrincipal User user) {
        QuestionRequest qr = (QuestionRequest) model.asMap().get("questionRequest");
        if (qr == null) {
            qr = new QuestionRequest();
        }
        Object selectedDoc = model.asMap().get("selectedDocumentId");
        if (selectedDoc instanceof Number) {
            qr.setDocumentId(((Number) selectedDoc).longValue());
        }
        model.addAttribute("questionRequest", qr);
        model.addAttribute("documents", documentService.getAllDocuments());
        model.addAttribute("username", user.getUsername());
        return "ask";
    }

    @PostMapping("/ask")
    public String submitQuestion(@Valid @ModelAttribute("questionRequest") QuestionRequest questionRequest,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("documents", documentService.getAllDocuments());
            return "ask";
        }
        try {
            var response = questionService.askQuestion(questionRequest);
            redirectAttributes.addFlashAttribute("answer", response.getAnswer());
            redirectAttributes.addFlashAttribute("selectedDocumentId", questionRequest.getDocumentId());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("status", "warning");
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("selectedDocumentId", questionRequest.getDocumentId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("status", "danger");
            redirectAttributes.addFlashAttribute("message", "Failed to get answer: " + e.getMessage());
            redirectAttributes.addFlashAttribute("selectedDocumentId", questionRequest.getDocumentId());
        }
        return "redirect:/ask";
    }

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        if (!model.containsAttribute("uploadRequest")) {
            model.addAttribute("uploadRequest", new DocumentUploadRequest());
        }
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@Valid @ModelAttribute("uploadRequest") DocumentUploadRequest uploadRequest,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors() || uploadRequest.getFile().isEmpty()) {
            model.addAttribute("status", "danger");
            model.addAttribute("message", "Please select a file");
            return "upload";
        }
        try {
            documentService.store(uploadRequest.getFile());
            redirectAttributes.addFlashAttribute("status", "success");
            redirectAttributes.addFlashAttribute("message", "Document uploaded successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("status", "danger");
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/upload";
    }

}