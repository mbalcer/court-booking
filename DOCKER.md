# Docker Infrastructure for Manual Testing

This file explains the Docker Compose setup for the Tennis Court Booking Application.

## Overview

The `docker-compose.yml` file provides the required infrastructure for manual testing:

- **Zookeeper** - Coordination service for Kafka
- **Kafka** - Message broker for event publishing
- **Kafka UI** - Web-based tool for monitoring Kafka topics and messages

## Services

### Zookeeper
- **Image:** confluentinc/cp-zookeeper:7.5.0
- **Port:** 2181
- **Purpose:** Manages Kafka cluster coordination and configuration
- **Container Name:** court-booking-zookeeper

### Kafka
- **Image:** confluentinc/cp-kafka:7.5.0
- **Ports:**
  - 9092 (external access)
  - 29092 (internal broker communication)
- **Purpose:** Message broker for publishing booking events
- **Container Name:** court-booking-kafka
- **Features:**
  - Auto-creates topics on first message
  - Single broker configuration
  - Configured for local development

### Kafka UI
- **Image:** provectuslabs/kafka-ui:latest
- **Port:** 8090
- **Purpose:** Web interface for monitoring Kafka
- **Container Name:** court-booking-kafka-ui
- **Access:** http://localhost:8090

## Usage

### Start All Services
```bash
docker-compose up -d
```

### Check Service Status
```bash
docker-compose ps
```

All services should show "Up" and "healthy" status.

### View Logs
```bash
# All services
docker-compose logs

# Specific service
docker-compose logs kafka
docker-compose logs zookeeper
docker-compose logs kafka-ui

# Follow logs in real-time
docker-compose logs -f kafka
```

### Stop All Services
```bash
docker-compose down
```

### Stop and Remove Volumes (Clears All Data)
```bash
docker-compose down -v
```

### Restart a Specific Service
```bash
docker-compose restart kafka
```

## Kafka UI Features

Access Kafka UI at **http://localhost:8090**

Features:
- **Topics:** View all Kafka topics (including `booking-created`)
- **Messages:** Browse messages in each topic
- **Consumers:** Monitor consumer groups
- **Configuration:** View broker and topic configuration

### Viewing Booking Events

1. Open http://localhost:8090
2. Click "Topics" in the left menu
3. Find the `booking-created` topic
4. Click "Messages" to see all published events
5. Events will appear as JSON with booking details

## Health Checks

All services have configured health checks:

- **Zookeeper:** Checks port 2181 connectivity
- **Kafka:** Verifies broker API versions endpoint
- **Kafka UI:** Checks actuator health endpoint

Services won't be marked as "healthy" until they're fully ready.

## Networking

All services run on a custom bridge network: `court-booking-network`

This allows:
- Services to communicate using container names
- Isolation from other Docker networks
- Easy service discovery

## Troubleshooting

### Services Won't Start

**Check Docker is running:**
```bash
docker ps
```

**Check logs for errors:**
```bash
docker-compose logs kafka
docker-compose logs zookeeper
```

**Restart services:**
```bash
docker-compose down
docker-compose up -d
```

### Port Conflicts

If ports are already in use:
```bash
# Find what's using a port
lsof -i :9092
lsof -i :8090
lsof -i :2181

# Kill the process or stop other Docker containers
docker ps
docker stop <container-id>
```

### Kafka Not Connecting

**Verify Kafka is healthy:**
```bash
docker-compose ps
# Look for "healthy" status
```

**Check Kafka logs:**
```bash
docker-compose logs kafka
```

**Test Kafka connectivity:**
```bash
docker exec -it court-booking-kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Clean Restart

If services are misbehaving:
```bash
# Stop everything
docker-compose down -v

# Remove any orphaned containers
docker ps -a | grep court-booking

# Start fresh
docker-compose up -d

# Wait for services to be healthy
watch docker-compose ps
```

## Resource Usage

Typical resource consumption:
- **Zookeeper:** ~100-200 MB RAM
- **Kafka:** ~300-500 MB RAM
- **Kafka UI:** ~50-100 MB RAM

**Total:** ~500-800 MB RAM

## Integration with Application

The Spring Boot application connects to Kafka at:
- **Bootstrap Server:** localhost:9092
- **Topic:** booking-created
- **Serialization:** JSON

Configuration in `application.yaml`:
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

## Testing Kafka

### Using Kafka Console Tools

**List topics:**
```bash
docker exec -it court-booking-kafka kafka-topics \
  --list \
  --bootstrap-server localhost:9092
```

**Describe a topic:**
```bash
docker exec -it court-booking-kafka kafka-topics \
  --describe \
  --topic booking-created \
  --bootstrap-server localhost:9092
```

**Consume messages:**
```bash
docker exec -it court-booking-kafka kafka-console-consumer \
  --topic booking-created \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

**Create a test message:**
```bash
docker exec -it court-booking-kafka kafka-console-producer \
  --topic booking-created \
  --bootstrap-server localhost:9092
# Type a message and press Enter
# Press Ctrl+C to exit
```

## Data Persistence

**Kafka data is NOT persisted** by default in this configuration. When you run `docker-compose down -v`, all topics and messages are deleted.

This is intentional for testing purposes - each test run starts with a clean slate.

To persist data, modify `docker-compose.yml` to add volumes:
```yaml
kafka:
  volumes:
    - kafka-data:/var/lib/kafka/data
```

## Production Considerations

This configuration is **for development/testing only**. For production:

1. Use multiple Kafka brokers (3+ recommended)
2. Increase replication factor
3. Configure proper retention policies
4. Use persistent volumes
5. Set up monitoring and alerting
6. Configure security (SSL, SASL)
7. Tune performance parameters
8. Use managed Kafka service (AWS MSK, Confluent Cloud, etc.)

## References

- [Confluent Platform](https://docs.confluent.io/platform/current/installation/docker/index.html)
- [Kafka UI Documentation](https://docs.kafka-ui.provectus.io/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
