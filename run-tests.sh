#!/bin/bash

# VProfile Health Check Test Runner
# This script runs all tests and provides a summary

echo "=========================================="
echo "VProfile Health Check - Test Runner"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ $2${NC}"
    else
        echo -e "${RED}✗ $2${NC}"
    fi
}

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean > /dev/null 2>&1
print_status $? "Clean completed"
echo ""

# Compile
echo "Compiling project..."
mvn compile -q
COMPILE_STATUS=$?
print_status $COMPILE_STATUS "Compilation"
echo ""

if [ $COMPILE_STATUS -ne 0 ]; then
    echo -e "${RED}Compilation failed. Please fix errors and try again.${NC}"
    exit 1
fi

# Run Unit Tests
echo "=========================================="
echo "Running Unit Tests (3 tests)..."
echo "=========================================="
mvn test -Dtest=HealthControllerTest
UNIT_TEST_STATUS=$?
print_status $UNIT_TEST_STATUS "Unit Tests"
echo ""

# Run Integration Tests
echo "=========================================="
echo "Running Integration Tests (3 tests)..."
echo "=========================================="
mvn test -Dtest=HealthControllerIT
INTEGRATION_TEST_STATUS=$?
print_status $INTEGRATION_TEST_STATUS "Integration Tests"
echo ""

# Run API Tests
echo "=========================================="
echo "Running API Tests (3 tests)..."
echo "=========================================="
mvn test -Dtest=HealthApiTest
API_TEST_STATUS=$?
print_status $API_TEST_STATUS "API Tests"
echo ""

# Summary
echo "=========================================="
echo "Test Summary"
echo "=========================================="
print_status $UNIT_TEST_STATUS "Unit Tests (3 tests)"
print_status $INTEGRATION_TEST_STATUS "Integration Tests (3 tests)"
print_status $API_TEST_STATUS "API Tests (3 tests)"
echo ""

# Overall status
if [ $UNIT_TEST_STATUS -eq 0 ] && [ $INTEGRATION_TEST_STATUS -eq 0 ] && [ $API_TEST_STATUS -eq 0 ]; then
    echo -e "${GREEN}=========================================="
    echo -e "All Tests Passed! ✓"
    echo -e "==========================================${NC}"
    echo ""
    echo "Your application is ready to deploy!"
    echo ""
    echo "To build the WAR file, run:"
    echo "  mvn clean package"
    echo ""
    echo "To run the application, use:"
    echo "  mvn spring-boot:run"
    echo ""
    echo "Then test the health endpoints:"
    echo "  curl http://localhost:8080/health"
    echo "  curl http://localhost:8080/health/live"
    echo "  curl http://localhost:8080/health/ready"
    exit 0
else
    echo -e "${RED}=========================================="
    echo -e "Some Tests Failed ✗"
    echo -e "==========================================${NC}"
    echo ""
    echo "Please check the test output above for details."
    echo "Common issues:"
    echo "  - Database connection not configured"
    echo "  - Port 8080 already in use"
    echo "  - Missing dependencies"
    echo ""
    echo "Run individual test suites with:"
    echo "  mvn test -Dtest=HealthControllerTest"
    echo "  mvn test -Dtest=HealthControllerIT"
    echo "  mvn test -Dtest=HealthApiTest"
    exit 1
fi
