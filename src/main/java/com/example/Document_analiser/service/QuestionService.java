package com.example.Document_analiser.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Document_analiser.AiChatClient;
import com.example.Document_analiser.dto.AnswerResponse;
import com.example.Document_analiser.dto.QuestionHistoryDto;
import com.example.Document_analiser.dto.QuestionRequest;
import com.example.Document_analiser.entity.Answer;
import com.example.Document_analiser.entity.Document;
import com.example.Document_analiser.entity.DocumentChunk;
import com.example.Document_analiser.entity.Question;
import com.example.Document_analiser.entity.User;
import com.example.Document_analiser.repository.DocumentChunkRepository;
import com.example.Document_analiser.repository.DocumentRepository;
import com.example.Document_analiser.repository.QuestionRepository;
import com.example.Document_analiser.repository.UserRepository;

import io.micrometer.core.annotation.Timed;

/**
 * Handles question processing, semantic search and answer generation.
 * Optimized with caching and performance monitoring.
 */
@Service
public class QuestionService {
    
    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);
    private final QuestionRepository questionRepository;
    private final AnswerService answerService;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final AiChatClient chatClient;
    private final EmbeddingClient embeddingClient;
    private final DocumentChunkRepository documentChunkRepository;
    private final VectorSearchService vectorSearchService;
    private final LogAnalysisService logAnalysisService;
    private final String systemPrompt;
    private final String answerInstruction;
    private final String examplePrompt;
    private final String embeddingModel;
    private static final int TOP_K = 5;

    public QuestionService(QuestionRepository questionRepository,
                          AnswerService answerService,
                          DocumentRepository documentRepository,
                          UserRepository userRepository,
                          AiChatClient chatClient,
                          EmbeddingClient embeddingClient,
                          DocumentChunkRepository documentChunkRepository,
                          VectorSearchService vectorSearchService,
                          LogAnalysisService logAnalysisService,
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
        this.vectorSearchService = vectorSearchService;
        this.logAnalysisService = logAnalysisService;
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

    /**
     * Persists a question, retrieves relevant document chunks and generates an answer.
     * Optimized with performance monitoring and caching for embeddings.
     *
     * @param request question payload containing text and document ID
     * @return answer response with generated text and timestamp
     */
    @Timed(value = "question.processing.time", description = "Time taken to process a question")
    public AnswerResponse askQuestion(QuestionRequest request) {
        log.debug("Processing question: {} for document: {}", request.getText(), request.getDocumentId());
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

        float[] questionEmbedding = getCachedEmbedding(request.getText());
        String fallbackMessage = "Няма достатъчно данни в документа за отговор.";

        if (questionEmbedding == null) {
            log.warn("Failed to generate embedding for question: {}", request.getText());
            return saveAnswer(question, fallbackMessage);
        }

        List<DocumentChunk> matches = findRelevantChunks(questionEmbedding, document.getId());
        if (matches == null || matches.isEmpty()) {
            log.debug("No relevant chunks found for question: {}", request.getText());
            return saveAnswer(question, fallbackMessage);
        }

        log.debug("Found {} relevant chunks for question", matches.size());
        matches.sort(Comparator.comparingInt(DocumentChunk::getChunkIndex));

        String contextPrompt = buildContextPrompt(matches, request.getText());
        if (contextPrompt == null) {
            return saveAnswer(question, fallbackMessage);
        }

        String answerText = generateAnswer(contextPrompt);
        if (answerText == null || answerText.trim().isEmpty() || answerText.toLowerCase().contains("i don't know")) {
            answerText = fallbackMessage;
        }

        log.debug("Generated answer for question: {}", request.getText());
        return saveAnswer(question, answerText);
    }

    private List<DocumentChunk> findRelevantChunks(float[] questionEmbedding, Long documentId) {
        try {
            return vectorSearchService.findTopByCosineSimilarity(questionEmbedding, documentId, TOP_K);
        } catch (Exception e) {
            log.warn("Vector SQL search failed, falling back to in-memory similarity: {}", e.getMessage());
            try {
                var pageable = org.springframework.data.domain.PageRequest.of(0, 1000);
                List<DocumentChunk> all = documentChunkRepository.findByDocumentIdOrderByChunkIndex(documentId, pageable);
                List<DocumentChunk> ranked = all.stream()
                        .filter(dc -> dc.getEmbedding() != null)
                        .sorted((a, b) -> Float.compare(
                                cosineSimilarity(b.getEmbedding(), questionEmbedding),
                                cosineSimilarity(a.getEmbedding(), questionEmbedding)))
                        .limit(TOP_K)
                        .toList();
                if (ranked.isEmpty() && !all.isEmpty()) {
                    // Last-resort: take first K chunks by order if embeddings are unavailable
                    log.debug("Falling back to first {} chunks by order", TOP_K);
                    return all.stream()
                            .limit(TOP_K)
                            .toList();
                }
                return ranked;
            } catch (Exception ex) {
                log.error("Fallback similarity search failed: {}", ex.getMessage());
                return java.util.Collections.emptyList();
            }
        }
    }

    private float cosineSimilarity(float[] v1, float[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) return -1f;
        double dot = 0, n1 = 0, n2 = 0;
        for (int i = 0; i < v1.length; i++) {
            float a = v1[i];
            float b = v2[i];
            dot += a * b;
            n1 += a * a;
            n2 += b * b;
        }
        double denom = Math.sqrt(n1) * Math.sqrt(n2);
        return denom == 0 ? -1f : (float)(dot / denom);
    }

    /**
     * Retrieves paged question history for the given user and optional document filter.
     * Cached for improved performance on repeated requests.
     *
     * @param username   owner of the questions
     * @param documentId document id to filter by, may be {@code null}
     * @param page       page number starting from 0
     * @param size       page size
     * @param ascending  true for ascending order, false for descending
     * @return paged list of question history DTOs
     */
    @Cacheable(value = "questionHistory", key = "#username + '_' + #documentId + '_' + #page + '_' + #size + '_' + #ascending", 
               cacheManager = "quickCacheManager")
    @Timed(value = "question.history.time", description = "Time taken to retrieve question history")
    public org.springframework.data.domain.Page<QuestionHistoryDto> getHistory(
            String username, Long documentId, int page, int size, boolean ascending) {
        log.debug("Retrieving question history for user: {}, document: {}, page: {}", username, documentId, page);
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

    /**
     * Gets cached embedding for text to avoid expensive recomputation.
     */
    @Cacheable(value = "embeddings", key = "#text.hashCode()", cacheManager = "embeddingCacheManager")
    @Timed(value = "embedding.generation.time", description = "Time taken to generate embeddings")
    private float[] getCachedEmbedding(String text) {
        log.debug("Generating embedding for text: {}", text.substring(0, Math.min(50, text.length())));
        try {
            return embeddingClient.embed(text, embeddingModel);
        } catch (Exception e) {
            log.error("Failed to generate embedding for text: {}", e.getMessage());
            logAnalysisService.recordError("embedding", "Failed to generate embedding", e);
            return null;
        }
    }

    /**
     * Builds context prompt from document chunks.
     */
    private String buildContextPrompt(List<DocumentChunk> matches, String questionText) {
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
            log.debug("No valid chunks found for context building");
            return null;
        }
        
        contextBuilder.append("\n").append(examplePrompt).append("\n");
        contextBuilder.append("Question: \"")
                .append(questionText)
                .append("\"\n\n")
                .append(answerInstruction);

        return contextBuilder.toString();
    }

    /**
     * Generates answer using AI client with performance monitoring.
     */
    @Timed(value = "ai.answer.generation.time", description = "Time taken for AI to generate answer")
    private String generateAnswer(String contextPrompt) {
        log.debug("Generating AI answer");
        try {
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(contextPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Failed to generate AI answer: {}", e.getMessage());
            logAnalysisService.recordError("ai_generation", "Failed to generate AI answer", e);
            return null;
        }
    }
}
