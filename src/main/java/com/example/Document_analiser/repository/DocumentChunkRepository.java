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

    // Return only ids to avoid mapping vector column in entity
    @Query(value = """
            SELECT id FROM document_chunks
            WHERE document_id = ?2
            ORDER BY embedding <=> CAST(?1 AS vector(1536))
            LIMIT ?3
            """, nativeQuery = true)
    List<Long> findTopIdsByCosineSimilarity(String embedding,
                                            Long documentId,
                                            int limit);

    @Query(value = """
            SELECT * FROM document_chunks
            WHERE document_id = :docId
            ORDER BY embedding <=> CAST(:embedding AS vector(1536))
            LIMIT :limit
            """, nativeQuery = true)
    List<DocumentChunk> findTopByCosineSimilarity(@Param("embedding") String embedding,
                                                   @Param("docId") Long documentId,
                                                   @Param("limit") int limit);

    @Query(value = """
            SELECT * FROM document_chunks
            ORDER BY embedding <=> CAST(:embedding AS vector(1536))
            LIMIT :limit
            """, nativeQuery = true)
    List<DocumentChunk> findTopChunks(@Param("embedding") String embedding,
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

    // Lightweight projection for embeddings to avoid loading LOB content
    @org.springframework.data.jpa.repository.Query(
            "SELECT dc.id AS id, dc.chunkIndex AS chunkIndex, dc.embedding AS embedding " +
            "FROM DocumentChunk dc WHERE dc.document.id = :documentId ORDER BY dc.chunkIndex")
    java.util.List<com.example.Document_analiser.repository.projection.ChunkEmbeddingView>
    findEmbeddingsByDocumentId(@Param("documentId") Long documentId, Pageable pageable);

    // Content-only projection to avoid touching vector column in fallbacks
    @org.springframework.data.jpa.repository.Query(
            "SELECT dc.id AS id, dc.chunkIndex AS chunkIndex, dc.content AS content " +
            "FROM DocumentChunk dc WHERE dc.document.id = :documentId ORDER BY dc.chunkIndex")
    java.util.List<com.example.Document_analiser.repository.projection.ChunkContentView>
    findContentByDocumentId(@Param("documentId") Long documentId, Pageable pageable);

    // Fetch content by ids via projection (order not guaranteed)
    @org.springframework.data.jpa.repository.Query(
            "SELECT dc.id AS id, dc.chunkIndex AS chunkIndex, dc.content AS content " +
            "FROM DocumentChunk dc WHERE dc.id IN :ids")
    java.util.List<com.example.Document_analiser.repository.projection.ChunkContentView>
    findContentByIdIn(@Param("ids") java.util.List<Long> ids);

    // Fetch selected chunks by id
    java.util.List<DocumentChunk> findByIdIn(java.util.List<Long> ids);
}
