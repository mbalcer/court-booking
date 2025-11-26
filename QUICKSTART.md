# Quick Start - Manual Testing

## 🚀 Fastest Way to Test (No Kafka Required)

### 1. Start the Application
```bash
./gradlew bootRun --args='--spring.profiles.active=test'
```

Wait for: `Started CourtBookingApplication in X.XXX seconds`

### 2. Run the Test Script
```bash
./test-api.sh
```

That's it! The script will run all test scenarios automatically.

---

## 🐳 Using Docker Compose (With Kafka)

### 1. Start All Dependencies
```bash
docker-compose up -d
```

### 2. Start the Application
```bash
./gradlew bootRun
```

### 3. Run Tests
```bash
./test-api.sh
```

### 4. View Kafka Events
- **Kafka UI:** http://localhost:8090
- **Or console:** `docker exec -it court-booking-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic booking-created --from-beginning`

### 5. Stop Everything
```bash
docker-compose down
```

See **DOCKER.md** for detailed Docker commands and troubleshooting.

---

## 📋 Manual Testing (Using curl)

### Create a booking:
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"date": "2025-12-01", "start": "10:00", "end": "11:00"}'
```

### Test overlapping (should fail):
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"date": "2025-12-01", "start": "10:30", "end": "11:30"}'
```

---

## 🗄️ View Database

1. Open: http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:courtbookingdb`
3. Username: `sa`
4. Password: (empty)
5. Query: `SELECT * FROM bookings;`

---

## 📮 Using Postman

Import `postman-collection.json` for ready-to-use test requests.

---

## 📊 With Kafka (Full Setup)

**Recommended:** Use Docker Compose (see section above)

**Alternative:** Manual Docker setup (see **DOCKER.md** or **TESTING.md** for detailed instructions)

---

## 🧪 Business Rules Being Tested

| Rule | Test Case | Expected |
|------|-----------|----------|
| Valid booking | 10:00-11:00 | ✅ 201 Created |
| Adjacent slots | 11:00-12:00 after 10:00-11:00 | ✅ 201 Created |
| Overlapping | 10:30-11:30 overlaps 10:00-11:00 | ❌ 400 Error |
| Before hours | 07:00-08:00 (opens 08:00) | ❌ 400 Error |
| After hours | 19:30-21:00 (closes 20:00) | ❌ 400 Error |
| Invalid slot | End before start | ❌ 400 Error |

---

## 📄 Full Documentation

- **TESTING.md** - Comprehensive testing guide
- **CLAUDE.md** - Architecture and design documentation
