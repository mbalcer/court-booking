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

See **TESTING.md** for complete instructions including Kafka setup.

### Quick Kafka Start:
```bash
# Start Kafka with Docker
docker run -d --name zookeeper -p 2181:2181 \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  confluentinc/cp-zookeeper:7.5.0

docker run -d --name kafka -p 9092:9092 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:7.5.0

# Wait 10 seconds, then create topic
docker exec kafka kafka-topics --create \
  --topic booking-created \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 --partitions 1

# Start app (default profile uses Kafka)
./gradlew bootRun
```

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
