# Manual Testing Guide

This guide provides step-by-step instructions for manually testing the Tennis Court Booking Application.

## Prerequisites

- Java 21 installed
- Gradle installed (or use the wrapper `./gradlew`)
- Docker and Docker Compose installed
- curl or Postman for API testing

## Quick Start

### 1. Start Dependencies (Kafka + Zookeeper)

Start the required infrastructure using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- **Zookeeper** on port 2181 (Kafka coordination)
- **Kafka** on port 9092 (message broker)
- **Kafka UI** on port 8090 (web interface for monitoring)

**Verify services are running:**
```bash
docker-compose ps
```

All services should show status "Up" and "healthy".

**Access Kafka UI:**
Open your browser and navigate to http://localhost:8090 to monitor Kafka topics and messages.

### 2. Start the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

**What to look for in the logs:**
- `Started CourtBookingApplication` - Application started successfully
- `H2 console available at '/h2-console'` - Database console is ready
- `Tomcat started on port 8080` - Web server is running
- No Kafka connection errors (if Kafka is running)

### 3. Verify Application is Running

Try creating a booking:
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-06-15", "start": "10:00", "end": "11:00"}'
```

You should get an HTTP 201 response with the booking details.

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

## Verifying Kafka Event Publishing

The application publishes `BookingCreatedEvent` to Kafka whenever a booking is successfully created.

### Using Kafka UI (Recommended)

The easiest way to verify events is using the Kafka UI web interface:

**1. Ensure Docker Compose services are running:**
```bash
docker-compose ps
```

**2. Access Kafka UI:**
Open your browser and navigate to: **http://localhost:8090**

**3. View the `booking-created` topic:**
- Click on "Topics" in the left menu
- Find the `booking-created` topic
- Click "Messages" to see published events
- Events are automatically created when the application starts (Kafka auto-creates topics)

**4. Create a booking and verify:**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-06-15", "start": "10:00", "end": "11:00"}'
```

**5. Refresh the Kafka UI and verify the event:**
You should see a new message in the `booking-created` topic with content:
```json
{
  "booking_id": 1,
  "date": "2024-06-15",
  "start_time": "10:00:00",
  "end_time": "11:00:00"
}
```

### Using Kafka Console Consumer (Alternative)

If you prefer the command line:

**1. Monitor events in real-time:**
```bash
docker exec -it court-booking-kafka kafka-console-consumer \
  --topic booking-created \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

**2. Create a booking** (in another terminal):
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-06-15", "start": "10:00", "end": "11:00"}'
```

**3. Verify the event appears** in the consumer terminal

### Kafka Topic Management

**List all topics:**
```bash
docker exec -it court-booking-kafka kafka-topics \
  --list \
  --bootstrap-server localhost:9092
```

**Describe the booking-created topic:**
```bash
docker exec -it court-booking-kafka kafka-topics \
  --describe \
  --topic booking-created \
  --bootstrap-server localhost:9092
```

**Delete all messages** (reset for testing):
```bash
docker exec -it court-booking-kafka kafka-topics \
  --delete \
  --topic booking-created \
  --bootstrap-server localhost:9092
```

The topic will be auto-created again when the next event is published.

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

**Kafka Event Publishing:**
```
INFO  c.t.c.a.o.e.BookingEventPublisherAdapter : Booking created event sent successfully for booking ID: 1
```
This confirms the event was successfully published to Kafka.

**Kafka Connection Issues:**
```
WARN  org.apache.kafka.clients.NetworkClient : Connection to node -1 could not be established
```
If you see this, Kafka is not running. Start it with: `docker-compose up -d`

---

## Testing Checklist

Use this checklist to verify all functionality:

### Infrastructure Setup
- [ ] Docker Compose services are running (`docker-compose ps`)
- [ ] Kafka UI accessible at http://localhost:8090
- [ ] Application starts without errors

### Database Testing
- [ ] Can access H2 console at http://localhost:8080/h2-console
- [ ] Database connection works with correct credentials
- [ ] Bookings table is auto-created

### REST API Testing
- [ ] Can create a valid booking (HTTP 201)
- [ ] Booking appears in database with auto-generated ID
- [ ] Cannot create booking before opening hours (HTTP 400)
- [ ] Cannot create booking after closing hours (HTTP 400)
- [ ] Cannot create overlapping bookings (HTTP 400)
- [ ] Can create adjacent bookings (back-to-back times)
- [ ] Invalid time slots are rejected (end before start)
- [ ] Missing fields return appropriate errors
- [ ] Error responses include descriptive messages

### Kafka Event Testing
- [ ] Events published to Kafka successfully (check logs)
- [ ] Events visible in Kafka UI at http://localhost:8090
- [ ] Event format matches expected structure (booking_id, date, start_time, end_time)

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

### Docker Compose Services Not Starting

**Problem:** Services fail to start or show unhealthy status

**Solution:**
```bash
# Stop all services
docker-compose down

# Remove volumes and restart
docker-compose down -v
docker-compose up -d

# Check logs for specific service
docker-compose logs kafka
docker-compose logs zookeeper

# Verify services are healthy
docker-compose ps
```

### Kafka Connection Issues

**Problem:** Application shows `Failed to send metadata` warnings

**Solution:**
1. Verify Kafka is running: `docker-compose ps`
2. Check Kafka logs: `docker-compose logs kafka`
3. Restart Kafka if needed: `docker-compose restart kafka`
4. Ensure port 9092 is not blocked

### Port Conflicts

**Problem:** Docker services fail due to port conflicts

**Solution:**
- Port 8080 (Application) - Stop the application or change its port
- Port 8090 (Kafka UI) - Stop other services using this port
- Port 9092 (Kafka) - Check for other Kafka instances
- Port 2181 (Zookeeper) - Check for other Zookeeper instances

```bash
# Check what's using a port (e.g., 9092)
lsof -i :9092
```

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
1. Writing integration tests (Step 13)
2. Adding GET and DELETE endpoints (Step 14)
3. Adding Spring Boot Actuator for health checks
4. Configuring CORS for frontend integration
5. Adding API documentation with Swagger/OpenAPI

## Shutting Down

When you're done testing:

```bash
# Stop the application (Ctrl+C in the terminal running ./gradlew bootRun)

# Stop Docker Compose services
docker-compose down

# Or stop and remove volumes (clears Kafka topics)
docker-compose down -v
```

---

## Summary

This application demonstrates **Hexagonal Architecture** with:
- **Domain Layer:** Pure business logic (no framework dependencies)
- **Application Layer:** Use case orchestration with ports/adapters
- **Adapter Layer:** REST API, JPA persistence, Kafka events
- **Configuration Layer:** Spring dependency injection

All business rules are enforced at the domain level, ensuring data integrity and consistent behavior across all entry points.
