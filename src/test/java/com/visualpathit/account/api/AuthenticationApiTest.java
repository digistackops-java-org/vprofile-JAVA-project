package com.visualpathit.account.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * API Tests for Authentication
 * Tests login, logout, and security features
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Authentication API Tests")
class AuthenticationApiTest {

    @LocalServerPort
    private int port;

    private static String authToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/auth/register - Register new user")
    void testRegisterUser() {
        String requestBody = """
            {
                "username": "authtest",
                "password": "SecurePass123!",
                "email": "auth@test.com"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(201)
            .body("username", equalTo("authtest"))
            .body("email", equalTo("auth@test.com"))
            .body("password", nullValue()); // Password should not be returned
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/auth/login - Login with valid credentials")
    void testLoginSuccess() {
        String loginBody = """
            {
                "username": "authtest",
                "password": "SecurePass123!"
            }
            """;

        authToken = given()
            .contentType(ContentType.JSON)
            .body(loginBody)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("username", equalTo("authtest"))
            .body("expiresIn", greaterThan(0))
        .extract()
            .path("token");
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/auth/login - Login with invalid credentials")
    void testLoginFailure() {
        String loginBody = """
            {
                "username": "authtest",
                "password": "WrongPassword"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(loginBody)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401)
            .body("message", containsString("Invalid credentials"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/users/me - Access protected endpoint with token")
    void testAccessProtectedEndpointWithToken() {
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/users/me")
        .then()
            .statusCode(200)
            .body("username", equalTo("authtest"));
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/users/me - Access protected endpoint without token")
    void testAccessProtectedEndpointWithoutToken() {
        given()
        .when()
            .get("/users/me")
        .then()
            .statusCode(401);
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/users/me - Access with expired token")
    void testAccessWithExpiredToken() {
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired.token";

        given()
            .header("Authorization", "Bearer " + expiredToken)
        .when()
            .get("/users/me")
        .then()
            .statusCode(401);
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/auth/refresh - Refresh token")
    void testRefreshToken() {
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("expiresIn", greaterThan(0));
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/auth/logout - Logout user")
    void testLogout() {
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .post("/auth/logout")
        .then()
            .statusCode(200)
            .body("message", containsString("Successfully logged out"));
    }

    @Test
    @Order(9)
    @DisplayName("POST /api/auth/forgot-password - Request password reset")
    void testForgotPassword() {
        String requestBody = """
            {
                "email": "auth@test.com"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/auth/forgot-password")
        .then()
            .statusCode(200)
            .body("message", containsString("Password reset email sent"));
    }

    @Test
    @Order(10)
    @DisplayName("POST /api/auth/reset-password - Reset password with token")
    void testResetPassword() {
        String requestBody = """
            {
                "token": "reset-token-here",
                "newPassword": "NewSecurePass123!"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/auth/reset-password")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(400)));
    }

    @Test
    @Order(11)
    @DisplayName("POST /api/auth/change-password - Change password")
    void testChangePassword() {
        // Login again to get fresh token
        String loginBody = """
            {
                "username": "authtest",
                "password": "SecurePass123!"
            }
            """;

        String token = given()
            .contentType(ContentType.JSON)
            .body(loginBody)
        .when()
            .post("/auth/login")
        .then()
            .extract()
            .path("token");

        String changePasswordBody = """
            {
                "oldPassword": "SecurePass123!",
                "newPassword": "NewPassword456!"
            }
            """;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(changePasswordBody)
        .when()
            .post("/auth/change-password")
        .then()
            .statusCode(200)
            .body("message", containsString("Password changed successfully"));
    }

    @Test
    @Order(12)
    @DisplayName("POST /api/auth/register - Weak password validation")
    void testWeakPasswordRejection() {
        String requestBody = """
            {
                "username": "weakpassuser",
                "password": "123",
                "email": "weak@test.com"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(400)
            .body("errors", hasItem(containsString("password")));
    }

    @Test
    @Order(13)
    @DisplayName("POST /api/auth/login - Account lockout after failed attempts")
    void testAccountLockout() {
        String loginBody = """
            {
                "username": "authtest",
                "password": "WrongPassword"
            }
            """;

        // Attempt multiple failed logins
        for (int i = 0; i < 5; i++) {
            given()
                .contentType(ContentType.JSON)
                .body(loginBody)
            .when()
                .post("/auth/login")
            .then()
                .statusCode(401);
        }

        // Next attempt should be locked
        given()
            .contentType(ContentType.JSON)
            .body(loginBody)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(anyOf(equalTo(401), equalTo(423)))
            .body("message", anyOf(
                containsString("Account locked"),
                containsString("Too many attempts")
            ));
    }

    @Test
    @Order(14)
    @DisplayName("GET /api/admin/users - Access admin endpoint with user token")
    void testAdminEndpointAccessDenied() {
        String loginBody = """
            {
                "username": "authtest",
                "password": "NewPassword456!"
            }
            """;

        String userToken = given()
            .contentType(ContentType.JSON)
            .body(loginBody)
        .when()
            .post("/auth/login")
        .then()
            .extract()
            .path("token");

        given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/admin/users")
        .then()
            .statusCode(403);
    }

    @Test
    @Order(15)
    @DisplayName("POST /api/auth/verify-email - Email verification")
    void testEmailVerification() {
        String requestBody = """
            {
                "token": "verification-token"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/auth/verify-email")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(400)));
    }
}
