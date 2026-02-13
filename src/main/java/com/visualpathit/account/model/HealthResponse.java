package com.visualpathit.account.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class HealthResponse {
    
    private String status;
    private LocalDateTime timestamp;
    private Map<String, Object> details;

    public HealthResponse() {
        this.timestamp = LocalDateTime.now();
        this.details = new HashMap<>();
    }

    public HealthResponse(String status) {
        this();
        this.status = status;
    }

    public HealthResponse(String status, Map<String, Object> details) {
        this(status);
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public void addDetail(String key, Object value) {
        this.details.put(key, value);
    }
}
