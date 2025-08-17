package com.example.Document_analiser.service;

import com.example.Document_analiser.ChatClient;
import com.example.Document_analiser.dto.AnswerResponse;
import com.example.Document_analiser.dto.QuestionRequest;
import com.example.Document_analiser.dto.QuestionHistoryDto;
import com.example.Document_analiser.entity.Answer;
import com.example.Document_analiser.entity.Document;
import com.example.Document_analiser.entity.DocumentChunk;
import com.example.Document_analiser.entity.Question;
import com.example.Document_analiser.entity.User;
import com.example.Document_analiser.repository.DocumentRepository;
import com.example.Document_analiser.repository.DocumentChunkRepository;
import com.example.Document_analiser.repository.QuestionRepository;
import com.example.Document_analiser.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Handles question processing, semantic search and answer generation.
 */
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final AnswerService answerService;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;
    private final EmbeddingClient embeddingClient;
    private final DocumentChunkRepository documentChunkRepository;
    private final String systemPrompt;
    private final String answerInstruction;
    private final String examplePrompt;
    private final String embeddingModel;
    private static final int TOP_K = 5;

    public QuestionService(QuestionRepository questionRepository,
                          AnswerService answerService,
                          DocumentRepository documentRepository,
                          UserRepository userRepository,
                          ChatClient chatClient,
                          EmbeddingClient embeddingClient,
                          DocumentChunkRepository documentChunkRepository,
                          @Value("${prompt.system}") String systemPrompt,
                          @Value("${prompt.answer}") String answerInstruction,
                          @Value("${prompt.example}") String examplePrompt,
                          @Value("${embedding.model}") String embeddingModel) {
        this.questionRepository = questionRepository;
        this.answerService = answerService;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.chatClient = chatClient;
        this.embeddingClient = embeddingClient;
        this.documentChunkRepository = documentChunkRepository;
        this.systemPrompt = systemPrompt;
        this.answerInstruction = answerInstruction;
        this.examplePrompt = examplePrompt;
        this.embeddingModel = embeddingModel;
    }

    /**
     * Exposes the question repository mainly for testing purposes.
     *
     * @return the question repository
     */
    public QuestionRepository getQuestionRepository() {
        return questionRepository;
    }

    private PromptType determinePromptType(String questionText) {
        // Placeholder for future dynamic prompt selection logic
        return PromptType.GENERAL;
    }

    private enum PromptType {
        DEFINITION,
        EXPLANATION,
        COMPARISON,
        GENERAL
    }

    /**
     * Persists a question, retrieves relevant document chunks and generates an answer.
     *
     * @param request question payload containing text and document ID
     * @return answer response with generated text and timestamp
     */
    @Transactional
    public AnswerResponse askQuestion(QuestionRequest request) {
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Question question = new Question();
        question.setText(request.getText());
        question.setAskedAt(LocalDateTime.now());
        question.setDocument(document);
        question.setUser(user);
        question = questionRepository.save(question);

        PromptType promptType = determinePromptType(request.getText());

        float[] questionEmbedding = embeddingClient.embed(request.getText(), embeddingModel);
        String fallbackMessage = "Няма достатъчно данни в документа за отговор.";

        if (questionEmbedding == null) {
            return saveAnswer(question, fallbackMessage);
        }

        List<DocumentChunk> matches = findRelevantChunks(questionEmbedding, document.getId());
        if (matches == null || matches.isEmpty()) {
            return saveAnswer(question, fallbackMessage);
        }

        matches.sort(Comparator.comparingInt(DocumentChunk::getChunkIndex));

        StringBuilder contextBuilder = new StringBuilder("Document context:\n");
        int idx = 1;
        for (DocumentChunk chunk : matches) {
            String text = chunk.getContent();
            if (text != null && !text.isBlank()) {
                contextBuilder.append(idx++).append(". \"")
                        .append(text.trim())
                        .append("\"\n");
            }
        }
        if (idx == 1) {
            return saveAnswer(question, fallbackMessage);
        }
        contextBuilder.append("\n").append(examplePrompt).append("\n");
        contextBuilder.append("Question: \"")
                .append(request.getText())
                .append("\"\n\n")
                .append(answerInstruction);

        String userPrompt = contextBuilder.toString();

        String answerText = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        if (answerText == null || answerText.trim().isEmpty() || answerText.toLowerCase().contains("i don't know")) {
            answerText = fallbackMessage;
        }

        return saveAnswer(question, answerText);
    }

    private List<DocumentChunk> findRelevantChunks(float[] questionEmbedding, Long documentId) {
        return documentChunkRepository.findTopByCosineSimilarity(questionEmbedding, documentId, TOP_K);
    }

    /**
     * Retrieves paged question history for the given user and optional document filter.
     *
     * @param username   owner of the questions
     * @param documentId document id to filter by, may be {@code null}
     * @param page       page number starting from 0
     * @param size       page size
     * @param ascending  true for ascending order, false for descending
     * @return paged list of question history DTOs
     */
    public org.springframework.data.domain.Page<QuestionHistoryDto> getHistory(
            String username, Long documentId, int page, int size, boolean ascending) {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                ascending ? org.springframework.data.domain.Sort.Direction.ASC :
                        org.springframework.data.domain.Sort.Direction.DESC,
                "askedAt");
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size, sort);

        var questionsPage = questionRepository.findHistory(username, documentId, pageable);
        return questionsPage.map(q -> {
            QuestionHistoryDto dto = new QuestionHistoryDto();
            dto.setQuestionText(q.getText());
            dto.setAskedAt(q.getAskedAt());
            dto.setDocumentId(q.getDocument() != null ? q.getDocument().getId() : null);
            dto.setDocumentName(q.getDocument() != null ? q.getDocument().getName() : null);
            dto.setTopic(q.getTopic());
            if (q.getAnswer() != null) {
                dto.setAnswerText(q.getAnswer().getText());
                dto.setAnsweredAt(q.getAnswer().getGeneratedAt());
            }
            return dto;
        });
    }

    // Stub interface for embedding generation
    public interface EmbeddingClient {
        float[] embed(String text, String model);
    }

    private AnswerResponse saveAnswer(Question question, String answerText) {
        Answer answer = new Answer();
        answer.setText(answerText);
        answer.setGeneratedAt(LocalDateTime.now());
        answer.setQuestion(question);
        answerService.save(answer);

        AnswerResponse response = new AnswerResponse();
        response.setAnswer(answerText);
        response.setGeneratedAt(answer.getGeneratedAt());
        return response;
    }
}