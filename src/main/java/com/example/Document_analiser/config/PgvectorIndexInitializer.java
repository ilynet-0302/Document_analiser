package com.example.Document_analiser.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PgvectorIndexInitializer {

    private final JdbcTemplate jdbcTemplate;

    public PgvectorIndexInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void createIndex() {
        try {
            // Ensure the embedding column has fixed dimensions required by pgvector indexes
            jdbcTemplate.execute(
                    "ALTER TABLE IF EXISTS document_chunks " +
                            "ALTER COLUMN embedding TYPE vector(1536) USING embedding::vector(1536)");
        } catch (Exception ignored) {
            // If the column already has dimensions or table missing during bootstrap, ignore
        }

        jdbcTemplate.execute(
                "CREATE INDEX IF NOT EXISTS document_chunks_embedding_idx " +
                        "ON document_chunks USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100)");
    }
}
