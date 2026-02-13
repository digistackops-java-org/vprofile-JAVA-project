package com.visualpathit.account.controller;

import com.visualpathit.account.model.HealthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for HealthController
 * Tests individual methods in isolation using mocks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Health Controller Unit Tests")
class HealthControllerTest {

    @InjectMocks
    private HealthController healthController;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @BeforeEach
    void setUp() {
        // Inject the mocked dataSource into the controller
        ReflectionTestUtils.setField(healthController, "dataSource", dataSource);
    }

    @Test
    @DisplayName("Test 1: Basic health endpoint returns UP status")
    void testHealthEndpoint_ReturnsUpStatus() {
        // When
        ResponseEntity<HealthResponse> response = healthController.health();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("UP");
        assertThat(response.getBody().getDetails()).containsKey("application");
        assertThat(response.getBody().getDetails().get("application")).isEqualTo("vprofile");
    }

    @Test
    @DisplayName("Test 2: Liveness probe returns UP status")
    void testLivenessProbe_ReturnsUpStatus() {
        // When
        ResponseEntity<HealthResponse> response = healthController.liveness();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("UP");
        assertThat(response.getBody().getDetails()).containsKey("check");
        assertThat(response.getBody().getDetails().get("check")).isEqualTo("liveness");
    }

    @Test
    @DisplayName("Test 3: Readiness probe returns UP when database is healthy")
    void testReadinessProbe_WithHealthyDatabase_ReturnsUp() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);

        // When
        ResponseEntity<HealthResponse> response = healthController.readiness();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("UP");
        assertThat(response.getBody().getDetails()).containsKey("database");
        assertThat(response.getBody().getDetails().get("database")).isEqualTo("UP");
    }
}
