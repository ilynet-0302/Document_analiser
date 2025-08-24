package com.example.Document_analiser.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for analyzing application logs and identifying performance patterns.
 */
@Service
public class LogAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(LogAnalysisService.class);
    
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> warnCounts = new ConcurrentHashMap<>();
    private final Map<String, List<LogEntry>> recentLogs = new ConcurrentHashMap<>();
    private final int MAX_RECENT_LOGS = 1000;

    /**
     * Record an error event for analysis.
     */
    public void recordError(String category, String message, Throwable throwable) {
        errorCounts.computeIfAbsent(category, k -> new AtomicLong(0)).incrementAndGet();
        
        LogEntry entry = new LogEntry(LocalDateTime.now(), "ERROR", category, message, 
                throwable != null ? throwable.getMessage() : null);
        addToRecentLogs(category, entry);
        
        log.debug("Recorded error in category: {}", category);
    }

    /**
     * Record a warning event for analysis.
     */
    public void recordWarning(String category, String message) {
        warnCounts.computeIfAbsent(category, k -> new AtomicLong(0)).incrementAndGet();
        
        LogEntry entry = new LogEntry(LocalDateTime.now(), "WARN", category, message, null);
        addToRecentLogs(category, entry);
        
        log.debug("Recorded warning in category: {}", category);
    }

    /**
     * Get error statistics by category.
     */
    public Map<String, Long> getErrorStatistics() {
        Map<String, Long> stats = new HashMap<>();
        errorCounts.forEach((category, count) -> stats.put(category, count.get()));
        return stats;
    }

    /**
     * Get warning statistics by category.
     */
    public Map<String, Long> getWarningStatistics() {
        Map<String, Long> stats = new HashMap<>();
        warnCounts.forEach((category, count) -> stats.put(category, count.get()));
        return stats;
    }

    /**
     * Get recent logs for a specific category.
     */
    public List<LogEntry> getRecentLogs(String category, int limit) {
        List<LogEntry> logs = recentLogs.getOrDefault(category, new ArrayList<>());
        return logs.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .toList();
    }

    /**
     * Get comprehensive log analysis report.
     */
    public Map<String, Object> getLogAnalysisReport() {
        Map<String, Object> report = new HashMap<>();
        
        // Error analysis
        Map<String, Long> errors = getErrorStatistics();
        long totalErrors = errors.values().stream().mapToLong(Long::longValue).sum();
        report.put("totalErrors", totalErrors);
        report.put("errorsByCategory", errors);
        
        // Warning analysis
        Map<String, Long> warnings = getWarningStatistics();
        long totalWarnings = warnings.values().stream().mapToLong(Long::longValue).sum();
        report.put("totalWarnings", totalWarnings);
        report.put("warningsByCategory", warnings);
        
        // Top error categories
        List<Map.Entry<String, Long>> topErrors = errors.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .toList();
        report.put("topErrorCategories", topErrors);
        
        // Performance insights
        Map<String, String> insights = generatePerformanceInsights(errors, warnings);
        report.put("performanceInsights", insights);
        
        return report;
    }

    /**
     * Generate performance insights based on log patterns.
     */
    private Map<String, String> generatePerformanceInsights(Map<String, Long> errors, Map<String, Long> warnings) {
        Map<String, String> insights = new HashMap<>();
        
        // Database-related issues
        long dbErrors = errors.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().contains("database") || 
                           e.getKey().toLowerCase().contains("sql") ||
                           e.getKey().toLowerCase().contains("hibernate"))
                .mapToLong(Map.Entry::getValue)
                .sum();
        
        if (dbErrors > 10) {
            insights.put("database", "High number of database-related errors detected. Consider connection pool optimization and query analysis.");
        }
        
        // Cache-related issues
        long cacheWarnings = warnings.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().contains("cache"))
                .mapToLong(Map.Entry::getValue)
                .sum();
        
        if (cacheWarnings > 5) {
            insights.put("cache", "Cache warnings detected. Review cache configuration and hit ratios.");
        }
        
        // Embedding-related issues
        long embeddingErrors = errors.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().contains("embedding"))
                .mapToLong(Map.Entry::getValue)
                .sum();
        
        if (embeddingErrors > 5) {
            insights.put("embedding", "Embedding generation errors detected. Check AI service connectivity and rate limits.");
        }
        
        // Memory-related issues
        long memoryWarnings = warnings.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().contains("memory") || 
                           e.getKey().toLowerCase().contains("heap"))
                .mapToLong(Map.Entry::getValue)
                .sum();
        
        if (memoryWarnings > 3) {
            insights.put("memory", "Memory-related warnings detected. Consider increasing heap size or optimizing memory usage.");
        }
        
        return insights;
    }

    private void addToRecentLogs(String category, LogEntry entry) {
        recentLogs.compute(category, (key, logs) -> {
            if (logs == null) {
                logs = new ArrayList<>();
            }
            logs.add(entry);
            
            // Keep only recent logs
            if (logs.size() > MAX_RECENT_LOGS) {
                logs.sort(Comparator.comparing(LogEntry::getTimestamp).reversed());
                logs = new ArrayList<>(logs.subList(0, MAX_RECENT_LOGS));
            }
            
            return logs;
        });
    }

    /**
     * Log entry for analysis.
     */
    public static class LogEntry {
        private final LocalDateTime timestamp;
        private final String level;
        private final String category;
        private final String message;
        private final String exception;

        public LogEntry(LocalDateTime timestamp, String level, String category, String message, String exception) {
            this.timestamp = timestamp;
            this.level = level;
            this.category = category;
            this.message = message;
            this.exception = exception;
        }

        public LocalDateTime getTimestamp() { return timestamp; }
        public String getLevel() { return level; }
        public String getCategory() { return category; }
        public String getMessage() { return message; }
        public String getException() { return exception; }

        public String getFormattedTimestamp() {
            return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        @Override
        public String toString() {
            return String.format("[%s] %s - %s: %s%s", 
                    getFormattedTimestamp(), level, category, message,
                    exception != null ? " (" + exception + ")" : "");
        }
    }
}