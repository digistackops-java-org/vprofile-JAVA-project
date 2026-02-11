package com.visualpathit.account.integration;

import com.visualpathit.account.model.User;
import net.spy.memcached.MemcachedClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for Memcached Caching
 * Tests caching functionality with Memcached
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Memcached Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemcachedIntegrationTest {

    @Autowired(required = false)
    private MemcachedClient memcachedClient;

    @Autowired(required = false)
    private CacheManager cacheManager;

    private User testUser;
    private static final String CACHE_NAME = "users";
    private static final String CACHE_KEY = "user:1";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("cachetest");
        testUser.setEmail("cache@test.com");
        testUser.setRole("USER");
    }

    @AfterEach
    void tearDown() {
        if (memcachedClient != null) {
            memcachedClient.delete(CACHE_KEY);
        }
        if (cacheManager != null) {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should connect to Memcached server")
    void testMemcachedConnection() {
        if (memcachedClient != null) {
            assertNotNull(memcachedClient);
            assertTrue(memcachedClient.getVersions().size() > 0);
        } else {
            System.out.println("Memcached client not configured - skipping test");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Should store and retrieve object from cache")
    void testCacheStoreAndRetrieve() {
        if (memcachedClient != null) {
            // Act - Store in cache
            memcachedClient.set(CACHE_KEY, 3600, testUser);

            // Give memcached time to process
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Assert - Retrieve from cache
            Object cachedUser = memcachedClient.get(CACHE_KEY);
            assertNotNull(cachedUser);
            assertTrue(cachedUser instanceof User);
            assertEquals("cachetest", ((User) cachedUser).getUsername());
        } else {
            System.out.println("Memcached client not configured - skipping test");
        }
    }

    @Test
    @Order(3)
    @DisplayName("Should delete object from cache")
    void testCacheDelete() {
        if (memcachedClient != null) {
            // Arrange
            memcachedClient.set(CACHE_KEY, 3600, testUser);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Act
            memcachedClient.delete(CACHE_KEY);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Assert
            Object cachedUser = memcachedClient.get(CACHE_KEY);
            assertNull(cachedUser);
        } else {
            System.out.println("Memcached client not configured - skipping test");
        }
    }

    @Test
    @Order(4)
    @DisplayName("Should handle cache expiration")
    void testCacheExpiration() {
        if (memcachedClient != null) {
            // Act - Store with 2 second expiration
            memcachedClient.set(CACHE_KEY, 2, testUser);
            
            // Assert - Object exists initially
            Object cachedUser = memcachedClient.get(CACHE_KEY);
            assertNotNull(cachedUser);

            // Wait for expiration
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Assert - Object should be expired
            cachedUser = memcachedClient.get(CACHE_KEY);
            assertNull(cachedUser);
        } else {
            System.out.println("Memcached client not configured - skipping test");
        }
    }

    @Test
    @Order(5)
    @DisplayName("Should use Spring Cache abstraction")
    void testSpringCacheAbstraction() {
        if (cacheManager != null) {
            // Get cache
            Cache cache = cacheManager.getCache(CACHE_NAME);
            assertNotNull(cache);

            // Put value
            cache.put(CACHE_KEY, testUser);

            // Get value
            Cache.ValueWrapper wrapper = cache.get(CACHE_KEY);
            assertNotNull(wrapper);
            assertNotNull(wrapper.get());
            
            User cachedUser = (User) wrapper.get();
            assertEquals("cachetest", cachedUser.getUsername());

            // Evict
            cache.evict(CACHE_KEY);
            assertNull(cache.get(CACHE_KEY));
        } else {
            System.out.println("CacheManager not configured - skipping test");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Should handle cache miss")
    void testCacheMiss() {
        if (memcachedClient != null) {
            // Act
            Object result = memcachedClient.get("nonexistent:key");

            // Assert
            assertNull(result);
        } else {
            System.out.println("Memcached client not configured - skipping test");
        }
    }

    @Test
    @Order(7)
    @DisplayName("Should update cached object")
    void testCacheUpdate() {
        if (memcachedClient != null) {
            // Arrange - Store initial value
            memcachedClient.set(CACHE_KEY, 3600, testUser);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Act - Update cached value
            testUser.setEmail("updated@test.com");
            memcachedClient.set(CACHE_KEY, 3600, testUser);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Assert
            User cachedUser = (User) memcachedClient.get(CACHE_KEY);
            assertNotNull(cachedUser);
            assertEquals("updated@test.com", cachedUser.getEmail());
        } else {
            System.out.println("Memcached client not configured - skipping test");
        }
    }

    @Test
    @Order(8)
    @DisplayName("Should handle multiple cache entries")
    void testMultipleCacheEntries() {
        if (memcachedClient != null) {
            // Arrange
            User user1 = new User();
            user1.setId(1L);
            user1.setUsername("user1");

            User user2 = new User();
            user2.setId(2L);
            user2.setUsername("user2");

            // Act
            memcachedClient.set("user:1", 3600, user1);
            memcachedClient.set("user:2", 3600, user2);
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Assert
            User cached1 = (User) memcachedClient.get("user:1");
            User cached2 = (User) memcachedClient.get("user:2");

            assertNotNull(cached1);
            assertNotNull(cached2);
            assertEquals("user1", cached1.getUsername());
            assertEquals("user2", cached2.getUsername());

            // Cleanup
            memcachedClient.delete("user:1");
            memcachedClient.delete("user:2");
        } else {
            System.out.println("Memcached client not configured - skipping test");
        }
    }
}
