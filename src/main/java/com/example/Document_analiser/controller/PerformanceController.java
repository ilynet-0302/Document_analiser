package com.example.Document_analiser.controller;

import com.example.Document_analiser.service.LogAnalysisService;
import com.example.Document_analiser.service.PerformanceMonitoringService;
import com.example.Document_analiser.service.ResponseTimeAnalyzer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for performance monitoring and analysis.
 * Provides endpoints to view performance metrics and statistics.
 */
@RestController
@RequestMapping("/api/admin/performance")
public class PerformanceController {

    private final PerformanceMonitoringService performanceService;
    private final ResponseTimeAnalyzer responseTimeAnalyzer;
    private final LogAnalysisService logAnalysisService;
    private final MeterRegistry meterRegistry;

    public PerformanceController(PerformanceMonitoringService performanceService,
                               ResponseTimeAnalyzer responseTimeAnalyzer,
                               LogAnalysisService logAnalysisService,
                               MeterRegistry meterRegistry) {
        this.performanceService = performanceService;
        this.responseTimeAnalyzer = responseTimeAnalyzer;
        this.logAnalysisService = logAnalysisService;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Get comprehensive performance statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Custom metrics
        stats.put("customMetrics", performanceService.getPerformanceStats());
        
        // JVM metrics
        Map<String, Object> jvmStats = new HashMap<>();
        meterRegistry.getMeters().forEach(meter -> {
            if (meter.getId().getName().startsWith("jvm")) {
                jvmStats.put(meter.getId().getName(), meter.measure());
            }
        });
        stats.put("jvmMetrics", jvmStats);
        
        // Application metrics
        Map<String, Object> appStats = new HashMap<>();
        meterRegistry.getMeters().forEach(meter -> {
            String name = meter.getId().getName();
            if (name.startsWith("questions") || name.startsWith("documents") || 
                name.startsWith("embedding") || name.startsWith("ai")) {
                appStats.put(name, meter.measure());
            }
        });
        stats.put("applicationMetrics", appStats);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Get cache performance metrics.
     */
    @GetMapping("/cache-metrics")
    public ResponseEntity<Map<String, Object>> getCacheMetrics() {
        Map<String, Object> cacheStats = new HashMap<>();
        
        meterRegistry.getMeters().forEach(meter -> {
            String name = meter.getId().getName();
            if (name.startsWith("cache")) {
                cacheStats.put(name, meter.measure());
            }
        });
        
        return ResponseEntity.ok(cacheStats);
    }

    /**
     * Get database performance metrics.
     */
    @GetMapping("/database-metrics")
    public ResponseEntity<Map<String, Object>> getDatabaseMetrics() {
        Map<String, Object> dbStats = new HashMap<>();
        
        meterRegistry.getMeters().forEach(meter -> {
            String name = meter.getId().getName();
            if (name.startsWith("hikari") || name.startsWith("jdbc") || 
                name.startsWith("hibernate")) {
                dbStats.put(name, meter.measure());
            }
        });
        
        return ResponseEntity.ok(dbStats);
    }

    /**
     * Get response time analysis.
     */
    @GetMapping("/response-times")
    public ResponseEntity<Map<String, Object>> getResponseTimes() {
        Map<String, Object> responseTimes = new HashMap<>();
        
        meterRegistry.getMeters().forEach(meter -> {
            String name = meter.getId().getName();
            if (name.contains("time") || name.contains("duration")) {
                responseTimes.put(name, meter.measure());
            }
        });
        
        return ResponseEntity.ok(responseTimes);
    }

    /**
     * Get detailed response time analysis for all endpoints.
     */
    @GetMapping("/response-analysis")
    public ResponseEntity<Map<String, Object>> getResponseTimeAnalysis() {
        return ResponseEntity.ok(responseTimeAnalyzer.getResponseTimeAnalysis());
    }

    /**
     * Get slow endpoints based on threshold.
     */
    @GetMapping("/slow-endpoints")
    public ResponseEntity<Map<String, Object>> getSlowEndpoints(@RequestParam(defaultValue = "1000") double thresholdMs) {
        return ResponseEntity.ok(responseTimeAnalyzer.getSlowEndpoints(thresholdMs));
    }

    /**
     * Get performance recommendations.
     */
    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, String>> getPerformanceRecommendations() {
        return ResponseEntity.ok(responseTimeAnalyzer.getPerformanceRecommendations());
    }

    /**
     * Get log analysis report.
     */
    @GetMapping("/log-analysis")
    public ResponseEntity<Map<String, Object>> getLogAnalysis() {
        return ResponseEntity.ok(logAnalysisService.getLogAnalysisReport());
    }

    /**
     * Get recent logs for a specific category.
     */
    @GetMapping("/logs/{category}")
    public ResponseEntity<Object> getRecentLogs(@PathVariable String category, 
                                               @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(logAnalysisService.getRecentLogs(category, limit));
    }

    /**
     * Get error statistics.
     */
    @GetMapping("/errors")
    public ResponseEntity<Map<String, Long>> getErrorStatistics() {
        return ResponseEntity.ok(logAnalysisService.getErrorStatistics());
    }

    /**
     * Get warning statistics.
     */
    @GetMapping("/warnings")
    public ResponseEntity<Map<String, Long>> getWarningStatistics() {
        return ResponseEntity.ok(logAnalysisService.getWarningStatistics());
    }
}