package com.example.Document_analiser;

public class ChatClient {
    public ChatClientPrompt prompt() {
        return new ChatClientPrompt();
    }

    public static class ChatClientPrompt {
        public ChatClientPrompt user(String question) {
            return this;
        }
        public ChatClientPrompt call() {
            return this;
        }
        public String content() {
            return "Dummy response";
        }
    }
} 