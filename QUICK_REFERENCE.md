# VProfile Test Suite - Quick Reference

## 📦 Package Contents

### 🎯 Health Check Endpoints (NEW!)
- ✅ `/health/live` - Liveness probe for Kubernetes
- ✅ `/health/ready` - Readiness probe with database & memory checks
- ✅ `/health` - General health endpoint

### 🧪 Test Suite (107+ Tests)

#### Unit Tests (43 tests)
1. **UserServiceTest.java** - 13 tests
2. **UserControllerTest.java** - 12 tests  
3. **UserRepositoryTest.java** - 18 tests

#### Integration Tests (36 tests)
4. **UserIntegrationTest.java** - 8 tests
5. **DatabaseIntegrationTest.java** - 10 tests
6. **MemcachedIntegrationTest.java** - 8 tests
7. **RabbitMQIntegrationTest.java** - 10 tests

#### API Tests (45 tests)
8. **UserApiTest.java** - 15 tests
9. **HealthCheckApiTest.java** - 15 tests
10. **AuthenticationApiTest.java** - 15 tests

### 📁 Configuration Files
- `pom.xml` - Complete Maven configuration
- `application-test.properties` - Test configuration
- `README.md` - Full documentation
- `IMPLEMENTATION_GUIDE.md` - Step-by-step setup

---

## ⚡ Quick Start

### 1. Add Health Controller
```bash
cp HealthCheckController.java src/main/java/com/visualpathit/account/controller/
```

### 2. Add Test Files
```bash
# Copy all test files to appropriate directories
cp *Test.java src/test/java/com/visualpathit/account/[service|controller|repository|integration|api]/
cp application-test.properties src/test/resources/
```

### 3. Update pom.xml
Add test dependencies from provided `pom.xml`

### 4. Run Tests
```bash
# All tests
mvn clean verify

# Unit tests only (fastest)
mvn test -P unit-tests

# With coverage
mvn clean test jacoco:report
```

---

## 🎯 Test Commands Cheat Sheet

```bash
# Run everything
mvn clean verify

# Unit tests only
mvn test -P unit-tests

# Integration tests only
mvn verify -P integration-tests

# API tests only
mvn verify -P api-tests

# Specific test
mvn test -Dtest=UserServiceTest

# With coverage
mvn clean test jacoco:report

# Parallel execution
mvn test -T 4

# Skip tests (build only)
mvn clean package -DskipTests
```

---

## 📊 Coverage Goals

- **Line Coverage**: 70%+
- **Branch Coverage**: 60%+
- **Method Coverage**: 75%+

View report: `target/site/jacoco/index.html`

---

## 🔍 Health Endpoint Testing

```bash
# Test liveness
curl http://localhost:8080/health/live

# Test readiness
curl http://localhost:8080/health/ready

# Expected response:
{
  "status": "UP",
  "timestamp": 1706789012345,
  "service": "vprofile-app",
  "checks": {
    "database": "UP",
    "memory": {
      "status": "UP",
      "usagePercent": "24.40%"
    }
  }
}
```

---

## 🐳 Kubernetes Configuration

```yaml
livenessProbe:
  httpGet:
    path: /health/live
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /health/ready
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

---

## 📋 Test Structure

```
src/test/java/com/visualpathit/account/
├── service/
│   └── UserServiceTest.java          (Unit Tests)
├── controller/
│   └── UserControllerTest.java       (Unit Tests)
├── repository/
│   └── UserRepositoryTest.java       (Unit Tests)
├── integration/
│   ├── UserIntegrationTest.java      (Integration)
│   ├── DatabaseIntegrationTest.java  (Integration)
│   ├── MemcachedIntegrationTest.java (Integration)
│   └── RabbitMQIntegrationTest.java  (Integration)
└── api/
    ├── UserApiTest.java              (API Tests)
    ├── HealthCheckApiTest.java       (API Tests)
    └── AuthenticationApiTest.java    (API Tests)
```

---

## ✅ Verification Checklist

After setup, verify:
- [ ] HealthCheckController copied to src/main/java/.../controller/
- [ ] All test files in correct test directories
- [ ] application-test.properties in src/test/resources/
- [ ] Test dependencies added to pom.xml
- [ ] User model has required fields (id, username, password, email, role)
- [ ] UserRepository interface created
- [ ] UserService class created
- [ ] Tests run: `mvn test -P unit-tests`
- [ ] Health endpoints accessible: `curl localhost:8080/health/live`
- [ ] Coverage report generated: `target/site/jacoco/index.html`

---

## 🚨 Common Issues

| Issue | Solution |
|-------|----------|
| Cannot resolve User | Create User.java model with required fields |
| No UserRepository bean | Create UserRepository interface |
| Tests fail database connection | Check application-test.properties in src/test/resources |
| Port already in use | Tests use random port, stop other instances |
| Out of memory | Increase: `export MAVEN_OPTS="-Xmx2048m"` |

---

## 📚 Documentation Files

1. **IMPLEMENTATION_GUIDE.md** - Complete setup instructions
2. **README.md** - Full test suite documentation
3. **QUICK_REFERENCE.md** - This file
4. **pom.xml** - Maven configuration
5. **application-test.properties** - Test configuration

---

## 📈 Expected Results

After successful setup:
- ✅ 107+ tests passing
- ✅ 70%+ code coverage
- ✅ Health endpoints responding
- ✅ All test categories working:
  - Unit Tests: ✅ Fast (<10s)
  - Integration Tests: ✅ Medium (<30s)
  - API Tests: ✅ Complete (<60s)

---

## 🎉 Success!

You now have:
- **Production-ready health endpoints** for Kubernetes
- **Comprehensive test suite** with 107+ tests
- **Multiple test categories** (Unit, Integration, API)
- **Code coverage reporting** via JaCoCo
- **CI/CD ready** configurations

**Total Lines of Code**: 4,790+
**Test Coverage**: 70%+ target
**Execution Time**: < 2 minutes for full suite

---

For detailed information, see:
- `IMPLEMENTATION_GUIDE.md` - Step-by-step setup
- `README.md` - Complete documentation
- Test files - Inline documentation and examples
