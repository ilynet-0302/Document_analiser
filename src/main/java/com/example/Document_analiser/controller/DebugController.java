package com.example.Document_analiser.controller;

import com.example.Document_analiser.repository.DocumentChunkRepository;
import com.example.Document_analiser.repository.projection.ChunkEmbeddingView;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private final DocumentChunkRepository documentChunkRepository;

    public DebugController(DocumentChunkRepository documentChunkRepository) {
        this.documentChunkRepository = documentChunkRepository;
    }

    @GetMapping("/document/{id}/stats")
    public ResponseEntity<Map<String, Object>> documentStats(@PathVariable("id") Long documentId) {
        var pageable = PageRequest.of(0, 10_000);
        List<ChunkEmbeddingView> views;
        try {
            views = documentChunkRepository.findEmbeddingsByDocumentId(documentId, pageable);
        } catch (Exception e) {
            // Fallback: load entities (LOB is LAZY so should be fine)
            var chunks = documentChunkRepository.findByDocumentIdOrderByChunkIndex(documentId, pageable);
            Map<String, Object> out = new HashMap<>();
            out.put("documentId", documentId);
            out.put("chunksTotal", chunks.size());
            long withEmb = chunks.stream().filter(c -> c.getEmbedding() != null).count();
            out.put("withEmbedding", withEmb);
            out.put("withoutEmbedding", chunks.size() - withEmb);
            out.put("sampleChunkIndexes", chunks.stream().limit(5).map(c -> c.getChunkIndex()).toList());
            return ResponseEntity.ok(out);
        }

        long withEmb = views.stream().filter(v -> v.getEmbedding() != null).count();
        Map<String, Object> out = new HashMap<>();
        out.put("documentId", documentId);
        out.put("chunksTotal", views.size());
        out.put("withEmbedding", withEmb);
        out.put("withoutEmbedding", views.size() - withEmb);
        out.put("sampleIds", views.stream().limit(5).map(ChunkEmbeddingView::getId).toList());
        out.put("sampleIndexes", views.stream().limit(5).map(ChunkEmbeddingView::getChunkIndex).toList());
        return ResponseEntity.ok(out);
    }
}

