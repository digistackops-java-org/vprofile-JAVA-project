#!/bin/bash

# Health Check Test Script
# Tests all health endpoints manually

# Configuration
HOST=${1:-localhost}
PORT=${2:-8080}
BASE_URL="http://${HOST}:${PORT}"

echo "=========================================="
echo "Testing VProfile Health Endpoints"
echo "=========================================="
echo "Target: $BASE_URL"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Function to test endpoint
test_endpoint() {
    local endpoint=$1
    local name=$2
    
    echo -e "${BLUE}Testing: $name${NC}"
    echo "URL: $BASE_URL$endpoint"
    echo ""
    
    response=$(curl -s -w "\nHTTP_CODE:%{http_code}" "$BASE_URL$endpoint")
    http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_CODE:/d')
    
    if [ "$http_code" == "200" ]; then
        echo -e "${GREEN}✓ Status: $http_code OK${NC}"
    else
        echo -e "${RED}✗ Status: $http_code${NC}"
    fi
    
    echo "Response:"
    echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
    echo ""
    echo "------------------------------------------"
    echo ""
}

# Check if server is running
echo "Checking if server is running..."
if curl -s --head --request GET "$BASE_URL/health" | grep "200 OK" > /dev/null; then
    echo -e "${GREEN}✓ Server is running${NC}"
    echo ""
else
    echo -e "${RED}✗ Server is not running or not responding${NC}"
    echo ""
    echo "Please start the application first:"
    echo "  mvn spring-boot:run"
    echo ""
    echo "Or specify a different host/port:"
    echo "  ./test-health-endpoints.sh your-host 8080"
    exit 1
fi

# Test all endpoints
test_endpoint "/health" "Basic Health Check"
test_endpoint "/health/live" "Liveness Probe"
test_endpoint "/health/ready" "Readiness Probe"

echo "=========================================="
echo "Testing Complete!"
echo "=========================================="
echo ""
echo "Summary:"
echo "• /health - Basic health status"
echo "• /health/live - Liveness probe (for Kubernetes)"
echo "• /health/ready - Readiness probe (checks dependencies)"
echo ""
