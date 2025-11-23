# Quick Reference Card - Manual Testing

## Start Infrastructure & Application

### 1. Start Docker Services (Kafka + Zookeeper)
```bash
docker-compose up -d
```

### 2. Start Application
```bash
./gradlew bootRun
```

### 3. Run Automated Test Script
```bash
./test-api.sh
```

## Quick Access URLs

- **Application:** http://localhost:8080
- **H2 Database Console:** http://localhost:8080/h2-console
- **Kafka UI:** http://localhost:8090

## Stop Services

```bash
# Stop application (Ctrl+C)
# Stop Docker services
docker-compose down
```

## Manual Test Commands

### Success Cases

**Valid Booking:**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-06-15", "start": "10:00", "end": "11:00"}'
```

**Adjacent Bookings (Touch but Don't Overlap):**
```bash
# First
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-06-16", "start": "09:00", "end": "10:00"}'

# Second (should succeed)
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-06-16", "start": "10:00", "end": "11:00"}'
```

### Failure Cases (Business Rules)

**Before Opening (08:00):**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-06-15", "start": "07:00", "end": "08:00"}'
```

**After Closing (20:00):**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-06-15", "start": "19:30", "end": "21:00"}'
```

**Overlapping:**
```bash
# Create first booking
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-06-15", "start": "10:00", "end": "11:00"}'

# Try overlapping (should fail)
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-06-15", "start": "10:30", "end": "11:30"}'
```

**Invalid Time Slot:**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-06-15", "start": "15:00", "end": "14:00"}'
```

## Database Console

**URL:** http://localhost:8080/h2-console

**Connection:**
- JDBC URL: `jdbc:h2:mem:courtdb`
- Username: `sa`
- Password: (empty)

**Query All Bookings:**
```sql
SELECT * FROM bookings ORDER BY booking_date, start_time;
```

**Count Bookings:**
```sql
SELECT COUNT(*) FROM bookings;
```

**Bookings by Date:**
```sql
SELECT * FROM bookings WHERE booking_date = '2024-06-15';
```

## Expected Responses

### Success (HTTP 201)
```json
{
  "id": 1,
  "date": "2024-06-15",
  "startTime": "10:00:00",
  "endTime": "11:00:00"
}
```

### Error (HTTP 400)
```json
{
  "timestamp": "2024-06-15T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Booking cannot start before opening time...",
  "path": "/api/bookings"
}
```

## Configuration

### Application Settings
- **Port:** 8080
- **Opening Hours:** 08:00 - 20:00
- **Database:** H2 in-memory (data lost on restart)
- **Kafka Topic:** booking-created (optional)

### Files
- **Config:** `src/main/resources/application.yaml`
- **Test Guide:** `TESTING.md`
- **Test Script:** `test-api.sh`

## Build & Test

```bash
# Clean build
./gradlew clean build

# Run unit tests only
./gradlew test

# Run specific test
./gradlew test --tests BookingDomainServiceTest
```

## Architecture

```
REST Controller → Application Service → Domain Service → Repository
                       ↓
                 Event Publisher
```

**Layers:**
1. **Adapter (REST)** - HTTP endpoints
2. **Application** - Use case orchestration
3. **Domain** - Business logic
4. **Adapter (Persistence)** - Database
5. **Adapter (Event)** - Kafka

## Key Business Rules

1. **Opening Hours:** 08:00 - 20:00
2. **No Overlapping Bookings:** Same date with overlapping times
3. **Valid Time Slots:** Start < End
4. **Adjacent OK:** 10:00-11:00 + 11:00-12:00 is allowed
