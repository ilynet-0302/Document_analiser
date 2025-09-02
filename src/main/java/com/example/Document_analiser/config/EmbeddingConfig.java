package com.example.Document_analiser.config;

import com.example.Document_analiser.service.QuestionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingConfig {

    @Bean
    public QuestionService.EmbeddingClient embeddingClient() {
        // Deterministic local embeddings to keep search working without extra deps
        final int dim = 1536; // must match vector(1536)
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
