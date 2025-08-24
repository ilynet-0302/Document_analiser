package com.example.Document_analiser.service;

import com.example.Document_analiser.entity.Document;
import com.example.Document_analiser.entity.DocumentChunk;
import com.example.Document_analiser.exception.UnsupportedFileTypeException;
import com.example.Document_analiser.repository.DocumentRepository;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.BreakIterator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private static final int MAX_CHUNK_TOKENS = 512;

    private final DocumentRepository documentRepository;
    private final QuestionService.EmbeddingClient embeddingClient;
    private final List<DocumentTextExtractor> textExtractors;
    private final String embeddingModel;

    public DocumentService(DocumentRepository documentRepository,
                          QuestionService.EmbeddingClient embeddingClient,
                          List<DocumentTextExtractor> textExtractors,
                          @Value("${embedding.model}") String embeddingModel) {
        this.documentRepository = documentRepository;
        this.embeddingClient = embeddingClient;
        this.textExtractors = textExtractors;
        this.embeddingModel = embeddingModel;
    }

    @Timed(value = "document.store.time", description = "Time taken to store and process document")
    @CacheEvict(value = {"documents", "documentChunks"}, allEntries = true, cacheManager = "quickCacheManager")
    public void store(MultipartFile file) throws IOException {
        log.info("Storing document: {} (size: {} bytes)", file.getOriginalFilename(), file.getSize());
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        documentRepository.findByName(file.getOriginalFilename())
                .ifPresent(documentRepository::delete);

        String documentText = extractTextFromFile(file);
        log.debug("Extracted {} characters from document", documentText.length());
        
        List<String> chunks = chunkText(documentText, MAX_CHUNK_TOKENS);
        log.debug("Created {} chunks from document", chunks.size());

        Document document = new Document();
        document.setName(file.getOriginalFilename());
        document.setType(getFileExtension(file.getOriginalFilename()));
        document.setUploadDate(LocalDateTime.now());
        document.setContent(documentText);

        List<DocumentChunk> chunkEntities = new ArrayList<>();
        int index = 0;
        for (String chunk : chunks) {
            if (chunk.trim().isEmpty()) {
                log.warn("Skipping empty chunk {} for document {}", index, file.getOriginalFilename());
                continue;
            }
            float[] embedding = getCachedEmbedding(chunk);
            if (embedding == null) {
                log.warn("Embedding generation returned null for chunk {}", index);
                continue;
            }
            DocumentChunk dc = new DocumentChunk();
            dc.setChunkIndex(index);
            dc.setContent(chunk);
            dc.setEmbedding(embedding);
            dc.setDocument(document);
            chunkEntities.add(dc);
            index++;
        }
        document.setChunks(chunkEntities);
        documentRepository.save(document);
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        for (DocumentTextExtractor extractor : textExtractors) {
            if (extractor.supports(file)) {
                return extractor.extract(file);
            }
        }
        throw new UnsupportedFileTypeException(file.getContentType());
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        }
        return "unknown";
    }

    public List<String> chunkText(String content, int maxTokens) {
        List<String> chunks = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.getDefault());
        iterator.setText(content);
        StringBuilder current = new StringBuilder();
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String sentence = content.substring(start, end);
            if (current.length() + sentence.length() > maxTokens && current.length() > 0) {
                chunks.add(current.toString().trim());
                current.setLength(0);
            }
            current.append(sentence);
        }
        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }

    @Cacheable(value = "documents", cacheManager = "quickCacheManager")
    @Timed(value = "document.getAll.time", description = "Time taken to retrieve all documents")
    public List<Document> getAllDocuments() {
        log.debug("Retrieving all documents from database");
        return documentRepository.findAll();
    }

    /**
     * Gets cached embedding for text to avoid expensive recomputation.
     */
    @Cacheable(value = "embeddings", key = "#text.hashCode()", cacheManager = "embeddingCacheManager")
    @Timed(value = "embedding.generation.time", description = "Time taken to generate embeddings")
    private float[] getCachedEmbedding(String text) {
        log.debug("Generating embedding for chunk: {}", text.substring(0, Math.min(50, text.length())));
        try {
            return embeddingClient.embed(text, embeddingModel);
        } catch (Exception e) {
            log.error("Failed to generate embedding for chunk: {}", e.getMessage());
            return null;
        }
    }

    // TODO: Implement similarity search using stored chunk embeddings
    // public List<Document> findSimilarDocuments(String query, int limit) {
    //     // Placeholder for future implementation
    // }
}