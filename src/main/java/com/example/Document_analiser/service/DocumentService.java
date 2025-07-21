package com.example.Document_analiser.service;

import com.example.Document_analiser.entity.Document;
import com.example.Document_analiser.repository.DocumentRepository;
import com.example.Document_analiser.exception.UnsupportedFileTypeException;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final QuestionService.EmbeddingClient embeddingClient;
    private final QuestionService.VectorStore vectorStore;

    public DocumentService(DocumentRepository documentRepository,
                          QuestionService.EmbeddingClient embeddingClient,
                          QuestionService.VectorStore vectorStore) {
        this.documentRepository = documentRepository;
        this.embeddingClient = embeddingClient;
        this.vectorStore = vectorStore;
    }

    public void store(MultipartFile file) throws IOException {
        // Ограничение за размер (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }
        String documentText = extractTextFromFile(file);

        // Chunk-ване на текста (200 думи на chunk)
        List<String> chunks = chunkText(documentText, 200);

        // Създаване и запис на Document
        Document document = new Document();
        document.setName(file.getOriginalFilename());
        document.setType(getFileExtension(file.getOriginalFilename()));
        document.setUploadDate(LocalDateTime.now());
        document.setContent(documentText);
        Document savedDocument = documentRepository.save(document);

        // Индексиране на всеки chunk във векторната база
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            float[] embedding = embeddingClient.embed(chunk);
            vectorStore.add(List.of(new QuestionService.Vector(
                Map.of(
                    "documentId", savedDocument.getId().toString(),
                    "chunkIndex", String.valueOf(i),
                    "content", chunk
                )
            )), embedding);
        }
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        Tika tika = new Tika();
        String mimeType = tika.detect(file.getInputStream());
        if (!mimeType.startsWith("application/") && !mimeType.startsWith("text/")) {
            throw new UnsupportedFileTypeException(mimeType);
        }
        try {
            return tika.parseToString(file.getInputStream());
        } catch (TikaException e) {
            throw new IOException("Failed to extract text from file", e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        }
        return "unknown";
    }

    public List<String> chunkText(String content, int maxTokens) {
        List<String> chunks = new ArrayList<>();
        String[] words = content.split("\\s+");
        for (int i = 0; i < words.length; i += maxTokens) {
            int end = Math.min(i + maxTokens, words.length);
            chunks.add(String.join(" ", Arrays.copyOfRange(words, i, end)));
        }
        return chunks;
    }

    // TODO: Implement similarity search
    // public List<Document> findSimilarDocuments(String query, int limit) {
    //     var queryEmbedding = embeddingClient.embed(query);
    //     var results = vectorStore.search(SearchRequest.query(queryEmbedding).withTopK(limit));
    //     // Process results and return documents
    // }
} 