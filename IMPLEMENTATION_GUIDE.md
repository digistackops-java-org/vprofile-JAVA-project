# VProfile Test Suite - Implementation Guide

## 📋 What You've Received

This comprehensive test suite package includes:

### ✅ **Health Check Endpoints** (NEW)
- `HealthCheckController.java` - Kubernetes-ready liveness and readiness probes
  - `/health/live` - Liveness endpoint
  - `/health/ready` - Readiness endpoint with database and memory checks
  - `/health` - General health endpoint

### ✅ **Unit Tests** (10 Test Files)
1. **UserServiceTest.java** - 13 tests for service layer
   - User creation, update, deletion
   - Password validation
   - Role-based queries
   
2. **UserControllerTest.java** - 12 tests for REST controllers
   - CRUD operations
   - Validation scenarios
   - Security checks
   
3. **UserRepositoryTest.java** - 18 tests for data layer
   - Custom queries
   - Pagination
   - Sorting

### ✅ **Integration Tests** (4 Test Files)
4. **UserIntegrationTest.java** - 8 full-stack tests
   - End-to-end user operations
   - Database persistence verification
   - Transaction handling
   
5. **DatabaseIntegrationTest.java** - 10 tests
   - JPA operations
   - Entity lifecycle
   - Database constraints
   
6. **MemcachedIntegrationTest.java** - 8 tests
   - Cache operations
   - Expiration handling
   - Multiple entries
   
7. **RabbitMQIntegrationTest.java** - 10 tests
   - Message publishing
   - Queue operations
   - Message properties

### ✅ **API Tests** (3 Test Files)
8. **UserApiTest.java** - 15 REST API tests
   - Full CRUD via HTTP
   - Validation
   - Error handling
   
9. **HealthCheckApiTest.java** - 15 health endpoint tests
   - Liveness probe validation
   - Readiness probe with checks
   - Performance testing
   
10. **AuthenticationApiTest.java** - 15 authentication tests
    - Login/logout
    - Token management
    - Password reset flows

### ✅ **Configuration Files**
- `pom.xml` - Complete Maven configuration with all dependencies
- `application-test.properties` - Test-specific configuration
- `README.md` - Comprehensive documentation

---

## 🚀 Quick Start - Implementation Steps

### Step 1: Add Health Check Controller to Your Application

```bash
# Copy the HealthCheckController.java to your project
cp HealthCheckController.java src/main/java/com/visualpathit/account/controller/
```

**File location**: `src/main/java/com/visualpathit/account/controller/HealthCheckController.java`

### Step 2: Add Test Dependencies to pom.xml

Add these key dependencies to your existing `pom.xml`:

```xml
<!-- Add to <dependencies> section -->

<!-- H2 Database for Testing -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- REST Assured for API Testing -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.3.2</version>
    <scope>test</scope>
</dependency>

<!-- Spring Security Test -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

Or replace your entire `pom.xml` with the provided one.

### Step 3: Add Test Configuration

```bash
# Copy test configuration
cp application-test.properties src/test/resources/
```

### Step 4: Add Test Files

Create the test directory structure:
```bash
mkdir -p src/test/java/com/visualpathit/account/{service,controller,repository,integration,api}
```

Copy test files:
```bash
# Unit Tests
cp UserServiceTest.java src/test/java/com/visualpathit/account/service/
cp UserControllerTest.java src/test/java/com/visualpathit/account/controller/
cp UserRepositoryTest.java src/test/java/com/visualpathit/account/repository/

# Integration Tests
cp UserIntegrationTest.java src/test/java/com/visualpathit/account/integration/
cp DatabaseIntegrationTest.java src/test/java/com/visualpathit/account/integration/
cp MemcachedIntegrationTest.java src/test/java/com/visualpathit/account/integration/
cp RabbitMQIntegrationTest.java src/test/java/com/visualpathit/account/integration/

# API Tests
cp UserApiTest.java src/test/java/com/visualpathit/account/api/
cp HealthCheckApiTest.java src/test/java/com/visualpathit/account/api/
cp AuthenticationApiTest.java src/test/java/com/visualpathit/account/api/
```

### Step 5: Verify Your Project Structure

```
vprofile-project/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/visualpathit/account/
│   │   │       ├── controller/
│   │   │       │   ├── UserController.java
│   │   │       │   └── HealthCheckController.java  ⭐ NEW
│   │   │       ├── model/
│   │   │       │   └── User.java
│   │   │       ├── repository/
│   │   │       │   └── UserRepository.java
│   │   │       └── service/
│   │   │           └── UserService.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/
│       │   └── com/visualpathit/account/
│       │       ├── service/
│       │       │   └── UserServiceTest.java
│       │       ├── controller/
│       │       │   └── UserControllerTest.java
│       │       ├── repository/
│       │       │   └── UserRepositoryTest.java
│       │       ├── integration/
│       │       │   ├── UserIntegrationTest.java
│       │       │   ├── DatabaseIntegrationTest.java
│       │       │   ├── MemcachedIntegrationTest.java
│       │       │   └── RabbitMQIntegrationTest.java
│       │       └── api/
│       │           ├── UserApiTest.java
│       │           ├── HealthCheckApiTest.java
│       │           └── AuthenticationApiTest.java
│       └── resources/
│           └── application-test.properties
```

### Step 6: Run the Tests

```bash
# Run all tests
mvn clean test verify

