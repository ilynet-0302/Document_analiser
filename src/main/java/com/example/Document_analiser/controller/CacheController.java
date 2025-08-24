package com.example.Document_analiser.controller;

import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for cache management and monitoring.
 * Provides endpoints to clear caches and view cache statistics.
 */
@RestController
@RequestMapping("/api/admin/cache")
public class CacheController {

    private final CacheManager cacheManager;
    private final CacheManager embeddingCacheManager;
    private final CacheManager quickCacheManager;

    public CacheController(CacheManager cacheManager,
                          CacheManager embeddingCacheManager,
                          CacheManager quickCacheManager) {
        this.cacheManager = cacheManager;
        this.embeddingCacheManager = embeddingCacheManager;
        this.quickCacheManager = quickCacheManager;
    }

    /**
     * Clear all caches.
     */
    @PostMapping("/clear-all")
    public ResponseEntity<String> clearAllCaches() {
        clearCacheManager(cacheManager);
        clearCacheManager(embeddingCacheManager);
        clearCacheManager(quickCacheManager);
        return ResponseEntity.ok("All caches cleared successfully");
    }

    /**
     * Clear a specific cache by name.
     */
    @PostMapping("/clear/{cacheName}")
    public ResponseEntity<String> clearCache(@PathVariable String cacheName) {
        boolean cleared = clearSpecificCache(cacheName);
        if (cleared) {
            return ResponseEntity.ok("Cache '" + cacheName + "' cleared successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get cache statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("defaultCaches", getCacheManagerStats(cacheManager));
        stats.put("embeddingCaches", getCacheManagerStats(embeddingCacheManager));
        stats.put("quickCaches", getCacheManagerStats(quickCacheManager));
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Get list of all cache names.
     */
    @GetMapping("/names")
    public ResponseEntity<Map<String, Collection<String>>> getCacheNames() {
        Map<String, Collection<String>> cacheNames = new HashMap<>();
        
        cacheNames.put("default", cacheManager.getCacheNames());
        cacheNames.put("embedding", embeddingCacheManager.getCacheNames());
        cacheNames.put("quick", quickCacheManager.getCacheNames());
        
        return ResponseEntity.ok(cacheNames);
    }

    private void clearCacheManager(CacheManager manager) {
        manager.getCacheNames().forEach(cacheName -> {
            var cache = manager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    private boolean clearSpecificCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return true;
        }
        
        cache = embeddingCacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return true;
        }
        
        cache = quickCacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return true;
        }
        
        return false;
    }

    private Map<String, Object> getCacheManagerStats(CacheManager manager) {
        Map<String, Object> stats = new HashMap<>();
        Collection<String> cacheNames = manager.getCacheNames();
        
        stats.put("cacheCount", cacheNames.size());
        stats.put("cacheNames", cacheNames);
        
        return stats;
    }
}