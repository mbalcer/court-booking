# Manual Testing Guide

This guide provides step-by-step instructions for manually testing the Tennis Court Booking Application.

## Prerequisites

- Java 21 installed
- Gradle installed (or use the wrapper `./gradlew`)
- curl or Postman for API testing
- (Optional) Docker for Kafka testing

## Quick Start

### 1. Start the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

**What to look for in the logs:**
- `Started CourtBookingApplication` - Application started successfully
- `H2 console available at '/h2-console'` - Database console is ready
- `Tomcat started on port 8080` - Web server is running

### 2. Verify Application is Running

```bash
curl http://localhost:8080/actuator/health
```

If you get a 404, that's expected (actuator not configured). Try the main endpoint instead.

## Testing Scenarios

### Scenario 1: Create a Valid Booking (Success)

**Request:**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-15",
    "start": "10:00",
    "end": "11:00"
  }'
```

**Expected Response (HTTP 201):**
```json
{
  "id": 1,
  "date": "2024-06-15",
  "startTime": "10:00:00",
  "endTime": "11:00:00"
}
```

**What happens internally:**
- Request validated
- Domain service checks business rules (opening hours, no overlaps)
- Booking saved to H2 database with auto-generated ID
- Event published to Kafka (will fail if Kafka not running, but booking still succeeds)
- Response returned to client

---

### Scenario 2: Create Another Valid Booking (No Overlap)

**Request:**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-15",
    "start": "14:00",
    "end": "15:30"
  }'
```

**Expected Response (HTTP 201):**
```json
{
  "id": 2,
  "date": "2024-06-15",
  "startTime": "14:00:00",
  "endTime": "15:30:00"
}
```

---

### Scenario 3: Booking Before Opening Hours (Failure)

**Request:**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-15",
    "start": "07:00",
    "end": "08:00"
  }'
```

**Expected Response (HTTP 400):**
```json
{
  "timestamp": "2024-06-15T07:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Booking cannot start before opening time. Start: 07:00, Opening time: 08:00",
  "path": "/api/bookings"
}
```

**Note:** Opening hours are 08:00-20:00 (configured in `BookingConfiguration.java:38-40`)

---

### Scenario 4: Booking After Closing Hours (Failure)

**Request:**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-15",
    "start": "19:30",
    "end": "21:00"
  }'
```

**Expected Response (HTTP 400):**
```json
{
  "timestamp": "2024-06-15T19:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Booking cannot end after closing time. End: 21:00, Closing time: 20:00",
  "path": "/api/bookings"
}
```

---

### Scenario 5: Overlapping Booking (Failure)

First, create a booking (if you haven't already from Scenario 1):
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-15",
    "start": "10:00",
    "end": "11:00"
  }'
```

Then try to create an overlapping booking:
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-15",
    "start": "10:30",
    "end": "11:30"
  }'
```

**Expected Response (HTTP 400):**
```json
{
  "timestamp": "2024-06-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "The requested time slot overlaps with an existing booking. Requested: [2024-06-15 10:30-11:30], Existing booking ID: 1",
  "path": "/api/bookings"
}
```

---

### Scenario 6: Invalid Time Slot (End Before Start)

**Request:**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-15",
    "start": "15:00",
    "end": "14:00"
  }'
```

**Expected Response (HTTP 400):**
```json
{
  "timestamp": "2024-06-15T15:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Start time must be before end time",
  "path": "/api/bookings"
}
```

---

### Scenario 7: Missing Required Fields

**Request:**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-15"
  }'
```

**Expected Response (HTTP 400):**
```json
{
  "timestamp": "2024-06-15T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Required request body is missing or invalid",
  "path": "/api/bookings"
}
```

---

### Scenario 8: Adjacent Bookings (Should Succeed)

Adjacent bookings that touch but don't overlap should succeed:

**First Booking:**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-16",
    "start": "09:00",
    "end": "10:00"
  }'
```

**Adjacent Booking (Should Succeed):**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-16",
    "start": "10:00",
    "end": "11:00"
  }'
```

**Expected:** Both should return HTTP 201 with booking details.

---

## Inspecting the Database (H2 Console)

### 1. Access H2 Console

Open your browser and navigate to:
```
http://localhost:8080/h2-console
```

### 2. Connect to Database

Use these connection details:
- **JDBC URL:** `jdbc:h2:mem:courtdb`
- **User Name:** `sa`
- **Password:** (leave empty)

Click **Connect**

### 3. Query Bookings

Run this SQL query to see all bookings:
```sql
SELECT * FROM bookings;
```

**Expected columns:**
- `id` - Auto-generated booking ID
- `booking_date` - Date of the booking
- `start_time` - Start time
- `end_time` - End time

### 4. Verify Data Persistence

After creating bookings via the REST API, you should see them in the database with:
- Unique IDs (1, 2, 3, ...)
- Correct date and time values
- No overlapping bookings for the same date

---

