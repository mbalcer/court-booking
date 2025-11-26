# Manual Testing Guide - Court Booking Application

This guide provides step-by-step instructions for manually testing the Tennis Court Booking Application.

## Prerequisites

- Java 21 installed
- Docker installed (for Kafka)
- curl or Postman for API testing

## Quick Start

### Option 1: Testing WITHOUT Kafka (Simplest)

If you want to test just the REST API and database without setting up Kafka:

1. **Temporarily disable Kafka** by commenting out the Kafka configuration in your main application class or create a test profile.

2. **Start the application:**
   ```bash
   ./gradlew bootRun
   ```

3. **Skip to "Testing the REST API" section below**

### Option 2: Full Testing WITH Kafka (Complete Setup)

## Step 1: Start Kafka (Required for Event Publishing)

Start Kafka and Zookeeper using Docker:

```bash
# Create a Docker network
docker network create kafka-network

# Start Zookeeper
docker run -d \
  --name zookeeper \
  --network kafka-network \
  -p 2181:2181 \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  -e ZOOKEEPER_TICK_TIME=2000 \
  confluentinc/cp-zookeeper:7.5.0

# Start Kafka
docker run -d \
  --name kafka \
  --network kafka-network \
  -p 9092:9092 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:7.5.0

# Wait 10-15 seconds for Kafka to start

# Create the booking-created topic
docker exec kafka kafka-topics \
  --create \
  --topic booking-created \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 1
```

**Verify Kafka is running:**
```bash
docker ps | grep kafka
```

## Step 2: Start the Application

```bash
# Build the application
./gradlew clean build

# Run the application
./gradlew bootRun
```

**Expected output:**
```
Started CourtBookingApplication in X.XXX seconds
```

The application should start on **http://localhost:8080**

## Step 3: Testing the REST API

### Test 1: Create a Valid Booking

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2025-12-01",
    "start": "10:00",
    "end": "11:00"
  }'
```

**Expected Response (HTTP 201):**
```json
{
  "id": 1,
  "date": "2025-12-01",
  "startTime": "10:00:00",
  "endTime": "11:00:00"
}
```

### Test 2: Create Another Booking (Different Time)

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2025-12-01",
    "start": "14:00",
    "end": "15:30"
  }'
```

**Expected Response (HTTP 201):**
```json
{
  "id": 2,
  "date": "2025-12-01",
  "startTime": "14:00:00",
  "endTime": "15:30:00"
}
```

### Test 3: Overlapping Booking (Should Fail)

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2025-12-01",
    "start": "10:30",
    "end": "11:30"
  }'
```

**Expected Response (HTTP 400):**
```json
{
  "timestamp": "2025-12-01T...",
  "status": 400,
  "error": "Bad Request",
  "message": "The requested time slot overlaps with an existing booking. Requested: [2025-12-01 10:30-11:30], Existing booking ID: 1",
  "path": "/api/bookings"
}
```

### Test 4: Booking Before Opening Hours (Should Fail)

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2025-12-01",
    "start": "07:00",
    "end": "08:00"
  }'
```

**Expected Response (HTTP 400):**
```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "Booking cannot start before opening time. Start: 07:00, Opening time: 08:00",
  "path": "/api/bookings"
}
```

### Test 5: Booking After Closing Hours (Should Fail)

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2025-12-01",
    "start": "19:30",
    "end": "21:00"
  }'
```

**Expected Response (HTTP 400):**
```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "Booking cannot end after closing time. End: 21:00, Closing time: 20:00",
  "path": "/api/bookings"
}
```

### Test 6: Invalid Time Slot (End Before Start)

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2025-12-01",
    "start": "15:00",
    "end": "14:00"
  }'
```

**Expected Response (HTTP 400):**
```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "End time must be after start time",
  "path": "/api/bookings"
}
```

### Test 7: Adjacent Bookings (Should Succeed - No Overlap)

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2025-12-01",
    "start": "11:00",
    "end": "12:00"
  }'
