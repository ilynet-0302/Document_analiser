package com.example.Document_analiser.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration for performance monitoring and metrics collection.
 */
@Configuration
@EnableAspectJAutoProxy
public class PerformanceConfig {

    /**
     * Enables @Timed annotation for method execution time measurement.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}