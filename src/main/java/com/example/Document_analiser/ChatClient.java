package com.example.Document_analiser;

public class ChatClient {
    public ChatClientPrompt prompt() {
        return new ChatClientPrompt();
    }

    public static class ChatClientPrompt {
        public ChatClientPrompt system(String message) {
            return this;
        }
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