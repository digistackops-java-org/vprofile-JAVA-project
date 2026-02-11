package com.visualpathit.account.api;

import com.visualpathit.account.model.User;
import com.visualpathit.account.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * API Tests for User Endpoints
 * Tests REST API using RestAssured
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("User API Tests")
class UserApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private static Long createdUserId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
    }

    @AfterAll
    static void cleanup(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/users - Create user")
    void testCreateUser() {
        String requestBody = """
            {
                "username": "apitest",
                "password": "password123",
                "email": "api@test.com",
                "role": "USER"
            }
            """;

        createdUserId = given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .body("username", equalTo("apitest"))
            .body("email", equalTo("api@test.com"))
            .body("role", equalTo("USER"))
            .body("id", notNullValue())
        .extract()
            .path("id");
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/users/{id} - Get user by ID")
    void testGetUserById() {
        given()
            .pathParam("id", createdUserId)
        .when()
            .get("/users/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(createdUserId.intValue()))
            .body("username", equalTo("apitest"))
            .body("email", equalTo("api@test.com"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/users - Get all users")
    void testGetAllUsers() {
        given()
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("username", hasItem("apitest"));
    }

    @Test
    @Order(4)
    @DisplayName("PUT /api/users/{id} - Update user")
    void testUpdateUser() {
        String updateBody = """
            {
                "username": "apitest",
                "email": "updated@test.com",
                "role": "USER"
            }
            """;

        given()
            .pathParam("id", createdUserId)
            .contentType(ContentType.JSON)
            .body(updateBody)
        .when()
            .put("/users/{id}")
        .then()
            .statusCode(200)
            .body("email", equalTo("updated@test.com"));
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/users/search - Search user by username")
    void testSearchUser() {
        given()
            .queryParam("username", "apitest")
        .when()
            .get("/users/search")
        .then()
            .statusCode(200)
            .body("username", equalTo("apitest"));
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/users - Validation: Empty username")
    void testCreateUserWithEmptyUsername() {
        String invalidBody = """
            {
                "username": "",
                "password": "password123",
                "email": "test@test.com"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(invalidBody)
        .when()
            .post("/users")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/users - Validation: Invalid email")
    void testCreateUserWithInvalidEmail() {
        String invalidBody = """
            {
                "username": "testuser",
                "password": "password123",
                "email": "invalid-email"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(invalidBody)
        .when()
            .post("/users")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/users - Duplicate username conflict")
    void testCreateDuplicateUser() {
        String duplicateBody = """
            {
                "username": "apitest",
                "password": "different",
                "email": "different@test.com"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(duplicateBody)
        .when()
            .post("/users")
        .then()
            .statusCode(409);
    }

    @Test
    @Order(9)
    @DisplayName("GET /api/users/{id} - Not found")
    void testGetNonExistentUser() {
        given()
            .pathParam("id", 99999)
        .when()
            .get("/users/{id}")
        .then()
            .statusCode(404);
    }

    @Test
    @Order(10)
    @DisplayName("PUT /api/users/{id} - Update non-existent user")
    void testUpdateNonExistentUser() {
        String updateBody = """
            {
                "username": "nonexistent",
                "email": "test@test.com"
            }
            """;

        given()
            .pathParam("id", 99999)
            .contentType(ContentType.JSON)
            .body(updateBody)
        .when()
            .put("/users/{id}")
        .then()
            .statusCode(404);
    }

    @Test
    @Order(11)
    @DisplayName("DELETE /api/users/{id} - Delete user")
    void testDeleteUser() {
        given()
            .pathParam("id", createdUserId)
        .when()
            .delete("/users/{id}")
        .then()
            .statusCode(204);

        // Verify deletion
        given()
            .pathParam("id", createdUserId)
        .when()
            .get("/users/{id}")
        .then()
            .statusCode(404);
    }

    @Test
    @Order(12)
    @DisplayName("POST /api/users - Test response headers")
    void testResponseHeaders() {
        String requestBody = """
            {
                "username": "headertest",
                "password": "password123",
                "email": "header@test.com"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .header("Content-Type", containsString("application/json"));
    }

    @Test
    @Order(13)
    @DisplayName("GET /api/users - Test pagination parameters")
    void testPaginationParameters() {
        given()
            .queryParam("page", 0)
            .queryParam("size", 10)
            .queryParam("sort", "username,asc")
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("$", hasSize(lessThanOrEqualTo(10)));
    }

    @Test
    @Order(14)
    @DisplayName("GET /api/users - Test filtering by role")
    void testFilterByRole() {
        given()
            .queryParam("role", "USER")
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("role", everyItem(equalTo("USER")));
    }

    @Test
    @Order(15)
    @DisplayName("OPTIONS /api/users - CORS preflight")
    void testCorsPreflightRequest() {
        given()
            .header("Origin", "http://localhost:3000")
            .header("Access-Control-Request-Method", "POST")
        .when()
            .options("/users")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(204)));
    }
}