```

**Expected Response (HTTP 201):**
Adjacent time slots (e.g., 10:00-11:00 and 11:00-12:00) do NOT overlap and should both succeed.

## Step 4: Verify Database Persistence

### Access H2 Console

1. Open browser: **http://localhost:8080/h2-console**
2. Use these connection settings:
   - **JDBC URL:** `jdbc:h2:mem:courtbookingdb`
   - **Username:** `sa`
   - **Password:** (leave empty)
3. Click **Connect**

### Query the Database

```sql
-- View all bookings
SELECT * FROM bookings;

-- Expected result: All created bookings with their IDs, dates, and times
```

## Step 5: Verify Kafka Events (Optional)

If you started Kafka, verify that events are being published:

### Consume Events from Kafka Topic

```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic booking-created \
  --from-beginning
```

**Expected output (for each successful booking):**
```json
{"booking_id":1,"date":"2025-12-01","start_time":"10:00:00","end_time":"11:00:00"}
{"booking_id":2,"date":"2025-12-01","start_time":"14:00:00","end_time":"15:30:00"}
```

Press **Ctrl+C** to stop consuming.

## Step 6: Check Application Logs

The application logs should show:

1. **SQL statements** (hibernate queries)
2. **Booking creation** (DEBUG level logs from domain service)
3. **Kafka publishing** (success/failure logs)

Example log entries:
```
DEBUG c.t.c.domain.service.BookingDomainService : Reserving booking for time slot: [2025-12-01 10:00-11:00]
DEBUG c.t.c.domain.service.BookingDomainService : Booking reserved successfully
INFO  c.t.c.a.o.e.BookingEventPublisherAdapter    : Successfully published booking created event for booking ID: 1
```

## Test Scenarios Summary

| Test Case | Date | Start | End | Expected Result | HTTP Code |
|-----------|------|-------|-----|----------------|-----------|
| Valid booking | 2025-12-01 | 10:00 | 11:00 | Success | 201 |
| Different time | 2025-12-01 | 14:00 | 15:30 | Success | 201 |
| Overlapping | 2025-12-01 | 10:30 | 11:30 | Overlap error | 400 |
| Before opening | 2025-12-01 | 07:00 | 08:00 | Opening hours error | 400 |
| After closing | 2025-12-01 | 19:30 | 21:00 | Closing hours error | 400 |
| Invalid slot | 2025-12-01 | 15:00 | 14:00 | Invalid time slot | 400 |
| Adjacent slots | 2025-12-01 | 11:00 | 12:00 | Success | 201 |

## Business Rules Being Tested

1. **Opening Hours Policy:** Courts are available from 08:00 to 20:00
2. **Overlapping Reservations Policy:** No two bookings can overlap on the same date
3. **Time Slot Validation:** End time must be after start time
4. **Adjacent Bookings:** Bookings that touch exactly (e.g., 10:00-11:00 and 11:00-12:00) are allowed

## Cleanup

### Stop the Application
Press **Ctrl+C** in the terminal running `./gradlew bootRun`

### Stop and Remove Kafka Containers (if started)

```bash
docker stop kafka zookeeper
docker rm kafka zookeeper
docker network rm kafka-network
```

## Troubleshooting

### Application won't start - Kafka connection error

**Symptom:** Application fails with "Connection to node -1 could not be established"

**Solution 1:** Test without Kafka (see "Option 1" above)

**Solution 2:** Make Kafka connection non-blocking by updating `BookingEventPublisherAdapter`:
- Comment out or make Kafka publishing asynchronous with proper error handling

### Port 8080 already in use

**Solution:**
```bash
# Find and kill process using port 8080
lsof -ti:8080 | xargs kill -9

# Or change port in application.yaml
server:
  port: 8081
```

### H2 Console shows "Database not found"

**Cause:** Application is not running

**Solution:** Start the application first with `./gradlew bootRun`, then access H2 console

## Using Postman (Alternative to curl)

If you prefer Postman:

1. **Import requests** or create manually:
   - Method: POST
   - URL: http://localhost:8080/api/bookings
   - Headers: `Content-Type: application/json`
   - Body (raw JSON): Use the JSON from curl examples above

2. **Create a collection** with all test scenarios for easy testing

## Next Steps

After manual testing is successful:
- Review Step 12 implementation plan for automated integration tests
- Consider implementing GET and DELETE endpoints (Step 13)
- Add more comprehensive test scenarios

---

**Happy Testing! 🎾**
