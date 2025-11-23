#!/bin/bash

# Tennis Court Booking API - Test Script
# This script provides quick commands to test the REST API

API_URL="http://localhost:8080/api/bookings"

echo "=========================================="
echo "Tennis Court Booking API - Test Script"
echo "=========================================="
echo ""

# Function to print test header
print_test() {
    echo ""
    echo "----------------------------------------"
    echo "$1"
    echo "----------------------------------------"
}

# Test 1: Valid booking
print_test "Test 1: Create a valid booking (10:00-11:00)"
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-15",
    "start": "10:00",
    "end": "11:00"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
echo ""

# Test 2: Another valid booking (no overlap)
print_test "Test 2: Create another valid booking (14:00-15:30)"
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-15",
    "start": "14:00",
    "end": "15:30"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
echo ""

# Test 3: Before opening hours
print_test "Test 3: Booking before opening hours (07:00-08:00) - Should FAIL"
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-15",
    "start": "07:00",
    "end": "08:00"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
echo ""

# Test 4: After closing hours
print_test "Test 4: Booking after closing hours (19:30-21:00) - Should FAIL"
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-15",
    "start": "19:30",
    "end": "21:00"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
echo ""

# Test 5: Overlapping booking
print_test "Test 5: Overlapping booking (10:30-11:30) - Should FAIL"
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-15",
    "start": "10:30",
    "end": "11:30"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
echo ""

# Test 6: Invalid time slot (end before start)
print_test "Test 6: Invalid time slot - end before start (15:00-14:00) - Should FAIL"
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-15",
    "start": "15:00",
    "end": "14:00"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
echo ""

# Test 7: Adjacent bookings (should succeed)
print_test "Test 7a: First adjacent booking (09:00-10:00)"
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-16",
    "start": "09:00",
    "end": "10:00"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
echo ""

print_test "Test 7b: Second adjacent booking (10:00-11:00) - Should SUCCEED"
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-16",
    "start": "10:00",
    "end": "11:00"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
echo ""

# Test 8: Edge case - booking at opening time
print_test "Test 8: Booking at exact opening time (08:00-09:00)"
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-17",
    "start": "08:00",
    "end": "09:00"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
echo ""

# Test 9: Edge case - booking ending at closing time
print_test "Test 9: Booking ending at exact closing time (19:00-20:00)"
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-17",
    "start": "19:00",
    "end": "20:00"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
echo ""

echo ""
echo "=========================================="
echo "Test script completed!"
echo "=========================================="
echo ""
echo "Check the H2 database console at: http://localhost:8080/h2-console"
echo "JDBC URL: jdbc:h2:mem:courtdb"
echo "Username: sa"
echo "Password: (empty)"
echo ""
