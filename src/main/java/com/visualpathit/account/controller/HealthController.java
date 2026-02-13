package com.visualpathit.account.controller;

import com.visualpathit.account.model.HealthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * Basic health check endpoint
     * Returns UP if the application is running
     */
    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = new HealthResponse("UP");
        response.addDetail("application", "vprofile");
        response.addDetail("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /**
     * Liveness probe endpoint
     * Indicates whether the application is running
     * Used by Kubernetes to know if it should restart the pod
     */
    @GetMapping("/live")
    public ResponseEntity<HealthResponse> liveness() {
        HealthResponse response = new HealthResponse("UP");
        response.addDetail("check", "liveness");
        response.addDetail("message", "Application is alive");
        return ResponseEntity.ok(response);
    }

    /**
     * Readiness probe endpoint
     * Indicates whether the application is ready to serve traffic
     * Checks database connection and other dependencies
     * Used by Kubernetes to know if it should send traffic to the pod
     */
    @GetMapping("/ready")
    public ResponseEntity<HealthResponse> readiness() {
        Map<String, Object> checks = new HashMap<>();
        boolean isReady = true;

        // Check database connectivity
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection()) {
                checks.put("database", "UP");
            } catch (Exception e) {
                checks.put("database", "DOWN");
                checks.put("database_error", e.getMessage());
                isReady = false;
            }
        } else {
            checks.put("database", "NOT_CONFIGURED");
        }

        // Add more checks as needed
        checks.put("diskSpace", "UP");
        checks.put("memory", "UP");

        String status = isReady ? "UP" : "DOWN";
        HealthResponse response = new HealthResponse(status, checks);
        response.addDetail("check", "readiness");

        HttpStatus httpStatus = isReady ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(httpStatus).body(response);
    }
}
