package com.visualpathit.account.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * API tests for Health endpoints using REST Assured
 * Tests the actual HTTP endpoints
 * 
 * Note: If you don't have a Spring Boot Application class,
 * comment out @SpringBootTest and run these tests manually against a running server
 */
@SpringBootTest(
    classes = TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DisplayName("Health API Tests with REST Assured")
class HealthApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/health";
    }

    @Test
    @DisplayName("API Test 1: GET /health returns 200 with correct JSON structure")
    void testHealthApi_ReturnsCorrectStructure() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get()
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("UP"))
            .body("timestamp", notNullValue())
            .body("details.application", equalTo("vprofile"))
            .body("details.version", notNullValue());
    }

    @Test
    @DisplayName("API Test 2: GET /health/live confirms application liveness")
    void testLivenessApi_ConfirmsApplicationIsAlive() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/live")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("UP"))
            .body("details.check", equalTo("liveness"))
            .body("details.message", containsString("alive"));
    }

    @Test
    @DisplayName("API Test 3: GET /health/ready checks all dependencies")
    void testReadinessApi_ChecksDependencies() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/ready")
        .then()
            .statusCode(anyOf(is(200), is(503)))
            .contentType(ContentType.JSON)
            .body("status", notNullValue())
            .body("details.check", equalTo("readiness"))
            .body("details.database", notNullValue())
            .body("details.diskSpace", notNullValue())
            .body("details.memory", notNullValue());
    }
}
