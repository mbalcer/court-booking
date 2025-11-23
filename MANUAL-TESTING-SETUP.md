# Manual Testing Setup - Complete

All configurations for Step 12 (Manual Testing) have been set up and are ready to use.

## What's Been Configured

### 1. Application Configuration (`src/main/resources/application.yaml`)
- ✅ **H2 Database** - In-memory database for testing
- ✅ **H2 Console** - Web UI at `/h2-console` for database inspection
- ✅ **JPA/Hibernate** - Auto-creates tables, shows SQL in logs
- ✅ **Kafka** - Configured for event publishing (optional)
- ✅ **Logging** - DEBUG level for application code, SQL query logging
- ✅ **Server Port** - 8080

### 2. Testing Documentation
- ✅ **TESTING.md** - Comprehensive manual testing guide with:
  - Step-by-step instructions
  - 9 detailed test scenarios (success + failure cases)
  - Expected requests and responses
  - H2 console instructions
  - Kafka testing (optional)
  - Troubleshooting guide

- ✅ **QUICK-REFERENCE.md** - Quick reference card with:
  - Common curl commands
  - Database queries
  - Configuration summary
  - Architecture overview

### 3. Automated Test Script
- ✅ **test-api.sh** - Executable script that runs all test scenarios
  - 9 automated tests covering all use cases
  - HTTP status code validation
  - Formatted output for easy reading

## Files Created/Updated

```
court-booking/
├── src/main/resources/
│   └── application.yaml          # Updated with full configuration
├── TESTING.md                    # Comprehensive testing guide
├── QUICK-REFERENCE.md            # Quick reference card
├── test-api.sh                   # Automated test script (executable)
└── MANUAL-TESTING-SETUP.md       # This file
```

## How to Use

### Quick Start (3 Steps)

1. **Start the application:**
   ```bash
   ./gradlew bootRun
   ```

2. **Run the automated test script:**
   ```bash
   ./test-api.sh
   ```

3. **Check the database:**
   - Open browser: http://localhost:8080/h2-console
   - Connect with: `jdbc:h2:mem:courtdb` / username: `sa` / no password
   - Run: `SELECT * FROM bookings;`

### Detailed Testing

For detailed testing instructions, see **TESTING.md** which includes:
- 9 complete test scenarios with expected results
- H2 database console setup
- Optional Kafka integration testing
- Troubleshooting guide
- Full explanation of what happens internally

### Quick Commands

For quick copy-paste commands, see **QUICK-REFERENCE.md** which includes:
- One-liner curl commands for all scenarios
- Common SQL queries
- Configuration summary
- Architecture diagram

## Test Coverage

The test scenarios cover:

| Scenario | Expected Result | HTTP Code |
|----------|----------------|-----------|
| Valid booking | Success | 201 |
| Non-overlapping bookings | Success | 201 |
| Adjacent bookings | Success | 201 |
| Before opening hours | Failure | 400 |
| After closing hours | Failure | 400 |
| Overlapping booking | Failure | 400 |
| Invalid time slot | Failure | 400 |
| At opening time (edge) | Success | 201 |
| At closing time (edge) | Success | 201 |

## Business Rules Being Tested

1. **Opening Hours Policy**
   - Court opens at 08:00
   - Court closes at 20:00
   - Bookings must fall within these hours

2. **Overlapping Reservations Policy**
   - No two bookings can overlap on the same date
   - Adjacent bookings (touching times) are allowed
   - Example: 10:00-11:00 and 11:00-12:00 is OK

3. **Time Slot Validation**
   - Start time must be before end time
   - All fields are required
   - Date and time formats must be valid

## Architecture Layers Tested

```
┌─────────────────────────────────────────┐
│   REST API (BookingController)         │ ← Test with curl/Postman
├─────────────────────────────────────────┤
│   Application Layer (Use Cases)        │ ← Orchestration
├─────────────────────────────────────────┤
│   Domain Layer (Business Logic)        │ ← Business rules validated
├─────────────────────────────────────────┤
│   Persistence Layer (H2 Database)      │ ← Check with H2 console
├─────────────────────────────────────────┤
│   Event Layer (Kafka - optional)       │ ← Optional verification
└─────────────────────────────────────────┘
```

## What to Observe

### In the Application Logs:
- ✅ Request received by controller
- ✅ Domain service validates business rules
- ✅ SQL INSERT statements (if successful)
- ✅ Event published to Kafka (if Kafka running)
- ✅ Response sent to client

### In the H2 Console:
- ✅ Bookings table auto-created
- ✅ Auto-generated IDs (1, 2, 3...)
- ✅ Correct date and time values stored
- ✅ No overlapping bookings for same date

### In the API Responses:
- ✅ HTTP 201 for successful bookings
- ✅ HTTP 400 for business rule violations
- ✅ Descriptive error messages
- ✅ Consistent JSON format

## Notes

- **Database:** H2 in-memory - data is lost on application restart (expected behavior)
- **Kafka:** Optional - application works without Kafka, events just won't be published
- **Port:** Application runs on port 8080 by default (configurable in application.yaml)
- **Opening Hours:** Configured as 08:00-20:00 in BookingConfiguration.java

## Next Steps

After manual testing:
1. Write integration tests (Step 12 - automated integration tests)
2. Add GET /api/bookings endpoint (Step 13)
3. Add DELETE /api/bookings/{id} endpoint (Step 13)
4. Consider adding Spring Boot Actuator for health checks
5. Consider adding API documentation (Swagger/OpenAPI)

## Troubleshooting

If you encounter issues:
1. Check **TESTING.md** → Troubleshooting section
2. Verify Java 21 is installed: `java -version`
3. Verify port 8080 is available: `lsof -i :8080`
4. Check application logs for errors
5. Verify H2 console connection details

## Summary

✅ All manual testing configurations are complete and ready to use
✅ Application can be started with `./gradlew bootRun`
✅ REST API can be tested with curl or the provided test script
✅ Database can be inspected via H2 console
✅ All business rules are enforced and can be verified
✅ Comprehensive documentation provided

**The application is fully configured for Step 12 manual testing!**
