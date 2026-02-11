package com.visualpathit.account.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visualpathit.account.model.User;
import com.visualpathit.account.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for User Management
 * Tests the full stack from controller to database
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("User Management Integration Tests")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("integrationtest");
        testUser.setPassword("password123");
        testUser.setEmail("integration@test.com");
        testUser.setRole("USER");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Should create user and persist to database")
    @WithMockUser
    @Transactional
    void testCreateUserIntegration() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("integrationtest")))
                .andExpect(jsonPath("$.email", is("integration@test.com")));

        // Verify database state
        User savedUser = userRepository.findByUsername("integrationtest").orElse(null);
        assertNotNull(savedUser);
        assertEquals("integrationtest", savedUser.getUsername());
        assertEquals("integration@test.com", savedUser.getEmail());
    }

    @Test
    @Order(2)
    @DisplayName("Should retrieve user from database")
    @WithMockUser
    void testGetUserIntegration() throws Exception {
        // Arrange - Save user to database
        User savedUser = userRepository.save(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/" + savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("integrationtest")))
                .andExpect(jsonPath("$.email", is("integration@test.com")));
    }

    @Test
    @Order(3)
    @DisplayName("Should update user in database")
    @WithMockUser
    @Transactional
    void testUpdateUserIntegration() throws Exception {
        // Arrange
        User savedUser = userRepository.save(testUser);
        savedUser.setEmail("updated@test.com");

        // Act & Assert
        mockMvc.perform(put("/api/users/" + savedUser.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("updated@test.com")));

        // Verify database state
        User updatedUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals("updated@test.com", updatedUser.getEmail());
    }

    @Test
    @Order(4)
    @DisplayName("Should delete user from database")
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void testDeleteUserIntegration() throws Exception {
        // Arrange
        User savedUser = userRepository.save(testUser);
        Long userId = savedUser.getId();

        // Act & Assert
        mockMvc.perform(delete("/api/users/" + userId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify database state
        assertFalse(userRepository.existsById(userId));
    }

    @Test
    @Order(5)
    @DisplayName("Should handle duplicate username error")
    @WithMockUser
    @Transactional
    void testCreateDuplicateUserIntegration() throws Exception {
        // Arrange - Create first user
        userRepository.save(testUser);

        // Act & Assert - Try to create duplicate
        User duplicateUser = new User();
        duplicateUser.setUsername("integrationtest");
        duplicateUser.setPassword("different");
        duplicateUser.setEmail("different@test.com");

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(6)
    @DisplayName("Should find all users from database")
    @WithMockUser
    void testGetAllUsersIntegration() throws Exception {
        // Arrange
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("pass1");
        user1.setEmail("user1@test.com");

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("pass2");
        user2.setEmail("user2@test.com");

        userRepository.save(user1);
        userRepository.save(user2);

        // Act & Assert
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].username", containsInAnyOrder("user1", "user2")));
    }

    @Test
    @Order(7)
    @DisplayName("Should search user by username")
    @WithMockUser
    void testSearchUserIntegration() throws Exception {
        // Arrange
        userRepository.save(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/search")
                .param("username", "integrationtest")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("integrationtest")));
    }

    @Test
    @Order(8)
    @DisplayName("Should handle transaction rollback on error")
    @WithMockUser
    @Transactional
    void testTransactionRollback() throws Exception {
        // This test verifies that database operations rollback on error
        long initialCount = userRepository.count();

        // Attempt to create invalid user (should fail validation)
        User invalidUser = new User();
        // Missing required fields

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        // Verify no data was persisted
        assertEquals(initialCount, userRepository.count());
    }
}
