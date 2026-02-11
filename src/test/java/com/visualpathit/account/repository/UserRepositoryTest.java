package com.visualpathit.account.repository;

import com.visualpathit.account.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for UserRepository
 * Tests custom query methods and JPA operations
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Unit Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = new User();
        testUser1.setUsername("testuser1");
        testUser1.setPassword("password1");
        testUser1.setEmail("user1@test.com");
        testUser1.setRole("USER");
        testUser1.setCreatedAt(LocalDateTime.now());

        testUser2 = new User();
        testUser2.setUsername("testuser2");
        testUser2.setPassword("password2");
        testUser2.setEmail("user2@test.com");
        testUser2.setRole("ADMIN");
        testUser2.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should save user to repository")
    void testSaveUser() {
        // Act
        User savedUser = userRepository.save(testUser1);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("testuser1", savedUser.getUsername());
    }

    @Test
    @DisplayName("Should find user by ID")
    void testFindById() {
        // Arrange
        User savedUser = entityManager.persist(testUser1);
        entityManager.flush();

        // Act
        Optional<User> found = userRepository.findById(savedUser.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(savedUser.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername() {
        // Arrange
        entityManager.persist(testUser1);
        entityManager.flush();

        // Act
        Optional<User> found = userRepository.findByUsername("testuser1");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("testuser1", found.get().getUsername());
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail() {
        // Arrange
        entityManager.persist(testUser1);
        entityManager.flush();

        // Act
        Optional<User> found = userRepository.findByEmail("user1@test.com");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("user1@test.com", found.get().getEmail());
    }

    @Test
    @DisplayName("Should find users by role")
    void testFindByRole() {
        // Arrange
        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
        entityManager.flush();

        // Act
        List<User> users = userRepository.findByRole("USER");
        List<User> admins = userRepository.findByRole("ADMIN");

        // Assert
        assertEquals(1, users.size());
        assertEquals(1, admins.size());
        assertEquals("testuser1", users.get(0).getUsername());
        assertEquals("testuser2", admins.get(0).getUsername());
    }

    @Test
    @DisplayName("Should check if username exists")
    void testExistsByUsername() {
        // Arrange
        entityManager.persist(testUser1);
        entityManager.flush();

        // Act & Assert
        assertTrue(userRepository.existsByUsername("testuser1"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmail() {
        // Arrange
        entityManager.persist(testUser1);
        entityManager.flush();

        // Act & Assert
        assertTrue(userRepository.existsByEmail("user1@test.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@test.com"));
    }

    @Test
    @DisplayName("Should delete user by ID")
    void testDeleteById() {
        // Arrange
        User savedUser = entityManager.persist(testUser1);
        entityManager.flush();
        Long userId = savedUser.getId();

        // Act
        userRepository.deleteById(userId);
        entityManager.flush();

        // Assert
        assertFalse(userRepository.existsById(userId));
    }

    @Test
    @DisplayName("Should count all users")
    void testCount() {
        // Arrange
        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
        entityManager.flush();

        // Act
        long count = userRepository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should find all users")
    void testFindAll() {
        // Arrange
        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
        entityManager.flush();

        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertEquals(2, users.size());
    }

    @Test
    @DisplayName("Should find users with pagination")
    void testFindAllWithPagination() {
        // Arrange
        for (int i = 0; i < 15; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setPassword("password" + i);
            user.setEmail("user" + i + "@test.com");
            user.setRole("USER");
            entityManager.persist(user);
        }
        entityManager.flush();

        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = userRepository.findAll(pageable);

        // Assert
        assertEquals(10, page.getContent().size());
        assertEquals(15, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    @DisplayName("Should find users with sorting")
    void testFindAllWithSorting() {
        // Arrange
        entityManager.persist(testUser2); // testuser2
        entityManager.persist(testUser1); // testuser1
        entityManager.flush();

        // Act
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.ASC, "username"));

        // Assert
        assertEquals(2, users.size());
        assertEquals("testuser1", users.get(0).getUsername());
        assertEquals("testuser2", users.get(1).getUsername());
    }

    @Test
    @DisplayName("Should find users by username containing")
    void testFindByUsernameContaining() {
        // Arrange
        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
        entityManager.flush();

        // Act
        List<User> users = userRepository.findByUsernameContaining("user");

        // Assert
        assertEquals(2, users.size());
    }

    @Test
    @DisplayName("Should find users created after date")
    void testFindByCreatedAtAfter() {
        // Arrange
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        testUser1.setCreatedAt(yesterday);
        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
        entityManager.flush();

        // Act
        List<User> users = userRepository.findByCreatedAtAfter(yesterday.plusHours(1));

        // Assert
        assertEquals(1, users.size());
        assertEquals("testuser2", users.get(0).getUsername());
    }

    @Test
    @DisplayName("Should delete users by role")
    void testDeleteByRole() {
        // Arrange
        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
        entityManager.flush();

        // Act
        userRepository.deleteByRole("ADMIN");
        entityManager.flush();

        // Assert
        List<User> remainingUsers = userRepository.findAll();
        assertEquals(1, remainingUsers.size());
        assertEquals("USER", remainingUsers.get(0).getRole());
    }

    @Test
    @DisplayName("Should count users by role")
    void testCountByRole() {
        // Arrange
        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
        entityManager.flush();

        // Act
        long userCount = userRepository.countByRole("USER");
        long adminCount = userRepository.countByRole("ADMIN");

        // Assert
        assertEquals(1, userCount);
        assertEquals(1, adminCount);
    }

    @Test
    @DisplayName("Should handle empty result")
    void testFindByUsernameNotFound() {
        // Act
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should update user")
    void testUpdateUser() {
        // Arrange
        User savedUser = entityManager.persist(testUser1);
        entityManager.flush();
        entityManager.clear();

        // Act
        savedUser.setEmail("newemail@test.com");
        User updatedUser = userRepository.save(savedUser);
        entityManager.flush();

        // Assert
        assertEquals("newemail@test.com", updatedUser.getEmail());
    }
}
