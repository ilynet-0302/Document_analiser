package com.example.Document_analiser.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service for analyzing endpoint response times and identifying performance bottlenecks.
 */
@Service
public class ResponseTimeAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(ResponseTimeAnalyzer.class);
    
    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> endpointTimers = new ConcurrentHashMap<>();
    private final Map<String, ResponseTimeStats> endpointStats = new ConcurrentHashMap<>();

    public ResponseTimeAnalyzer(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Record response time for an endpoint.
     */
    public void recordResponseTime(String endpoint, Duration duration) {
        Timer timer = endpointTimers.computeIfAbsent(endpoint, 
            name -> Timer.builder("endpoint.response.time")
                    .tag("endpoint", name)
                    .description("Response time for endpoint: " + name)
                    .register(meterRegistry));
        
        timer.record(duration);
        
        // Update custom statistics
        endpointStats.compute(endpoint, (key, stats) -> {
            if (stats == null) {
                stats = new ResponseTimeStats();
            }
            stats.addMeasurement(duration.toMillis());
            return stats;
        });
        
        log.debug("Recorded response time for {}: {} ms", endpoint, duration.toMillis());
    }

    /**
     * Get response time analysis for all endpoints.
     */
    public Map<String, Object> getResponseTimeAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        
        for (Map.Entry<String, ResponseTimeStats> entry : endpointStats.entrySet()) {
            String endpoint = entry.getKey();
            ResponseTimeStats stats = entry.getValue();
            
            Map<String, Object> endpointAnalysis = new HashMap<>();
            endpointAnalysis.put("count", stats.getCount());
            endpointAnalysis.put("averageMs", stats.getAverage());
            endpointAnalysis.put("minMs", stats.getMin());
            endpointAnalysis.put("maxMs", stats.getMax());
            endpointAnalysis.put("p95Ms", stats.getP95());
            endpointAnalysis.put("p99Ms", stats.getP99());
            
            // Get timer statistics from Micrometer
            Timer timer = endpointTimers.get(endpoint);
            if (timer != null) {
                endpointAnalysis.put("micrometerMean", timer.mean(TimeUnit.MILLISECONDS));
                endpointAnalysis.put("micrometerMax", timer.max(TimeUnit.MILLISECONDS));
                endpointAnalysis.put("totalTime", timer.totalTime(TimeUnit.MILLISECONDS));
            }
            
            analysis.put(endpoint, endpointAnalysis);
        }
        
        return analysis;
    }

    /**
     * Identify slow endpoints based on average response time.
     */
    public Map<String, Object> getSlowEndpoints(double thresholdMs) {
        Map<String, Object> slowEndpoints = new HashMap<>();
        
        for (Map.Entry<String, ResponseTimeStats> entry : endpointStats.entrySet()) {
            String endpoint = entry.getKey();
            ResponseTimeStats stats = entry.getValue();
            
            if (stats.getAverage() > thresholdMs) {
                Map<String, Object> details = new HashMap<>();
                details.put("averageMs", stats.getAverage());
                details.put("maxMs", stats.getMax());
                details.put("count", stats.getCount());
                details.put("exceedsThresholdBy", stats.getAverage() - thresholdMs);
                
                slowEndpoints.put(endpoint, details);
            }
        }
        
        return slowEndpoints;
    }

    /**
     * Get performance recommendations based on response time analysis.
     */
    public Map<String, String> getPerformanceRecommendations() {
        Map<String, String> recommendations = new HashMap<>();
        
        for (Map.Entry<String, ResponseTimeStats> entry : endpointStats.entrySet()) {
            String endpoint = entry.getKey();
            ResponseTimeStats stats = entry.getValue();
            
            if (stats.getAverage() > 5000) {
                recommendations.put(endpoint, "CRITICAL: Average response time > 5s. Consider caching, database optimization, or async processing.");
            } else if (stats.getAverage() > 2000) {
                recommendations.put(endpoint, "WARNING: Average response time > 2s. Review query performance and consider caching.");
            } else if (stats.getAverage() > 1000) {
                recommendations.put(endpoint, "INFO: Average response time > 1s. Monitor for potential optimization opportunities.");
            } else if (stats.getMax() > 10000) {
                recommendations.put(endpoint, "WARNING: Maximum response time > 10s. Check for occasional performance spikes.");
            }
        }
        
        return recommendations;
    }

    /**
     * Statistics for response times.
     */
    private static class ResponseTimeStats {
        private long count = 0;
        private double sum = 0;
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;
        private final java.util.List<Double> measurements = new java.util.ArrayList<>();

        public void addMeasurement(double value) {
            count++;
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
            
            // Keep last 1000 measurements for percentile calculation
            measurements.add(value);
            if (measurements.size() > 1000) {
                measurements.remove(0);
            }
        }

        public long getCount() { return count; }
        public double getAverage() { return count > 0 ? sum / count : 0; }
        public double getMin() { return min == Double.MAX_VALUE ? 0 : min; }
        public double getMax() { return max == Double.MIN_VALUE ? 0 : max; }

        public double getP95() {
            if (measurements.isEmpty()) return 0;
            java.util.List<Double> sorted = new java.util.ArrayList<>(measurements);
            sorted.sort(Double::compareTo);
            int index = (int) Math.ceil(0.95 * sorted.size()) - 1;
            return sorted.get(Math.max(0, index));
        }

        public double getP99() {
            if (measurements.isEmpty()) return 0;
            java.util.List<Double> sorted = new java.util.ArrayList<>(measurements);
            sorted.sort(Double::compareTo);
            int index = (int) Math.ceil(0.99 * sorted.size()) - 1;
            return sorted.get(Math.max(0, index));
        }
    }
}