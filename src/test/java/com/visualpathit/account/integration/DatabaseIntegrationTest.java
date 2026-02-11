package com.visualpathit.account.integration;

import com.visualpathit.account.model.User;
import com.visualpathit.account.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database Integration Tests
 * Tests JPA repository operations against real database
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Database Integration Tests")
class DatabaseIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("dbtest");
        testUser.setPassword("password");
        testUser.setEmail("db@test.com");
        testUser.setRole("USER");
    }

    @Test
    @DisplayName("Should save and retrieve user from database")
    void testSaveAndRetrieve() {
        // Act
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());
        assertTrue(retrievedUser.isPresent());
        assertEquals("dbtest", retrievedUser.get().getUsername());
        assertEquals("db@test.com", retrievedUser.get().getEmail());
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername() {
        // Arrange
        entityManager.persist(testUser);
        entityManager.flush();

        // Act
        Optional<User> foundUser = userRepository.findByUsername("dbtest");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("dbtest", foundUser.get().getUsername());
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail() {
        // Arrange
        entityManager.persist(testUser);
        entityManager.flush();

        // Act
        Optional<User> foundUser = userRepository.findByEmail("db@test.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("db@test.com", foundUser.get().getEmail());
    }

    @Test
    @DisplayName("Should update user in database")
    void testUpdateUser() {
        // Arrange
        User savedUser = entityManager.persist(testUser);
        entityManager.flush();

        // Act
        savedUser.setEmail("updated@test.com");
        userRepository.save(savedUser);
        entityManager.flush();
        entityManager.clear();

        // Assert
        User updatedUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals("updated@test.com", updatedUser.getEmail());
    }

    @Test
    @DisplayName("Should delete user from database")
    void testDeleteUser() {
        // Arrange
        User savedUser = entityManager.persist(testUser);
        entityManager.flush();
        Long userId = savedUser.getId();

        // Act
        userRepository.deleteById(userId);
        entityManager.flush();

        // Assert
        assertFalse(userRepository.existsById(userId));
    }

    @Test
    @DisplayName("Should find users by role")
    void testFindByRole() {
        // Arrange
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("admin123");
        adminUser.setEmail("admin@test.com");
        adminUser.setRole("ADMIN");

        entityManager.persist(testUser);
        entityManager.persist(adminUser);
        entityManager.flush();

        // Act
        List<User> usersByRole = userRepository.findByRole("USER");

        // Assert
        assertEquals(1, usersByRole.size());
        assertEquals("dbtest", usersByRole.get(0).getUsername());
    }

    @Test
    @DisplayName("Should check if username exists")
    void testExistsByUsername() {
        // Arrange
        entityManager.persist(testUser);
        entityManager.flush();

        // Act & Assert
        assertTrue(userRepository.existsByUsername("dbtest"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    @DisplayName("Should count users")
    void testCountUsers() {
        // Arrange
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("pass2");
        user2.setEmail("user2@test.com");

        entityManager.persist(testUser);
        entityManager.persist(user2);
        entityManager.flush();

        // Act
        long count = userRepository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should handle entity lifecycle")
    void testEntityLifecycle() {
        // Create (Transient)
        User newUser = new User();
        newUser.setUsername("lifecycle");
        newUser.setPassword("test");
        newUser.setEmail("lifecycle@test.com");
        assertNull(newUser.getId());

        // Persist (Managed)
        User managedUser = userRepository.save(newUser);
        assertNotNull(managedUser.getId());

        // Detach
        entityManager.detach(managedUser);

        // Merge back
        managedUser.setEmail("merged@test.com");
        User mergedUser = entityManager.merge(managedUser);
        entityManager.flush();

        // Verify
        assertEquals("merged@test.com", mergedUser.getEmail());

        // Remove
        userRepository.delete(mergedUser);
        entityManager.flush();
        assertFalse(userRepository.existsById(mergedUser.getId()));
    }

    @Test
    @DisplayName("Should handle database constraints")
    void testUniqueConstraints() {
        // Arrange
        entityManager.persist(testUser);
        entityManager.flush();

        // Act & Assert - Try to insert duplicate username
        User duplicateUser = new User();
        duplicateUser.setUsername("dbtest");
        duplicateUser.setPassword("different");
        duplicateUser.setEmail("different@test.com");

        assertThrows(Exception.class, () -> {
            userRepository.save(duplicateUser);
            entityManager.flush();
        });
    }
}
