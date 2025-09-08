package com.example.Document_analiser.repository.projection;

/**
 * Проекция за леко извличане на съдържанието на chunk (id, индекс, текст)
 * без четене на векторната колона (по-бързо за fallback/визуализация).
 */
public interface ChunkContentView {
    Long getId();
    int getChunkIndex();
    String getContent();
}

