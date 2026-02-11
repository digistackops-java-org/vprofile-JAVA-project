# VProfile Application - Test Suite Documentation

## Overview
This comprehensive test suite includes **Unit Tests**, **Integration Tests**, and **API Tests** for the VProfile Java application, along with health check endpoints for Kubernetes deployment.

## Table of Contents
1. [Test Structure](#test-structure)
2. [Health Check Endpoints](#health-check-endpoints)
3. [Running Tests](#running-tests)
4. [Test Coverage](#test-coverage)
5. [CI/CD Integration](#cicd-integration)
6. [Troubleshooting](#troubleshooting)

---

## Test Structure

### 📁 Directory Structure
```
src/
├── main/
│   └── java/
│       └── com/visualpathit/account/
│           ├── controller/
│           │   └── HealthCheckController.java     ⭐ NEW
│           ├── model/
│           ├── repository/
│           └── service/
└── test/
    ├── java/
    │   └── com/visualpathit/account/
    │       ├── service/
    │       │   └── UserServiceTest.java           (Unit Tests)
    │       ├── controller/
    │       │   └── UserControllerTest.java        (Unit Tests)
    │       ├── repository/
    │       │   └── UserRepositoryTest.java        (Unit Tests)
    │       ├── integration/
    │       │   ├── UserIntegrationTest.java       (Integration Tests)
    │       │   ├── DatabaseIntegrationTest.java   (Integration Tests)
    │       │   ├── MemcachedIntegrationTest.java  (Integration Tests)
    │       │   └── RabbitMQIntegrationTest.java   (Integration Tests)
    │       └── api/
    │           ├── UserApiTest.java               (API Tests)
    │           ├── HealthCheckApiTest.java        (API Tests)
    │           └── AuthenticationApiTest.java     (API Tests)
    └── resources/
        └── application-test.properties
```

### Test Categories

#### 1. **Unit Tests** (Fast, Isolated)
- **Purpose**: Test individual components in isolation
- **Execution Time**: < 5 seconds
- **Dependencies**: Mocked
- **Files**:
  - `UserServiceTest.java` - Service layer logic
  - `UserControllerTest.java` - REST controller endpoints
  - `UserRepositoryTest.java` - Custom repository methods

#### 2. **Integration Tests** (Medium Speed, Real Dependencies)
- **Purpose**: Test components working together
- **Execution Time**: 10-30 seconds
- **Dependencies**: Real database (H2), Spring context
- **Files**:
  - `UserIntegrationTest.java` - Full stack user operations
  - `DatabaseIntegrationTest.java` - JPA and database operations
  - `MemcachedIntegrationTest.java` - Caching functionality
  - `RabbitMQIntegrationTest.java` - Message queue operations

#### 3. **API Tests** (End-to-End)
- **Purpose**: Test REST API endpoints
- **Execution Time**: 15-45 seconds
- **Dependencies**: Full application context
- **Tools**: REST Assured
- **Files**:
  - `UserApiTest.java` - User CRUD operations
  - `HealthCheckApiTest.java` - Health endpoints
  - `AuthenticationApiTest.java` - Auth flows

---

## Health Check Endpoints

### ⭐ NEW: Health Check Controller

The application now includes Kubernetes-ready health check endpoints:

### Endpoints

#### 1. **Liveness Probe** - `/health/live`
- **Purpose**: Checks if the application is running
- **Response Code**: Always `200 OK` (if app is alive)
- **Use Case**: Kubernetes liveness probe
- **Response Example**:
```json
{
  "status": "UP",
  "timestamp": 1706789012345,
  "service": "vprofile-app",
  "version": "1.0.0"
}
```

#### 2. **Readiness Probe** - `/health/ready`
- **Purpose**: Checks if the application is ready to serve traffic
- **Response Codes**: 
  - `200 OK` - Application is ready
  - `503 Service Unavailable` - Not ready
- **Use Case**: Kubernetes readiness probe
- **Checks**:
  - Database connectivity
  - Memory usage (< 90%)
- **Response Example**:
```json
{
  "status": "UP",
  "timestamp": 1706789012345,
  "checks": {
    "database": "UP",
    "memory": {
      "status": "UP",
      "used": 524288000,
      "max": 2147483648,
      "usagePercent": "24.40%"
    }
  },
  "service": "vprofile-app"
}
```

#### 3. **General Health** - `/health`
- **Purpose**: Combined health check
- **Behavior**: Same as `/health/ready`

### Kubernetes Configuration Example

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: vprofile-app
spec:
  containers:
  - name: vprofile
    image: vprofile:latest
    ports:
    - containerPort: 8080
    livenessProbe:
      httpGet:
        path: /health/live
        port: 8080
      initialDelaySeconds: 30
      periodSeconds: 10
      timeoutSeconds: 5
      failureThreshold: 3
    readinessProbe:
      httpGet:
        path: /health/ready
        port: 8080
      initialDelaySeconds: 10
      periodSeconds: 5
      timeoutSeconds: 3
      failureThreshold: 3
```

---

## Running Tests

### Prerequisites
```bash
# Required
- Java 17 or 21
- Maven 3.9+
- MySQL 8 (for production, H2 for tests)

# Optional (for integration tests)
- Memcached
- RabbitMQ
- Docker (for TestContainers)
```

### 1. Run ALL Tests
```bash
mvn clean test verify
```

### 2. Run ONLY Unit Tests (Fastest)
```bash
# Option 1: Using Maven profile
mvn clean test -P unit-tests

# Option 2: Exclude integration and API tests
mvn clean test -Dtest=!*IntegrationTest,!*ApiTest
```

### 3. Run ONLY Integration Tests
```bash
# Using Maven profile
mvn clean verify -P integration-tests

# Or using Failsafe plugin
mvn clean integration-test
```

### 4. Run ONLY API Tests
```bash
mvn clean verify -P api-tests
```

### 5. Run Specific Test Class
```bash
# Unit test
mvn test -Dtest=UserServiceTest

# Integration test
mvn verify -Dit.test=UserIntegrationTest

# API test
mvn verify -Dit.test=UserApiTest
```

### 6. Run Specific Test Method
```bash
mvn test -Dtest=UserServiceTest#testCreateUser_Success
```

### 7. Run Tests with Code Coverage
```bash
mvn clean test jacoco:report

# View report at: target/site/jacoco/index.html
```

### 8. Run Tests in Parallel
```bash
mvn clean test -T 4  # Use 4 threads
```

---

## Test Coverage

### Current Coverage Goals
- **Line Coverage**: Minimum 70%
- **Branch Coverage**: Minimum 60%
- **Method Coverage**: Minimum 75%

### Generate Coverage Report
```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html  # macOS/Linux
start target/site/jacoco/index.html # Windows
```

### Coverage by Module

| Module | Target Coverage | Current |
|--------|----------------|---------|
| Service Layer | 80% | TBD |
| Controller Layer | 75% | TBD |
| Repository Layer | 85% | TBD |
| Integration | 70% | TBD |
| API | 70% | TBD |

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Test Suite

on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run Unit Tests
        run: mvn clean test -P unit-tests

  integration-tests:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: testdb
        ports:
          - 3306:3306
      rabbitmq:
        image: rabbitmq:3-management
        ports:
          - 5672:5672
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run Integration Tests
        run: mvn clean verify -P integration-tests

  api-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run API Tests
        run: mvn clean verify -P api-tests
      - name: Upload Coverage
        uses: codecov/codecov-action@v3
```

### Jenkins Pipeline Example

```groovy
pipeline {
    agent any
    
    stages {
        stage('Unit Tests') {
            steps {
                sh 'mvn clean test -P unit-tests'
            }
        }
        
        stage('Integration Tests') {
            steps {
                sh 'mvn clean verify -P integration-tests'
            }
        }
        
        stage('API Tests') {
            steps {
                sh 'mvn clean verify -P api-tests'
            }
        }
        
        stage('Coverage Report') {
            steps {
                sh 'mvn jacoco:report'
                publishHTML([
                    reportDir: 'target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: 'Code Coverage'
                ])
            }
        }
    }
    
    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            junit '**/target/failsafe-reports/*.xml'
        }
    }
}
```

---

## Test Configuration

### Application Test Properties
Located at: `src/test/resources/application-test.properties`

Key configurations:
- H2 in-memory database
- Disabled external services (Memcached, RabbitMQ) for isolated unit tests
- Debug logging enabled
- Test-specific security settings

### Custom Test Profiles

Activate test profile:
```bash
mvn test -Dspring.profiles.active=test
```

---

## Troubleshooting

### Common Issues

#### 1. **Tests Fail: "Connection refused" (Database)**
```bash
# Ensure H2 is in dependencies
mvn dependency:tree | grep h2

# Check application-test.properties
cat src/test/resources/application-test.properties
```

#### 2. **Tests Fail: Memcached/RabbitMQ Connection**
```bash
# For unit tests, these should be disabled
# Check test configuration or use @MockBean

# For integration tests, start services:
docker run -d -p 11211:11211 memcached
docker run -d -p 5672:5672 rabbitmq
```

#### 3. **Out of Memory Errors**
```bash
# Increase Maven memory
export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"

# Or in pom.xml
mvn test -Darguments="-Xmx2048m"
```

#### 4. **Port Already in Use**
```bash
# Random port for tests (default in test config)
# Or specify port
mvn test -Dserver.port=9090
```

#### 5. **Slow Test Execution**
```bash
# Run in parallel
mvn test -T 4

# Run only fast tests
mvn test -Dgroups=fast
```

### Debug Tests

```bash
# Run with debug output
mvn test -X

# Run single test with debug
mvn test -Dtest=UserServiceTest -X

# Run with JVM debug port
mvn test -Dmaven.surefire.debug
```

---

## Test Execution Commands Reference

```bash
# Quick Reference Card

# Run all tests
mvn clean verify

# Unit tests only (< 5 sec)
mvn test -P unit-tests

# Integration tests only (< 30 sec)
mvn verify -P integration-tests

# API tests only (< 45 sec)
mvn verify -P api-tests

# Specific test class
mvn test -Dtest=ClassName

# Specific test method
mvn test -Dtest=ClassName#methodName

# With coverage
mvn clean test jacoco:report

# Skip tests (build only)
mvn clean package -DskipTests

# Parallel execution
mvn test -T 4
```

---

## Next Steps

1. **Add these files to your project**:
   - Copy `HealthCheckController.java` to `src/main/java/com/visualpathit/account/controller/`
   - Copy all test files to appropriate test directories
   - Copy `application-test.properties` to `src/test/resources/`
   - Update `pom.xml` with test dependencies

2. **Configure your models and services**:
   - Ensure `User` model exists with required fields
   - Implement `UserService` and `UserRepository`
   - Add security configuration

3. **Run the tests**:
   ```bash
   mvn clean test
   ```

4. **Deploy with health checks**:
   - Use `/health/live` for Kubernetes liveness probe
   - Use `/health/ready` for Kubernetes readiness probe

---

## Support

For issues or questions:
- Check test logs: `target/surefire-reports/`
- Review coverage: `target/site/jacoco/index.html`
- Enable debug logging: `-X` flag

**Happy Testing! 🚀**
