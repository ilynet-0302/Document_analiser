package com.example.Document_analiser.repository;

import com.example.Document_analiser.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByName(String name);
    
    /**
     * Optimized query to fetch documents with their chunks to avoid N+1 problem.
     */
    @Query("SELECT DISTINCT d FROM Document d LEFT JOIN FETCH d.chunks")
    List<Document> findAllWithChunks();
}