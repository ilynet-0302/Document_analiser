package com.example.Document_analiser.service;

import com.example.Document_analiser.entity.DocumentChunk;
import com.example.Document_analiser.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VectorSearchService {

    private final DocumentChunkRepository documentChunkRepository;

    public VectorSearchService(DocumentChunkRepository documentChunkRepository) {
        this.documentChunkRepository = documentChunkRepository;
    }

    // Run vector SQL in a separate, read-only transaction so failures
    // don't mark outer transactions as rollback-only
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<DocumentChunk> findTopByCosineSimilarity(float[] embedding, Long documentId, int limit) {
        return documentChunkRepository.findTopByCosineSimilarity(toPgVectorLiteral(embedding), documentId, limit);
    }

    private String toPgVectorLiteral(float[] v) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(',');
            // Use plain decimals; pgvector ignores extra precision
            sb.append(Float.toString(v[i]));
        }
        sb.append("]");
        return sb.toString();
    }
}
