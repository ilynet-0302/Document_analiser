package com.example.Document_analiser.repository;

import com.example.Document_analiser.entity.DocumentChunk;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    @Query(value = """
            SELECT * FROM document_chunks
            WHERE document_id = :docId
            ORDER BY embedding <=> :embedding
            LIMIT :limit
            """, nativeQuery = true)
    List<DocumentChunk> findTopByCosineSimilarity(@Param("embedding") float[] embedding,
                                                   @Param("docId") Long documentId,
                                                   @Param("limit") int limit);

    @Query(value = """
            SELECT * FROM document_chunks
            ORDER BY embedding <=> :embedding
            LIMIT :limit
            """, nativeQuery = true)
    List<DocumentChunk> findTopChunks(@Param("embedding") float[] embedding,
                                      @Param("limit") int limit);

    /**
     * Optimized query to find chunks by document ID with pagination.
     */
    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.document.id = :documentId ORDER BY dc.chunkIndex")
    List<DocumentChunk> findByDocumentIdOrderByChunkIndex(@Param("documentId") Long documentId, Pageable pageable);

    /**
     * Batch load chunks for multiple documents to avoid N+1 problems.
     */
    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.document.id IN :documentIds ORDER BY dc.document.id, dc.chunkIndex")
    List<DocumentChunk> findByDocumentIdIn(@Param("documentIds") List<Long> documentIds);

    /**
     * Count distinct documents that have chunks.
     */
    @Query("SELECT COUNT(DISTINCT dc.document.id) FROM DocumentChunk dc")
    long countDistinctDocuments();

    /**
     * Find chunks with content length statistics.
     */
    @Query("SELECT dc FROM DocumentChunk dc WHERE LENGTH(dc.content) BETWEEN :minLength AND :maxLength")
    List<DocumentChunk> findByContentLengthBetween(@Param("minLength") int minLength, @Param("maxLength") int maxLength);

    /**
     * Get average chunk size per document.
     */
    @Query("SELECT dc.document.id, AVG(LENGTH(dc.content)) FROM DocumentChunk dc GROUP BY dc.document.id")
    List<Object[]> getAverageChunkSizePerDocument();
}