# Run only unit tests (fastest)
mvn test -P unit-tests

# Run with coverage report
mvn clean test jacoco:report
```

---

## 🔧 Required Code Modifications

### 1. User Model (User.java)

Your `User` class should have these fields:

```java
package com.visualpathit.account.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String role;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Getters and setters
}
```

### 2. UserRepository Interface

```java
package com.visualpathit.account.repository;

import com.visualpathit.account.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(String role);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByUsernameContaining(String username);
    List<User> findByCreatedAtAfter(LocalDateTime date);
    void deleteByRole(String role);
    long countByRole(String role);
}
```

### 3. UserService Class

```java
package com.visualpathit.account.service;

import com.visualpathit.account.model.User;
import com.visualpathit.account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    public User updateUser(Long id, User user) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        user.setId(id);
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
    }
    
    public boolean validatePassword(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }
    
    public List<User> findByRole(String role) {
        return userRepository.findByRole(role);
    }
}
```

### 4. UserController Class

```java
package com.visualpathit.account.controller;

import com.visualpathit.account.model.User;
import com.visualpathit.account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        try {
            User created = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        try {
            User updated = userService.updateUser(id, user);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<User> searchUser(@RequestParam String username) {
        return userService.findByUsername(username)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

---

## 📊 Test Execution Commands

```bash
# 1. Run ALL tests (Unit + Integration + API)
mvn clean verify

# 2. Run ONLY Unit Tests (Fastest - < 10 seconds)
mvn test -P unit-tests

# 3. Run ONLY Integration Tests (Medium - < 30 seconds)
mvn verify -P integration-tests

# 4. Run ONLY API Tests (Slower - < 1 minute)
mvn verify -P api-tests

# 5. Run with Code Coverage
mvn clean test jacoco:report
# Open: target/site/jacoco/index.html

# 6. Run specific test class
mvn test -Dtest=UserServiceTest

# 7. Run in parallel (faster)
mvn test -T 4
```

---

## 🎯 Success Criteria

After implementation, you should see:

✅ **107+ Tests Passing**:
- 43+ Unit Tests
- 36+ Integration Tests  
- 45+ API Tests

✅ **Code Coverage**:
- Minimum 70% line coverage
- Detailed report in `target/site/jacoco/`

✅ **Health Endpoints Working**:
```bash
curl http://localhost:8080/health/live
# Should return: {"status":"UP", ...}

curl http://localhost:8080/health/ready
# Should return: {"status":"UP", "checks": {...}}
```

---

## 🐛 Common Issues & Solutions

### Issue 1: "Cannot resolve User class"
**Solution**: Ensure your `User` model exists with all required fields (id, username, password, email, role, createdAt)

### Issue 2: "Cannot find UserRepository"
**Solution**: Create the `UserRepository` interface extending `JpaRepository`

### Issue 3: "Tests fail with 'No qualifying bean'"
**Solution**: Ensure `@Service`, `@Repository`, `@Controller` annotations are present

### Issue 4: "Database connection errors"
**Solution**: Tests use H2 in-memory database. Check `application-test.properties` is in `src/test/resources/`

### Issue 5: "Port already in use"
**Solution**: Test configuration uses random port (`server.port=0`). If issue persists, stop other instances.

---

## 📦 What's Included - File Summary

| File | Purpose | Lines | Tests |
|------|---------|-------|-------|
| HealthCheckController.java | Kubernetes health endpoints | ~120 | - |
| UserServiceTest.java | Service layer unit tests | ~340 | 13 |
| UserControllerTest.java | Controller unit tests | ~320 | 12 |
| UserRepositoryTest.java | Repository unit tests | ~420 | 18 |
| UserIntegrationTest.java | Full stack integration | ~240 | 8 |
| DatabaseIntegrationTest.java | Database integration | ~280 | 10 |
| MemcachedIntegrationTest.java | Cache integration | ~340 | 8 |
| RabbitMQIntegrationTest.java | Messaging integration | ~380 | 10 |
| UserApiTest.java | REST API tests | ~400 | 15 |
| HealthCheckApiTest.java | Health endpoint tests | ~360 | 15 |
| AuthenticationApiTest.java | Auth flow tests | ~440 | 15 |
| pom.xml | Maven config with all dependencies | ~430 | - |
| application-test.properties | Test configuration | ~70 | - |
| README.md | Complete documentation | ~650 | - |

**Total: 4,790+ lines of production-ready test code**

---

## 🚀 Next Steps

1. **Copy files to your project** following Step-by-Step guide above
2. **Run initial test**: `mvn test -Dtest=HealthCheckApiTest`
3. **Fix any missing dependencies** or classes
4. **Run full test suite**: `mvn clean verify`
5. **Check coverage report**: Open `target/site/jacoco/index.html`
6. **Deploy with health checks** to Kubernetes

---

## 📞 Support

If you encounter issues:
1. Check the README.md for troubleshooting
2. Review test logs in `target/surefire-reports/`
3. Verify all required classes exist (User, UserService, UserRepository, etc.)
4. Ensure all dependencies are in pom.xml

**Good luck with your testing! 🎉**
