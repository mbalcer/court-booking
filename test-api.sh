#!/bin/bash

# Tennis Court Booking API - Manual Test Script
# This script runs through all the test scenarios

BASE_URL="http://localhost:8080/api/bookings"

echo "=========================================="
echo "Tennis Court Booking API - Test Suite"
echo "=========================================="
echo ""

# Color codes for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

run_test() {
    local test_name=$1
    local expected_code=$2
    local json_data=$3

    echo -e "${YELLOW}Test: ${test_name}${NC}"
    echo "Request: $json_data"

    response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
        -H "Content-Type: application/json" \
        -d "$json_data")

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" -eq "$expected_code" ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $http_code)"
    else
        echo -e "${RED}✗ FAIL${NC} (Expected HTTP $expected_code, got $http_code)"
    fi

    echo "Response: $body"
    echo ""
    sleep 1
}

# Check if server is running
echo "Checking if server is running on localhost:8080..."
if ! curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null; then
    if ! curl -s -o /dev/null http://localhost:8080 2>/dev/null; then
        echo -e "${RED}ERROR: Server is not running on localhost:8080${NC}"
        echo "Please start the application with: ./gradlew bootRun"
        exit 1
    fi
fi
echo -e "${GREEN}✓ Server is running${NC}"
echo ""

# Test 1: Valid booking
run_test "Create Valid Booking (10:00-11:00)" 201 '{
    "date": "2025-12-01",
    "start": "10:00",
    "end": "11:00"
}'

# Test 2: Another valid booking (different time)
run_test "Create Second Booking (14:00-15:30)" 201 '{
    "date": "2025-12-01",
    "start": "14:00",
    "end": "15:30"
}'

# Test 3: Adjacent booking (should succeed - no overlap)
run_test "Create Adjacent Booking (11:00-12:00)" 201 '{
    "date": "2025-12-01",
    "start": "11:00",
    "end": "12:00"
}'

# Test 4: Overlapping booking (should fail)
run_test "Overlapping Booking (Should Fail)" 400 '{
    "date": "2025-12-01",
    "start": "10:30",
    "end": "11:30"
}'

# Test 5: Before opening hours (should fail)
run_test "Before Opening Hours (Should Fail)" 400 '{
    "date": "2025-12-01",
    "start": "07:00",
    "end": "08:00"
}'

# Test 6: After closing hours (should fail)
run_test "After Closing Hours (Should Fail)" 400 '{
    "date": "2025-12-01",
    "start": "19:30",
    "end": "21:00"
}'

# Test 7: Invalid time slot (end before start)
run_test "Invalid Time Slot (Should Fail)" 400 '{
    "date": "2025-12-01",
    "start": "15:00",
    "end": "14:00"
}'

# Test 8: Booking at opening time (should succeed)
run_test "At Opening Time (08:00-09:00)" 201 '{
    "date": "2025-12-02",
    "start": "08:00",
    "end": "09:00"
}'

# Test 9: Booking ending at closing time (should succeed)
run_test "Ending at Closing Time (19:00-20:00)" 201 '{
    "date": "2025-12-02",
    "start": "19:00",
    "end": "20:00"
}'

echo "=========================================="
echo "Test Suite Completed"
echo "=========================================="
echo ""
echo "To view database contents:"
echo "1. Open http://localhost:8080/h2-console"
echo "2. JDBC URL: jdbc:h2:mem:courtbookingdb"
echo "3. Username: sa"
echo "4. Password: (leave empty)"
echo "5. Run: SELECT * FROM bookings;"
echo ""
echo "To view Kafka events (if Kafka is running):"
echo "docker exec -it kafka kafka-console-consumer \\"
echo "  --bootstrap-server localhost:9092 \\"
echo "  --topic booking-created \\"
echo "  --from-beginning"
