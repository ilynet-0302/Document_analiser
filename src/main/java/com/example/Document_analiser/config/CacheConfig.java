package com.example.Document_analiser.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * Cache configuration using Caffeine for high-performance in-memory caching.
 * Optimized for document analysis use cases with different TTL strategies.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Primary cache manager with default settings for general use cases.
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheSpecification("maximumSize=1000,expireAfterWrite=30m,expireAfterAccess=15m,recordStats");
        cacheManager.setCacheNames(List.of("questions", "documents", "users"));
        return cacheManager;
    }

    /**
     * Cache manager for embeddings with longer TTL due to expensive computation.
     */
    @Bean("embeddingCacheManager")
    public CacheManager embeddingCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheSpecification("maximumSize=500,expireAfterWrite=2h,expireAfterAccess=1h,recordStats");
        cacheManager.setCacheNames(List.of("embeddings"));
        return cacheManager;
    }

    /**
     * Cache manager for frequently accessed data with shorter TTL.
     */
    @Bean("quickCacheManager")
    public CacheManager quickCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheSpecification("maximumSize=2000,expireAfterWrite=10m,expireAfterAccess=5m,recordStats");
        cacheManager.setCacheNames(List.of("relevantChunks", "documentChunks", "chunkStats"));
        return cacheManager;
    }

}