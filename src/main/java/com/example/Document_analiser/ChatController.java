package com.example.Document_analiser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private final AiChatClient chatClient;
    private final String systemPrompt;

    public ChatController(AiChatClient chatClient,
                          @Value("${prompt.system}") String systemPrompt) {
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt;
    }

    @PostMapping("/api/ask")
    public String ask(@RequestBody String question) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .call()
                .content();
    }
}
