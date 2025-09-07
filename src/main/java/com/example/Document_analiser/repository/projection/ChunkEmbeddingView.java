package com.example.Document_analiser.repository.projection;

public interface ChunkEmbeddingView {
    Long getId();
    int getChunkIndex();
    float[] getEmbedding();
}

