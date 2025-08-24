package com.example.Document_analiser.controller;

import com.example.Document_analiser.service.QueryOptimizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for performance testing and benchmarking.
 * Provides endpoints to test various performance scenarios.
 */
@RestController
@RequestMapping("/api/admin/performance-test")
public class PerformanceTestController {

    private final QueryOptimizationService queryOptimizationService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public PerformanceTestController(QueryOptimizationService queryOptimizationService) {
        this.queryOptimizationService = queryOptimizationService;
    }

    /**
     * Test database query performance.
     */
    @PostMapping("/database-queries")
    public ResponseEntity<Map<String, Object>> testDatabaseQueries(@RequestParam(defaultValue = "100") int iterations) {
        Map<String, Object> results = new HashMap<>();
        
        Instant start = Instant.now();
        
        // Test chunk statistics query
        Instant chunkStatsStart = Instant.now();
        for (int i = 0; i < iterations; i++) {
            queryOptimizationService.getChunkStatistics();
        }
        Duration chunkStatsDuration = Duration.between(chunkStatsStart, Instant.now());
        
        Duration totalDuration = Duration.between(start, Instant.now());
        
        results.put("iterations", iterations);
        results.put("chunkStatsAvgMs", chunkStatsDuration.toMillis() / (double) iterations);
        results.put("totalDurationMs", totalDuration.toMillis());
        results.put("queriesPerSecond", iterations / (totalDuration.toMillis() / 1000.0));
        
        return ResponseEntity.ok(results);
    }

    /**
     * Test cache performance.
     */
    @PostMapping("/cache-performance")
    public ResponseEntity<Map<String, Object>> testCachePerformance(@RequestParam(defaultValue = "50") int iterations) {
        Map<String, Object> results = new HashMap<>();
        
        // Test cache hit/miss patterns
        Instant start = Instant.now();
        
        // First run - cache misses
        Instant coldStart = Instant.now();
        for (int i = 0; i < iterations; i++) {
            queryOptimizationService.getChunkStatistics();
        }
        Duration coldDuration = Duration.between(coldStart, Instant.now());
        
        // Second run - cache hits
        Instant warmStart = Instant.now();
        for (int i = 0; i < iterations; i++) {
            queryOptimizationService.getChunkStatistics();
        }
        Duration warmDuration = Duration.between(warmStart, Instant.now());
        
        Duration totalDuration = Duration.between(start, Instant.now());
        
        results.put("iterations", iterations);
        results.put("coldRunAvgMs", coldDuration.toMillis() / (double) iterations);
        results.put("warmRunAvgMs", warmDuration.toMillis() / (double) iterations);
        results.put("cacheSpeedupRatio", coldDuration.toMillis() / (double) warmDuration.toMillis());
        results.put("totalDurationMs", totalDuration.toMillis());
        
        return ResponseEntity.ok(results);
    }

    /**
     * Test concurrent request handling.
     */
    @PostMapping("/concurrent-requests")
    public ResponseEntity<Map<String, Object>> testConcurrentRequests(
            @RequestParam(defaultValue = "10") int concurrency,
            @RequestParam(defaultValue = "5") int requestsPerThread) {
        
        Map<String, Object> results = new HashMap<>();
        
        Instant start = Instant.now();
        
        CompletableFuture<Void>[] futures = new CompletableFuture[concurrency];
        
        for (int i = 0; i < concurrency; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    queryOptimizationService.getChunkStatistics();
                }
            }, executorService);
        }
        
        // Wait for all threads to complete
        CompletableFuture.allOf(futures).join();
        
        Duration totalDuration = Duration.between(start, Instant.now());
        int totalRequests = concurrency * requestsPerThread;
        
        results.put("concurrency", concurrency);
        results.put("requestsPerThread", requestsPerThread);
        results.put("totalRequests", totalRequests);
        results.put("totalDurationMs", totalDuration.toMillis());
        results.put("avgRequestTimeMs", totalDuration.toMillis() / (double) totalRequests);
        results.put("requestsPerSecond", totalRequests / (totalDuration.toMillis() / 1000.0));
        
        return ResponseEntity.ok(results);
    }

    /**
     * Test memory usage patterns.
     */
    @GetMapping("/memory-usage")
    public ResponseEntity<Map<String, Object>> getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        
        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
        memoryInfo.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
        memoryInfo.put("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        memoryInfo.put("maxMemoryMB", runtime.maxMemory() / (1024 * 1024));
        memoryInfo.put("memoryUsagePercent", 
            ((runtime.totalMemory() - runtime.freeMemory()) * 100.0) / runtime.maxMemory());
        
        return ResponseEntity.ok(memoryInfo);
    }

    /**
     * Force garbage collection and measure impact.
     */
    @PostMapping("/gc-test")
    public ResponseEntity<Map<String, Object>> testGarbageCollection() {
        Runtime runtime = Runtime.getRuntime();
        
        Map<String, Object> results = new HashMap<>();
        
        // Memory before GC
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        Instant start = Instant.now();
        System.gc();
        Duration gcDuration = Duration.between(start, Instant.now());
        
        // Memory after GC
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        
        results.put("memoryBeforeMB", memoryBefore / (1024 * 1024));
        results.put("memoryAfterMB", memoryAfter / (1024 * 1024));
        results.put("memoryFreedMB", (memoryBefore - memoryAfter) / (1024 * 1024));
        results.put("gcDurationMs", gcDuration.toMillis());
        
        return ResponseEntity.ok(results);
    }

    /**
     * Comprehensive performance benchmark.
     */
    @PostMapping("/benchmark")
    public ResponseEntity<Map<String, Object>> runBenchmark() {
        Map<String, Object> benchmark = new HashMap<>();
        
        // Database performance
        ResponseEntity<Map<String, Object>> dbTest = testDatabaseQueries(50);
        benchmark.put("databasePerformance", dbTest.getBody());
        
        // Cache performance
        ResponseEntity<Map<String, Object>> cacheTest = testCachePerformance(25);
        benchmark.put("cachePerformance", cacheTest.getBody());
        
        // Concurrent performance
        ResponseEntity<Map<String, Object>> concurrentTest = testConcurrentRequests(5, 10);
        benchmark.put("concurrentPerformance", concurrentTest.getBody());
        
        // Memory usage
        ResponseEntity<Map<String, Object>> memoryTest = getMemoryUsage();
        benchmark.put("memoryUsage", memoryTest.getBody());
        
        benchmark.put("timestamp", Instant.now().toString());
        benchmark.put("benchmarkDurationMs", "Comprehensive benchmark completed");
        
        return ResponseEntity.ok(benchmark);
    }
}