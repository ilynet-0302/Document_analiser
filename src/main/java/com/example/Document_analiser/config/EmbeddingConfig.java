package com.example.Document_analiser.config;

import com.example.Document_analiser.service.QuestionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация на embedding клиент.
 *
 * - Какво прави: осигурява детерминирана локална имплементация за генериране
 *   на векторни представяния (embeddings), за да работи търсенето без външни
 *   зависимости.
 * - Защо: бързо и стабилно за dev/демо; размерът трябва да съвпада с
 *   pgvector колоната (1536).
 */
@Configuration
public class EmbeddingConfig {

    /** Връща имплементация на {@link QuestionService.EmbeddingClient} с локални embeddings. */
    @Bean
    public QuestionService.EmbeddingClient embeddingClient() {
        // Детерминирани локални embeddings, за да няма външни зависимости
        final int dim = 1536; // трябва да съвпада с vector(1536)
        return (text, model) -> {
            float[] v = new float[dim];
            if (text == null) return v;
            long x = text.hashCode();
            if (x == 0) x = 1;
            for (int i = 0; i < dim; i++) {
                x ^= (x << 13);
                x ^= (x >>> 17);
                x ^= (x << 5);
                v[i] = ((x & 0xFFFF) / 32768.0f) - 1.0f;
            }
            double norm = 0.0;
            for (float f : v) norm += f * f;
            norm = Math.sqrt(norm);
            if (norm > 0) {
                for (int i = 0; i < dim; i++) v[i] /= (float) norm;
            }
            return v;
        };
    }
}
