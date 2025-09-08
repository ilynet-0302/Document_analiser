package com.example.Document_analiser.service;

import com.example.Document_analiser.entity.DocumentChunk;
import com.example.Document_analiser.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Служба за векторно търсене върху pgvector.
 *
 * - Какво прави: намира най-близките по косинусова близост chunk-ове спрямо embedding.
 * - Как: първо взима само идентификатори по векторен ранг (бързо), после зарежда
 *   съдържанието чрез проекции, за да избегне четене на тежката vector колона.
 */
@Service
public class VectorSearchService {

    private final DocumentChunkRepository documentChunkRepository;

    public VectorSearchService(DocumentChunkRepository documentChunkRepository) {
        this.documentChunkRepository = documentChunkRepository;
    }

    /**
     * Стартира векторния SQL в отделна, read-only транзакция, за да не маркира
     * външни транзакции като rollback-only при грешка.
     *
     * @param embedding вектор на заявката
     * @param documentId филтър по документ (или null за глобално търсене)
     * @param limit брой резултати
     * @return подреден списък от най-подходящи chunk-ове по ранг
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<DocumentChunk> findTopByCosineSimilarity(float[] embedding, Long documentId, int limit) {
        var ids = documentChunkRepository.findTopIdsByCosineSimilarity(toPgVectorLiteral(embedding), documentId, limit);
        if (ids == null || ids.isEmpty()) return java.util.Collections.emptyList();
        // Fetch only content via projection to avoid reading vector column
        var views = documentChunkRepository.findContentByIdIn(ids);
        // Convert to lightweight DocumentChunk instances for downstream code
        java.util.Map<Long, com.example.Document_analiser.repository.projection.ChunkContentView> byId = new java.util.HashMap<>();
        for (var v : views) byId.put(v.getId(), v);
        java.util.List<DocumentChunk> out = new java.util.ArrayList<>();
        for (Long id : ids) {
            var v = byId.get(id);
            if (v != null) {
                DocumentChunk dc = new DocumentChunk();
                dc.setId(v.getId());
                dc.setChunkIndex(v.getChunkIndex());
                dc.setContent(v.getContent());
                out.add(dc);
            }
        }
        return out;
    }

    /** Конвертира float[] към pgvector литерал за SQL (например "[0.1,0.2,...]"). */
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
