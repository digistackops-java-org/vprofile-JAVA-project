package com.visualpathit.account.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller for Kubernetes/Container orchestration
 * Provides liveness and readiness probes
 */
@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @Autowired
    private DataSource dataSource;

    /**
     * Liveness probe - checks if the application is running
     * Returns 200 if the application is alive
     * This should be lightweight and always return quickly
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "vprofile-app");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Readiness probe - checks if the application is ready to serve traffic
     * Returns 200 if ready, 503 if not ready
     * Checks critical dependencies like database connectivity
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> checks = new HashMap<>();
        boolean isReady = true;

        // Check database connectivity
        boolean dbHealthy = checkDatabaseHealth();
        checks.put("database", dbHealthy ? "UP" : "DOWN");
        if (!dbHealthy) {
            isReady = false;
        }

        // Check memory
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        boolean memoryHealthy = memoryUsagePercent < 90;
        checks.put("memory", Map.of(
            "status", memoryHealthy ? "UP" : "DOWN",
            "used", usedMemory,
            "max", maxMemory,
            "usagePercent", String.format("%.2f%%", memoryUsagePercent)
        ));
        if (!memoryHealthy) {
            isReady = false;
        }

        response.put("status", isReady ? "UP" : "DOWN");
        response.put("timestamp", System.currentTimeMillis());
        response.put("checks", checks);
        response.put("service", "vprofile-app");

        return isReady 
            ? ResponseEntity.ok(response)
            : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * General health endpoint - combines liveness and readiness
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        return readiness();
    }

    /**
     * Check database health by executing a simple query
     */
    private boolean checkDatabaseHealth() {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
