package com.visualpathit.account.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Health endpoints
 * Tests the full Spring context with MockMvc
 * 
 * Note: If you don't have a Spring Boot Application class, 
 * comment out @SpringBootTest and use @WebMvcTest(HealthController.class) instead
 */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@DisplayName("Health Controller Integration Tests")
class HealthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Integration Test 1: GET /health returns 200 and UP status")
    void testHealthEndpoint_Returns200AndUpStatus() throws Exception {
        mockMvc.perform(get("/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.details.application", is("vprofile")));
    }

    @Test
    @DisplayName("Integration Test 2: GET /health/live returns 200 and liveness check")
    void testLivenessEndpoint_Returns200() throws Exception {
        mockMvc.perform(get("/health/live")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.details.check", is("liveness")))
                .andExpect(jsonPath("$.details.message", containsString("alive")));
    }

    @Test
    @DisplayName("Integration Test 3: GET /health/ready returns readiness status")
    void testReadinessEndpoint_ReturnsReadinessStatus() throws Exception {
        mockMvc.perform(get("/health/ready")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.details.check", is("readiness")))
                .andExpect(jsonPath("$.details", hasKey("database")));
    }
}
