package com.example.Document_analiser.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service for custom performance monitoring and metrics collection.
 */
@Service
public class PerformanceMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitoringService.class);
    
    private final MeterRegistry meterRegistry;
    private final Counter questionProcessedCounter;
    private final Counter documentUploadCounter;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Timer embeddingGenerationTimer;
    private final Timer aiResponseTimer;

    public PerformanceMonitoringService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.questionProcessedCounter = Counter.builder("questions.processed.total")
                .description("Total number of questions processed")
                .register(meterRegistry);
                
        this.documentUploadCounter = Counter.builder("documents.uploaded.total")
                .description("Total number of documents uploaded")
                .register(meterRegistry);
                
        this.cacheHitCounter = Counter.builder("cache.hits.total")
                .description("Total number of cache hits")
                .register(meterRegistry);
                
        this.cacheMissCounter = Counter.builder("cache.misses.total")
                .description("Total number of cache misses")
                .register(meterRegistry);
        
        // Initialize timers
        this.embeddingGenerationTimer = Timer.builder("embedding.generation.duration")
                .description("Time taken to generate embeddings")
                .register(meterRegistry);
                
        this.aiResponseTimer = Timer.builder("ai.response.duration")
                .description("Time taken for AI to generate responses")
                .register(meterRegistry);
    }

    /**
     * Record a question processing event.
     */
    public void recordQuestionProcessed() {
        questionProcessedCounter.increment();
        log.debug("Question processed counter incremented");
    }

    /**
     * Record a document upload event.
     */
    public void recordDocumentUploaded() {
        documentUploadCounter.increment();
        log.debug("Document upload counter incremented");
    }

    /**
     * Record a cache hit event.
     */
    public void recordCacheHit(String cacheName) {
        Counter.builder("cache.hits.specific")
                .tag("cache", cacheName)
                .description("Cache hits for " + cacheName)
                .register(meterRegistry)
                .increment();
        cacheHitCounter.increment();
        log.debug("Cache hit recorded for cache: {}", cacheName);
    }

    /**
     * Record a cache miss event.
     */
    public void recordCacheMiss(String cacheName) {
        Counter.builder("cache.misses.specific")
                .tag("cache", cacheName)
                .description("Cache misses for " + cacheName)
                .register(meterRegistry)
                .increment();
        cacheMissCounter.increment();
        log.debug("Cache miss recorded for cache: {}", cacheName);
    }

    /**
     * Record embedding generation time.
     */
    public void recordEmbeddingGeneration(Duration duration) {
        embeddingGenerationTimer.record(duration);
        log.debug("Embedding generation time recorded: {} ms", duration.toMillis());
    }

    /**
     * Record AI response time.
     */
    public void recordAiResponse(Duration duration) {
        aiResponseTimer.record(duration);
        log.debug("AI response time recorded: {} ms", duration.toMillis());
    }

    /**
     * Get current performance statistics.
     */
    public String getPerformanceStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== Performance Statistics ===\n");
        stats.append(String.format("Questions Processed: %.0f\n", questionProcessedCounter.count()));
        stats.append(String.format("Documents Uploaded: %.0f\n", documentUploadCounter.count()));
        stats.append(String.format("Cache Hits: %.0f\n", cacheHitCounter.count()));
        stats.append(String.format("Cache Misses: %.0f\n", cacheMissCounter.count()));
        stats.append(String.format("Avg Embedding Generation Time: %.2f ms\n", 
            embeddingGenerationTimer.mean(TimeUnit.MILLISECONDS)));
        stats.append(String.format("Avg AI Response Time: %.2f ms\n", 
            aiResponseTimer.mean(TimeUnit.MILLISECONDS)));
        
        return stats.toString();
    }
}