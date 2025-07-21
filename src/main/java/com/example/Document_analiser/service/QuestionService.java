package com.example.Document_analiser.service;

import com.example.Document_analiser.ChatClient;
import com.example.Document_analiser.dto.AnswerResponse;
import com.example.Document_analiser.dto.QuestionRequest;
import com.example.Document_analiser.dto.QuestionHistoryDto;
import com.example.Document_analiser.entity.Answer;
import com.example.Document_analiser.entity.Document;
import com.example.Document_analiser.entity.Question;
import com.example.Document_analiser.entity.User;
import com.example.Document_analiser.repository.AnswerRepository;
import com.example.Document_analiser.repository.DocumentRepository;
import com.example.Document_analiser.repository.QuestionRepository;
import com.example.Document_analiser.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;
    private final EmbeddingClient embeddingClient;
    private final VectorStore vectorStore;

    public QuestionService(QuestionRepository questionRepository,
                          AnswerRepository answerRepository,
                          DocumentRepository documentRepository,
                          UserRepository userRepository,
                          ChatClient chatClient,
                          EmbeddingClient embeddingClient,
                          VectorStore vectorStore) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.chatClient = chatClient;
        this.embeddingClient = embeddingClient;
        this.vectorStore = vectorStore;
    }

    public QuestionRepository getQuestionRepository() {
        return questionRepository;
    }

    @Transactional
    public AnswerResponse askQuestion(QuestionRequest request) {
        // Validate document existence
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        // Get current user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Save question
        Question question = new Question();
        question.setText(request.getText());
        question.setAskedAt(LocalDateTime.now());
        question.setDocument(document);
        question.setUser(user);
        question = questionRepository.save(question);

        // Generate embedding for the question
        float[] questionEmbedding = embeddingClient.embed(request.getText());

        // Similarity search (top 5)
        List<Vector> matches = vectorStore.similaritySearch(questionEmbedding, 5);
        String contextChunks = matches.stream()
                .map(v -> v.getMetadata().get("content"))
                .collect(Collectors.joining("\n---\n"));

        // Prompt engineering
        String systemPrompt = "You are an intelligent assistant that answers questions based only on provided context. " +
                "If the context does not contain enough information, say 'I don't know based on the document'.";

        String fewShotExample = "Example:\nQ: What is the main topic of the document?\nA: The document discusses network security protocols.\n";

        String structuredPrompt = systemPrompt + "\n\n" +
                "[CONTEXT]\n" + contextChunks + "\n[/CONTEXT]\n\n" +
                "[QUESTION]\n" + request.getText() + "\n[/QUESTION]\n\n" +
                fewShotExample +
                "Now answer this question:\nQ: " + request.getText() + "\nA:";

        // Call GPT-4 via ChatClient
        String answerText = chatClient.prompt()
                .user(structuredPrompt)
                .call()
                .content();

        // Fallback if model does not answer confidently
        if (answerText == null || answerText.trim().isEmpty() || answerText.toLowerCase().contains("i don't know")) {
            answerText = "We could not confidently answer your question based on the document.";
        }

        // Save answer
        Answer answer = new Answer();
        answer.setText(answerText);
        answer.setGeneratedAt(LocalDateTime.now());
        answer.setQuestion(question);
        answerRepository.save(answer);

        // Prepare response
        AnswerResponse response = new AnswerResponse();
        response.setAnswer(answerText);
        response.setGeneratedAt(answer.getGeneratedAt());
        return response;
    }

    public List<QuestionHistoryDto> getHistory(Long documentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Question> questions = (documentId == null)
            ? questionRepository.findAllByUserUsername(username)
            : questionRepository.findAllByUserUsername(username).stream().filter(q -> q.getDocument().getId().equals(documentId)).toList();
        return questions.stream()
                .sorted(Comparator.comparing(Question::getAskedAt).reversed())
                .map(q -> {
                    QuestionHistoryDto dto = new QuestionHistoryDto();
                    dto.setQuestionText(q.getText());
                    dto.setAskedAt(q.getAskedAt());
                    dto.setDocumentId(q.getDocument() != null ? q.getDocument().getId() : null);
                    if (q.getAnswer() != null) {
                        dto.setAnswerText(q.getAnswer().getText());
                        dto.setAnsweredAt(q.getAnswer().getGeneratedAt());
                    }
                    return dto;
                })
                .toList();
    }

    // Stub interfaces/classes for embedding and vector search
    public interface EmbeddingClient {
        float[] embed(String text);
    }
    public interface VectorStore {
        List<Vector> similaritySearch(float[] embedding, int topK);
        void add(List<Vector> vectors, float[] embedding);
    }
    public static class Vector {
        private final Map<String, String> metadata;
        public Vector(Map<String, String> metadata) {
            this.metadata = metadata;
        }
        public Map<String, String> getMetadata() {
            return metadata;
        }
    }
} 