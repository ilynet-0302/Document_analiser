package com.example.Document_analiser.service;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Service for centralized cache management and monitoring.
 */
@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);
    
    private final CacheManager cacheManager;
    private final CacheManager embeddingCacheManager;
    private final CacheManager quickCacheManager;

    public CacheService(CacheManager cacheManager,
                       CacheManager embeddingCacheManager,
                       CacheManager quickCacheManager) {
        this.cacheManager = cacheManager;
        this.embeddingCacheManager = embeddingCacheManager;
        this.quickCacheManager = quickCacheManager;
    }

    /**
     * Clears all caches.
     */
    public void clearAllCaches() {
        log.info("Clearing all caches");
        clearCacheManager(cacheManager, "default");
        clearCacheManager(embeddingCacheManager, "embedding");
        clearCacheManager(quickCacheManager, "quick");
    }

    /**
     * Clears a specific cache by name.
     */
    public void clearCache(String cacheName) {
        log.info("Clearing cache: {}", cacheName);
        
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return;
        }
        
        cache = embeddingCacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return;
        }
        
        cache = quickCacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return;
        }
        
        log.warn("Cache not found: {}", cacheName);
    }

    /**
     * Gets cache statistics for monitoring.
     */
    public String getCacheStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== Cache Statistics ===\n");
        
        appendCacheManagerStats(stats, cacheManager, "Default");
        appendCacheManagerStats(stats, embeddingCacheManager, "Embedding");
        appendCacheManagerStats(stats, quickCacheManager, "Quick");
        
        return stats.toString();
    }

    private void clearCacheManager(CacheManager manager, String managerName) {
        Collection<String> cacheNames = manager.getCacheNames();
        log.info("Clearing {} caches from {} manager", cacheNames.size(), managerName);
        
        for (String cacheName : cacheNames) {
            Cache cache = manager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.debug("Cleared cache: {}", cacheName);
            }
        }
    }

    private void appendCacheManagerStats(StringBuilder stats, CacheManager manager, String managerName) {
        stats.append(String.format("\n--- %s Cache Manager ---\n", managerName));
        Collection<String> cacheNames = manager.getCacheNames();
        
        if (cacheNames.isEmpty()) {
            stats.append("No caches found\n");
            return;
        }
        
        for (String cacheName : cacheNames) {
            Cache cache = manager.getCache(cacheName);
            if (cache != null) {
                stats.append(String.format("Cache: %s - Native: %s\n", 
                    cacheName, cache.getNativeCache().getClass().getSimpleName()));
            }
        }
    }
}