package com.example.Document_analiser.service;

import com.example.Document_analiser.entity.Document;
import com.example.Document_analiser.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class DocumentService {
    
    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public void store(MultipartFile file) throws IOException {
        // Extract text from file (currently supporting .txt files)
        String documentText = extractTextFromFile(file);
        
        // Create and save Document entity
        Document document = new Document();
        document.setName(file.getOriginalFilename());
        document.setType(getFileExtension(file.getOriginalFilename()));
        document.setUploadDate(LocalDateTime.now());
        document.setContent(documentText);
        
        Document savedDocument = documentRepository.save(document);
        
        // TODO: Add embedding generation and vector storage
        // var embedding = embeddingClient.embed(documentText);
        // vectorStore.add(List.of(
        //     new Vector(embedding, Map.of("documentId", savedDocument.getId().toString()))
        // ));
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        // For now, only supporting .txt files
        // TODO: Add support for .pdf and .docx using Apache Tika
        if (file.getOriginalFilename() != null && 
            file.getOriginalFilename().toLowerCase().endsWith(".txt")) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } else {
            throw new IllegalArgumentException("Currently only .txt files are supported");
        }
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        }
        return "unknown";
    }

    // TODO: Implement similarity search
    // public List<Document> findSimilarDocuments(String query, int limit) {
    //     var queryEmbedding = embeddingClient.embed(query);
    //     var results = vectorStore.search(SearchRequest.query(queryEmbedding).withTopK(limit));
    //     // Process results and return documents
    // }
} 