## Testing Kafka Event Publishing (Optional)

The application publishes `BookingCreatedEvent` to Kafka whenever a booking is successfully created. **Kafka is optional** - the application will run without it, but events won't be published.

### Option 1: Test Without Kafka

**What happens:**
- Application starts normally
- Bookings work correctly
- You'll see WARN logs: `Failed to send metadata after 60000 ms` (this is expected)
- Events are not published, but the application continues to function

### Option 2: Run Kafka Locally (Docker)

**1. Start Kafka with Docker Compose**

Create `docker-compose.yml`:
```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

**2. Start Kafka:**
```bash
docker-compose up -d
```

**3. Create the topic:**
```bash
docker exec -it <kafka-container-id> kafka-topics --create \
  --topic booking-created \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1
```

**4. Monitor events:**
```bash
docker exec -it <kafka-container-id> kafka-console-consumer \
  --topic booking-created \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

**5. Create a booking and verify event:**

Create a booking (using any successful request from above), then check the Kafka consumer output. You should see:
```json
{
  "booking_id": 1,
  "date": "2024-06-15",
  "start_time": "10:00:00",
  "end_time": "11:00:00"
}
```

---

## Monitoring Application Logs

### Important Log Messages to Look For

**Successful Booking:**
```
DEBUG c.t.c.a.s.BookingApplicationService : Reserving booking for date: 2024-06-15
DEBUG c.t.c.d.s.BookingDomainService      : Reserving booking for time slot: [2024-06-15 10:00-11:00]
DEBUG c.t.c.a.s.BookingApplicationService : Booking reserved successfully with ID: 1
INFO  c.t.c.a.o.e.BookingEventPublisherAdapter : Booking created event sent successfully for booking ID: 1
```

**Business Rule Violation:**
```
DEBUG c.t.c.d.p.OpeningHoursPolicy        : Validating time slot against opening hours: 08:00-20:00
ERROR c.t.c.a.i.w.e.GlobalExceptionHandler : Business exception: Booking cannot start before opening time...
```

**Database Operations:**
```
DEBUG org.hibernate.SQL : insert into bookings (booking_date, end_time, start_time, id) values (?, ?, ?, default)
TRACE o.h.t.d.s.BasicBinder : binding parameter [1] as [DATE] - [2024-06-15]
```

**Kafka Publishing (without Kafka running):**
```
WARN  org.apache.kafka.clients.NetworkClient : Connection to node -1 could not be established
```
This is **expected** if Kafka is not running. The application continues to work normally.

---

## Testing Checklist

Use this checklist to verify all functionality:

- [ ] Application starts without errors
- [ ] Can access H2 console at http://localhost:8080/h2-console
- [ ] Can create a valid booking (HTTP 201)
- [ ] Booking appears in database with auto-generated ID
- [ ] Cannot create booking before opening hours (HTTP 400)
- [ ] Cannot create booking after closing hours (HTTP 400)
- [ ] Cannot create overlapping bookings (HTTP 400)
- [ ] Can create adjacent bookings (back-to-back times)
- [ ] Invalid time slots are rejected (end before start)
- [ ] Missing fields return appropriate errors
- [ ] Error responses include descriptive messages
- [ ] Logs show successful SQL inserts
- [ ] (Optional) Events published to Kafka when available

---

## Troubleshooting

### Application Won't Start

**Problem:** Port 8080 already in use

**Solution:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change the port in application.yaml:
server:
  port: 8081
```

### Cannot Connect to H2 Console

**Problem:** H2 console not accessible

**Solution:**
1. Verify the application is running
2. Check the JDBC URL matches exactly: `jdbc:h2:mem:courtdb`
3. Ensure H2 console is enabled in `application.yaml`

### Kafka Warnings in Logs

**Problem:** `Failed to send metadata` warnings

**Solution:** This is normal if Kafka is not running. The application works fine without Kafka for testing the core booking functionality. Events simply won't be published.

### Database is Empty After Restart

**Problem:** All bookings disappear when restarting

**Solution:** This is expected behavior - H2 is configured as an in-memory database (`jdbc:h2:mem:courtdb`). Data is lost on restart. This is intentional for testing.

To persist data across restarts, change in `application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/courtdb  # File-based instead of in-memory
```

---

## Next Steps

After manual testing, consider:
1. Writing integration tests (Step 12)
2. Adding GET and DELETE endpoints (Step 13)
3. Adding Spring Boot Actuator for health checks
4. Configuring CORS for frontend integration
5. Adding API documentation with Swagger/OpenAPI

---

## Summary

This application demonstrates **Hexagonal Architecture** with:
- **Domain Layer:** Pure business logic (no framework dependencies)
- **Application Layer:** Use case orchestration with ports/adapters
- **Adapter Layer:** REST API, JPA persistence, Kafka events
- **Configuration Layer:** Spring dependency injection

All business rules are enforced at the domain level, ensuring data integrity and consistent behavior across all entry points.
