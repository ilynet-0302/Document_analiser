package com.example.Document_analiser.repository.projection;

/**
 * Проекция за леко извличане на id, индекс и embedding на chunk,
 * без да се мапва цялата ентити (за по-добра производителност).
 */
public interface ChunkEmbeddingView {
    Long getId();
    int getChunkIndex();
    float[] getEmbedding();
}

