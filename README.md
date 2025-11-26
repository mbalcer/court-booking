# 🎾 Tennis Court Booking Application

A Spring Boot application for managing tennis court reservations, built with **Hexagonal Architecture** principles.

## 🚀 Quick Start

### Option 1: Simple Testing (No Kafka)

```bash
# Start application
./gradlew bootRun --args='--spring.profiles.active=test'

# Run automated tests
./test-api.sh
```

### Option 2: Full Setup with Docker Compose

```bash
# Start all dependencies (Kafka, Zookeeper, Kafka UI)
docker-compose up -d

# Start application
./gradlew bootRun

# Run tests
./test-api.sh

# View Kafka events
open http://localhost:8090  # Kafka UI
```

### Using Make (Convenience Commands)

```bash
# See all available commands
make help

# Quick start with Docker
make docker-up
make run
make test-api

# Stop everything
make docker-down
```

---

## 📚 Documentation

- **[QUICKSTART.md](QUICKSTART.md)** - Quick reference and common commands
- **[TESTING.md](TESTING.md)** - Comprehensive testing guide (9 KB)
- **[DOCKER.md](DOCKER.md)** - Docker Compose setup and operations
- **[CLAUDE.md](CLAUDE.md)** - Architecture, design, and development guide

---

## 🏗️ Architecture

This application follows **Hexagonal Architecture** (Ports & Adapters):

```
┌─────────────────────────────────────────────────────────┐
│              ADAPTERS LAYER                             │
│   REST API │ JPA Repositories │ Kafka Event Publishers  │
├─────────────────────────────────────────────────────────┤
│           APPLICATION LAYER                             │
│      Use Cases │ Application Services │ Ports           │
├─────────────────────────────────────────────────────────┤
│              DOMAIN LAYER (CORE)                        │
│   Entities │ Value Objects │ Services │ Policies        │
└─────────────────────────────────────────────────────────┘
```

### Key Features

- ✅ **Domain-Driven Design** - Pure domain logic, framework-independent
- ✅ **Hexagonal Architecture** - Clear separation of concerns
- ✅ **REST API** - Booking endpoints with global exception handling
- ✅ **Event Publishing** - Kafka integration for domain events
- ✅ **JPA Persistence** - H2 in-memory database
- ✅ **Business Policies** - Opening hours, overlap prevention
- ✅ **Comprehensive Tests** - 130+ unit tests across all layers

---

## 🛠️ Technology Stack

- **Java 21** - Latest LTS version
- **Spring Boot 3.5.7** - Application framework
- **Spring Data JPA** - Database persistence
- **Spring Kafka** - Event publishing
- **H2 Database** - In-memory database (development)
- **Lombok** - Boilerplate reduction
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Docker Compose** - Dependency orchestration
- **Gradle 8.14.3** - Build tool

---

## 📋 API Endpoints

### Create Booking

```bash
POST /api/bookings
Content-Type: application/json

{
  "date": "2025-12-01",
  "start": "10:00",
  "end": "11:00"
}
```

**Success Response (201 Created):**
```json
{
  "id": 1,
  "date": "2025-12-01",
  "startTime": "10:00:00",
  "endTime": "11:00:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2025-12-01T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "The requested time slot overlaps with an existing booking...",
  "path": "/api/bookings"
}
```

---

## 🧪 Business Rules

| Rule | Validation |
|------|------------|
| **Opening Hours** | Courts available 08:00 - 20:00 |
| **No Overlaps** | Bookings cannot overlap on same date |
| **Valid Time Slot** | End time must be after start time |
| **Adjacent Bookings** | Touching slots (10:00-11:00, 11:00-12:00) are allowed |

---

## 🐳 Docker Services

When using `docker-compose up`:

| Service | Port | Purpose |
|---------|------|---------|
| **Zookeeper** | 2181 | Kafka coordination |
| **Kafka** | 9092 | Event message broker |
| **Kafka UI** | 8090 | Web-based Kafka management |

---

## 🗄️ Database Access

### H2 Console

1. Start application: `./gradlew bootRun`
2. Open: http://localhost:8080/h2-console
3. Settings:
   - **JDBC URL:** `jdbc:h2:mem:courtbookingdb`
   - **Username:** `sa`
   - **Password:** _(empty)_
4. Query: `SELECT * FROM bookings;`

---

## 🧰 Common Commands

### Gradle

```bash
# Build project
./gradlew build

# Run tests
./gradlew test

# Run application
./gradlew bootRun

# Clean build
./gradlew clean
```

### Docker Compose

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Reset all data
docker-compose down -v
```

### Kafka

```bash
# List topics
make kafka-topics

# Consume events
make kafka-consume

# Open Kafka UI
make kafka-ui
```

---

## 📊 Testing

### Automated Test Script

```bash
./test-api.sh
```

Runs 9 test scenarios:
- ✅ 5 valid booking scenarios
- ❌ 4 error scenarios (overlap, hours, invalid)

### Manual Testing

```bash
# Create booking
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"date":"2025-12-01","start":"10:00","end":"11:00"}'
```

### Postman Collection

Import `postman-collection.json` for ready-to-use API requests.

---

## 📁 Project Structure

```
src/main/java/com/tennis/court_booking/
├── CourtBookingApplication.java    # Spring Boot entry point
├── config/                         # Spring configuration
├── domain/                         # Domain layer (pure Java)
│   ├── entity/                     # Entities (Booking)
│   ├── valueobject/                # Value objects (TimeSlot)
│   ├── policy/                     # Business policies
│   ├── service/                    # Domain services
│   └── exception/                  # Domain exceptions
├── application/                    # Application layer
│   ├── port/in/                    # Inbound ports (use cases)
│   ├── port/out/                   # Outbound ports (repositories)
│   ├── service/                    # Application services
│   └── mapper/                     # DTO mappers
└── adapter/                        # Adapter layer
    ├── in/web/                     # REST adapter
    └── out/                        # Outbound adapters
        ├── persistence/            # JPA adapter
        └── event/                  # Kafka adapter
```

---

## 🔧 Configuration

### Application Profiles

- **default** - Full setup with Kafka (requires `docker-compose up`)
- **test** - Simplified setup without Kafka

### Environment Variables

Copy `.env.example` to `.env` and customize:

```bash
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=default
```

---

## 🚧 Roadmap

- [x] Domain layer (entities, value objects, policies)
- [x] Application layer (use cases, services, mappers)
- [x] REST adapter (controllers, DTOs, exception handling)
- [x] Persistence adapter (JPA repositories)
- [x] Event adapter (Kafka publishing)
- [x] Configuration (Spring wiring)
- [x] Testing setup (Docker Compose, scripts)
- [ ] Integration tests (Step 12)
- [ ] Additional endpoints - GET, DELETE (Step 13)
- [ ] Deployment configuration

---

## 📖 Learn More

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)

---

## 🤝 Contributing

This is a learning project demonstrating hexagonal architecture principles. See **CLAUDE.md** for detailed architecture and design decisions.

---

## 📄 License

Educational project - Tennis Court Booking System

---

**Built with ❤️ using Hexagonal Architecture**
