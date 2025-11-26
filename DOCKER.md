# Docker Setup Guide - Court Booking Application

This guide explains how to use Docker Compose to run all application dependencies.

## 📦 What's Included

The `docker-compose.yml` includes:

1. **Zookeeper** - Required for Kafka coordination
2. **Kafka** - Message broker for event publishing
3. **Kafka UI** - Web interface for monitoring Kafka (http://localhost:8090)

**Note:** The H2 database is embedded in the Spring Boot application and doesn't require a separate container.

---

## 🚀 Quick Start

### Start All Services

```bash
# Start all services in the background
docker-compose up -d

# View logs
docker-compose logs -f

# View logs for specific service
docker-compose logs -f kafka
```

### Stop All Services

```bash
# Stop services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

---

## 📋 Service Details

### Zookeeper
- **Port:** 2181
- **Purpose:** Kafka coordination and metadata management
- **Health Check:** Automatically verified before Kafka starts

### Kafka
- **Port:** 9092 (external), 29092 (internal)
- **Purpose:** Event publishing for booking created events
- **Topic:** `booking-created` (auto-created on first use)
- **Replication:** Single broker setup (suitable for development)

### Kafka UI
- **URL:** http://localhost:8090
- **Purpose:** Visual management and monitoring of Kafka
- **Features:**
  - View topics and messages
  - Monitor consumer groups
  - Browse message content
  - Create/delete topics

---

## 🔧 Common Operations

### Check Service Status

```bash
# Check all running services
docker-compose ps

# Check service health
docker-compose ps kafka
```

### View Kafka Topics

```bash
# List all topics
docker exec court-booking-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --list

# Create a topic manually (optional - auto-created by app)
docker exec court-booking-kafka kafka-topics \
  --create \
  --topic booking-created \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 1

# Describe a topic
docker exec court-booking-kafka kafka-topics \
  --describe \
  --topic booking-created \
  --bootstrap-server localhost:9092
```

### Consume Kafka Messages

```bash
# View all messages from beginning
docker exec -it court-booking-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic booking-created \
  --from-beginning

# View new messages only
docker exec -it court-booking-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic booking-created

# Press Ctrl+C to stop consuming
```

### Produce Test Messages

```bash
# Open producer console
docker exec -it court-booking-kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic booking-created

# Type messages and press Enter
# Press Ctrl+C to exit
```

---

## 🧪 Complete Testing Workflow

### 1. Start Dependencies

```bash
docker-compose up -d
```

Wait for services to be healthy (~30 seconds):

```bash
# Check status
docker-compose ps

# Expected output:
# NAME                        STATUS
# court-booking-kafka         Up (healthy)
# court-booking-kafka-ui      Up (healthy)
# court-booking-zookeeper     Up (healthy)
```

### 2. Start the Application

```bash
# Start with default profile (uses Kafka)
./gradlew bootRun

# Or build and run JAR
./gradlew build
java -jar build/libs/court-booking-0.0.1-SNAPSHOT.jar
```

### 3. Run Tests

```bash
# In another terminal
./test-api.sh
```

### 4. Monitor Kafka Events

**Option A: Command Line**
```bash
docker exec -it court-booking-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic booking-created \
  --from-beginning
```

**Option B: Kafka UI (Recommended)**
1. Open http://localhost:8090
2. Click on **Topics** → **booking-created**
3. Click **Messages** to see all events
4. Each booking creation publishes a message

### 5. Stop Everything

```bash
# Stop application (Ctrl+C in terminal running ./gradlew bootRun)

# Stop Docker services
docker-compose down
```

---

## 🔍 Troubleshooting

### Kafka Won't Start

**Symptom:** Kafka container keeps restarting

**Solution:**
```bash
# Check logs
docker-compose logs kafka

# Common issue: Port already in use
lsof -i :9092 | grep LISTEN

# Kill process using port
lsof -ti:9092 | xargs kill -9

# Restart
docker-compose restart kafka
```

### Zookeeper Connection Issues

**Symptom:** Application can't connect to Zookeeper

**Solution:**
```bash
# Check Zookeeper health
docker exec court-booking-zookeeper nc -z localhost 2181

# Restart Zookeeper and Kafka
docker-compose restart zookeeper kafka
```

### Topic Not Created

**Symptom:** `booking-created` topic doesn't exist

**Solution:**
```bash
# Manually create topic
docker exec court-booking-kafka kafka-topics \
  --create \
  --topic booking-created \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 1

# Verify
docker exec court-booking-kafka kafka-topics \
  --list \
  --bootstrap-server localhost:9092
```

### Clean Slate (Reset Everything)

```bash
# Stop and remove all containers, networks, and volumes
docker-compose down -v

# Remove old volumes
docker volume prune -f

# Restart
docker-compose up -d
```

### Port Conflicts

If ports 2181, 9092, or 8090 are already in use, edit `docker-compose.yml`:

```yaml
# Change external port (left side)
ports:
  - "9093:9092"  # Use 9093 instead of 9092

# Then update application.yaml:
spring:
  kafka:
    bootstrap-servers: localhost:9093
```

---

## 📊 Kafka UI Features

Access Kafka UI at **http://localhost:8090**

### View Topics
1. Navigate to **Topics**
2. Click on **booking-created**
3. See partition info, message count, configuration

### Browse Messages
1. Topics → **booking-created** → **Messages**
2. See all published events
3. View message content, headers, metadata
4. Filter by timestamp or offset

### Monitor Consumer Groups
1. Navigate to **Consumer Groups**
2. View **court-booking-group**
3. See lag, offset position, member info

---

## 🎯 Production Considerations

This `docker-compose.yml` is designed for **development and testing**. For production:

1. **Use external Kafka cluster** (managed service like Confluent Cloud, AWS MSK)
2. **Increase replication factor** (at least 3 for fault tolerance)
3. **Configure proper partitioning** based on load
4. **Enable authentication** (SASL/SSL)
5. **Set up monitoring** (Prometheus, Grafana)
6. **Configure persistence** with proper volume management
7. **Use separate H2/PostgreSQL/MySQL** instead of in-memory database

---

## 📖 Related Documentation

- **QUICKSTART.md** - Quick reference for testing
- **TESTING.md** - Comprehensive testing guide
- **CLAUDE.md** - Architecture and design documentation

---

## 🔗 Useful Links

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Confluent Docker Images](https://hub.docker.com/r/confluentinc/cp-kafka)
- [Kafka UI GitHub](https://github.com/provectus/kafka-ui)

---

**Happy Kafka Testing! 🎾📨**
