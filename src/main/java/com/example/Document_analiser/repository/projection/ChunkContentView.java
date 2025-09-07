package com.example.Document_analiser.repository.projection;

public interface ChunkContentView {
    Long getId();
    int getChunkIndex();
    String getContent();
}

