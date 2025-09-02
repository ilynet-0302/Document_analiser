package com.example.Document_analiser;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Thin adapter over Spring AI's {@link ChatClient} to keep
 * the existing call chain used by QuestionService.
 */
@Component
public class AiChatClient {

    private final ChatClient ai;

    public AiChatClient(ChatClient.Builder builder) {
        this.ai = builder.build();
    }

    public ChatClientPrompt prompt() {
        return new ChatClientPrompt(ai);
    }

    public static class ChatClientPrompt {
        private final ChatClient ai;
        private String system;
        private String user;

        public ChatClientPrompt(ChatClient ai) {
            this.ai = ai;
        }

        public ChatClientPrompt system(String message) {
            this.system = message;
            return this;
        }
        public ChatClientPrompt user(String question) {
            this.user = question;
            return this;
        }
        public ChatClientPrompt call() {
            return this;
        }
        public String content() {
            return ai.prompt()
                    .system(system != null ? system : "")
                    .user(user != null ? user : "")
                    .call()
                    .content();
        }
    }
}
