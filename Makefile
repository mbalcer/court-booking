.PHONY: help build test run run-test docker-up docker-down docker-logs clean test-api h2-console kafka-topics kafka-consume

# Default target
help:
	@echo "Court Booking Application - Make Commands"
	@echo ""
	@echo "Application Commands:"
	@echo "  make build         - Build the application"
	@echo "  make test          - Run all tests"
	@echo "  make run           - Run application (with Kafka)"
	@echo "  make run-test      - Run application (without Kafka)"
	@echo "  make test-api      - Run API test script"
	@echo "  make clean         - Clean build artifacts"
	@echo ""
	@echo "Docker Commands:"
	@echo "  make docker-up     - Start all dependencies (Kafka, Zookeeper)"
	@echo "  make docker-down   - Stop all dependencies"
	@echo "  make docker-logs   - View Docker logs"
	@echo "  make docker-reset  - Reset all Docker data"
	@echo ""
	@echo "Kafka Commands:"
	@echo "  make kafka-topics  - List all Kafka topics"
	@echo "  make kafka-consume - Consume booking-created events"
	@echo "  make kafka-ui      - Open Kafka UI in browser"
	@echo ""
	@echo "Database Commands:"
	@echo "  make h2-console    - Open H2 console in browser"
	@echo ""

# Build application
build:
	./gradlew clean build

# Run tests
test:
	./gradlew test

# Run application with Kafka
run:
	./gradlew bootRun

# Run application without Kafka (test profile)
run-test:
	./gradlew bootRun --args='--spring.profiles.active=test'

# Clean build artifacts
clean:
	./gradlew clean

# Run API test script
test-api:
	./test-api.sh

# Docker: Start all services
docker-up:
	docker-compose up -d
	@echo "Waiting for services to be healthy..."
	@sleep 10
	@docker-compose ps

# Docker: Stop all services
docker-down:
	docker-compose down

# Docker: View logs
docker-logs:
	docker-compose logs -f

# Docker: Reset all data
docker-reset:
	docker-compose down -v
	docker volume prune -f

# Kafka: List topics
kafka-topics:
	docker exec court-booking-kafka kafka-topics \
		--bootstrap-server localhost:9092 \
		--list

# Kafka: Consume booking-created events
kafka-consume:
	docker exec -it court-booking-kafka kafka-console-consumer \
		--bootstrap-server localhost:9092 \
		--topic booking-created \
		--from-beginning

# Kafka: Open Kafka UI
kafka-ui:
	@echo "Opening Kafka UI at http://localhost:8090"
	@open http://localhost:8090 2>/dev/null || xdg-open http://localhost:8090 2>/dev/null || echo "Please open http://localhost:8090 in your browser"

# Database: Open H2 console
h2-console:
	@echo "Opening H2 Console at http://localhost:8080/h2-console"
	@echo "JDBC URL: jdbc:h2:mem:courtbookingdb"
	@echo "Username: sa"
	@echo "Password: (leave empty)"
	@open http://localhost:8080/h2-console 2>/dev/null || xdg-open http://localhost:8080/h2-console 2>/dev/null || echo "Please open http://localhost:8080/h2-console in your browser"
