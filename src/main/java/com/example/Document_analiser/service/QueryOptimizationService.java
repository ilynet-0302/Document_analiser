package com.example.Document_analiser.service;

import com.example.Document_analiser.entity.DocumentChunk;
import com.example.Document_analiser.repository.DocumentChunkRepository;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for optimized database queries with caching and performance monitoring.
 */
@Service
public class QueryOptimizationService {

    private static final Logger log = LoggerFactory.getLogger(QueryOptimizationService.class);
    
    private final DocumentChunkRepository documentChunkRepository;

    public QueryOptimizationService(DocumentChunkRepository documentChunkRepository) {
        this.documentChunkRepository = documentChunkRepository;
    }

    /**
     * Optimized method to find relevant chunks with caching and pagination.
     * Uses batch processing to avoid loading too many chunks at once.
     */
    @Cacheable(value = "relevantChunks", key = "#documentId + '_' + #limit", cacheManager = "quickCacheManager")
    @Timed(value = "query.relevant.chunks.time", description = "Time to find relevant chunks")
    public List<DocumentChunk> findRelevantChunksOptimized(Long documentId, int limit) {
        log.debug("Finding relevant chunks for document: {} with limit: {}", documentId, limit);
        
        Pageable pageable = PageRequest.of(0, Math.min(limit, 50)); // Limit to prevent memory issues
        return documentChunkRepository.findByDocumentIdOrderByChunkIndex(documentId, pageable);
    }

    /**
     * Batch load document chunks to avoid N+1 problems.
     */
    @Cacheable(value = "documentChunks", key = "#documentIds.hashCode()", cacheManager = "quickCacheManager")
    @Timed(value = "query.batch.chunks.time", description = "Time to batch load chunks")
    public List<DocumentChunk> batchLoadChunks(List<Long> documentIds) {
        log.debug("Batch loading chunks for {} documents", documentIds.size());
        
        if (documentIds.isEmpty()) {
            return List.of();
        }
        
        return documentChunkRepository.findByDocumentIdIn(documentIds);
    }

    /**
     * Get chunk statistics for performance analysis.
     */
    @Cacheable(value = "chunkStats", cacheManager = "quickCacheManager")
    @Timed(value = "query.chunk.stats.time", description = "Time to calculate chunk statistics")
    public ChunkStatistics getChunkStatistics() {
        log.debug("Calculating chunk statistics");
        
        long totalChunks = documentChunkRepository.count();
        long documentsWithChunks = documentChunkRepository.countDistinctDocuments();
        double avgChunksPerDocument = documentsWithChunks > 0 ? (double) totalChunks / documentsWithChunks : 0;
        
        return new ChunkStatistics(totalChunks, documentsWithChunks, avgChunksPerDocument);
    }

    /**
     * Statistics about document chunks.
     */
    public static class ChunkStatistics {
        private final long totalChunks;
        private final long documentsWithChunks;
        private final double avgChunksPerDocument;

        public ChunkStatistics(long totalChunks, long documentsWithChunks, double avgChunksPerDocument) {
            this.totalChunks = totalChunks;
            this.documentsWithChunks = documentsWithChunks;
            this.avgChunksPerDocument = avgChunksPerDocument;
        }

        public long getTotalChunks() { return totalChunks; }
        public long getDocumentsWithChunks() { return documentsWithChunks; }
        public double getAvgChunksPerDocument() { return avgChunksPerDocument; }

        @Override
        public String toString() {
            return String.format("ChunkStatistics{totalChunks=%d, documentsWithChunks=%d, avgChunksPerDocument=%.2f}",
                    totalChunks, documentsWithChunks, avgChunksPerDocument);
        }
    }
}