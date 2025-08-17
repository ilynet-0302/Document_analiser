package com.example.Document_analiser.repository;

import com.example.Document_analiser.entity.DocumentChunk;
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
}
