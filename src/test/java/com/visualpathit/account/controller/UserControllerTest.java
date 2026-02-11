package com.visualpathit.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visualpathit.account.model.User;
import com.visualpathit.account.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Tests for UserController
 * Tests REST endpoints using MockMvc
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");
    }

    @Test
    @DisplayName("GET /api/users should return all users")
    @WithMockUser
    void testGetAllUsers() throws Exception {
        // Arrange
        List<User> users = Arrays.asList(testUser, new User());
        when(userService.findAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("testuser")));

        verify(userService, times(1)).findAllUsers();
    }

    @Test
    @DisplayName("GET /api/users/{id} should return user by id")
    @WithMockUser
    void testGetUserById_Success() throws Exception {
        // Arrange
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /api/users/{id} should return 404 when user not found")
    @WithMockUser
    void testGetUserById_NotFound() throws Exception {
        // Arrange
        when(userService.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(999L);
    }

    @Test
    @DisplayName("POST /api/users should create new user")
    @WithMockUser
    void testCreateUser_Success() throws Exception {
        // Arrange
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("testuser")));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("POST /api/users should return 400 for invalid input")
    @WithMockUser
    void testCreateUser_InvalidInput() throws Exception {
        // Arrange
        User invalidUser = new User();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @DisplayName("PUT /api/users/{id} should update user")
    @WithMockUser
    void testUpdateUser_Success() throws Exception {
        // Arrange
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("updated@example.com");

        when(userService.updateUser(anyLong(), any(User.class))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("updated@example.com")));

        verify(userService, times(1)).updateUser(eq(1L), any(User.class));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} should delete user")
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_Success() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} should return 403 for non-admin")
    @WithMockUser(roles = "USER")
    void testDeleteUser_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/users/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(anyLong());
    }

    @Test
    @DisplayName("GET /api/users/search should find users by username")
    @WithMockUser
    void testSearchUsers() throws Exception {
        // Arrange
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/api/users/search")
                .param("username", "testuser")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")));

        verify(userService, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("POST /api/users should return 409 when username exists")
    @WithMockUser
    void testCreateUser_UsernameConflict() throws Exception {
        // Arrange
        when(userService.createUser(any(User.class)))
            .thenThrow(new IllegalArgumentException("Username already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).createUser(any(User.class));
    }
}
