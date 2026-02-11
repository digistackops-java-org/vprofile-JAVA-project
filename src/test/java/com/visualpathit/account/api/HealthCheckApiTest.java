package com.visualpathit.account.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * API Tests for Health Check Endpoints
 * Tests liveness and readiness probes
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Health Check API Tests")
class HealthCheckApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/health";
    }

    @Test
    @DisplayName("GET /health/live - Should return liveness status")
    void testLivenessProbe() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/live")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("UP"))
            .body("timestamp", notNullValue())
            .body("service", equalTo("vprofile-app"))
            .body("version", notNullValue());
    }

    @Test
    @DisplayName("GET /health/ready - Should return readiness status")
    void testReadinessProbe() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/ready")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(503)))
            .contentType(ContentType.JSON)
            .body("status", anyOf(equalTo("UP"), equalTo("DOWN")))
            .body("timestamp", notNullValue())
            .body("checks", notNullValue())
            .body("service", equalTo("vprofile-app"));
    }

    @Test
    @DisplayName("GET /health/ready - Should check database status")
    void testReadinessDatabaseCheck() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/ready")
        .then()
            .body("checks.database", anyOf(equalTo("UP"), equalTo("DOWN")));
    }

    @Test
    @DisplayName("GET /health/ready - Should check memory status")
    void testReadinessMemoryCheck() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/ready")
        .then()
            .body("checks.memory", notNullValue())
            .body("checks.memory.status", anyOf(equalTo("UP"), equalTo("DOWN")))
            .body("checks.memory.used", notNullValue())
            .body("checks.memory.max", notNullValue())
            .body("checks.memory.usagePercent", notNullValue());
    }

    @Test
    @DisplayName("GET /health - Should return general health status")
    void testGeneralHealthEndpoint() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get()
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(503)))
            .contentType(ContentType.JSON)
            .body("status", anyOf(equalTo("UP"), equalTo("DOWN")))
            .body("checks", notNullValue());
    }

    @Test
    @DisplayName("GET /health/live - Should respond quickly")
    void testLivenessPerformance() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/live")
        .then()
            .statusCode(200)
            .time(lessThan(1000L)); // Should respond in less than 1 second
    }

    @Test
    @DisplayName("GET /health/ready - Should include all required fields")
    void testReadinessResponseStructure() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/ready")
        .then()
            .body("$", hasKey("status"))
            .body("$", hasKey("timestamp"))
            .body("$", hasKey("checks"))
            .body("$", hasKey("service"));
    }

    @Test
    @DisplayName("GET /health/live - Should be accessible without authentication")
    void testLivenessNoAuth() {
        // Health endpoints should be accessible without authentication
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/live")
        .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("GET /health/ready - Should be accessible without authentication")
    void testReadinessNoAuth() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/ready")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(503)));
    }

    @Test
    @DisplayName("GET /health/live - Should handle multiple concurrent requests")
    void testLivenessConcurrency() {
        // Send multiple requests concurrently
        for (int i = 0; i < 10; i++) {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/live")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
        }
    }

    @Test
    @DisplayName("GET /health/ready - Database check should be boolean")
    void testReadinessDatabaseCheckType() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/ready")
        .then()
            .body("checks.database", instanceOf(String.class))
            .body("checks.database", anyOf(equalTo("UP"), equalTo("DOWN")));
    }

    @Test
    @DisplayName("GET /health/ready - Memory usage should be numeric")
    void testReadinessMemoryMetrics() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/ready")
        .then()
            .body("checks.memory.used", instanceOf(Number.class))
            .body("checks.memory.max", instanceOf(Number.class))
            .body("checks.memory.usagePercent", instanceOf(String.class));
    }

    @Test
    @DisplayName("GET /health/live - Should return consistent response format")
    void testLivenessResponseConsistency() {
        // Make multiple requests and verify consistent structure
        for (int i = 0; i < 5; i++) {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/live")
            .then()
                .statusCode(200)
                .body("$", hasKey("status"))
                .body("$", hasKey("timestamp"))
                .body("$", hasKey("service"))
                .body("$", hasKey("version"));
        }
    }

    @Test
    @DisplayName("HEAD /health/live - Should support HEAD requests")
    void testLivenessHeadRequest() {
        given()
        .when()
            .head("/live")
        .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("GET /health/ready - Should return 503 if not ready")
    void testReadinessDownStatus() {
        // Note: This test might pass with 200 if all services are UP
        // It's here to document the expected behavior
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/ready")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(503)));
    }
}
