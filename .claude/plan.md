# Implementation Plan (Step-by-Step)

## Step 1 — Project Initialization
- Create Spring Boot project (Maven or Gradle)
- Add dependencies: Web, JPA, Validation, Lombok, H2, JUnit, Mockito
- Create package structure (domain, application, adapter, config)
- Create `application.yml` configuration

## Step 2 — Domain Value Objects
- Implement `TimeSlot(date, start, end)` with validation
- Implement `overlaps(TimeSlot other)`
- Write unit tests

## Step 3 — Domain Entity
- Implement `Booking(id, TimeSlot)`
- Write unit tests

## Step 4 — Business Policies
- Implement `OpeningHoursPolicy`
- Implement `OverlappingReservationsPolicy`
- Write unit tests for both policies

## Step 5 — Domain Service
- Implement `BookingDomainService.reserve(slot, existingBookings)`
- Throw `BusinessException` on rule violations
- Write domain scenario tests

## Step 6 — Ports
- OUT port: `BookingRepository(findByDate, save, findById, delete)`
- OUT port: `BookingEventPublisher.publish(BookingCreatedEvent event)`
- IN port: `BookingUseCase.reserve(ReserveCommand)`
- `ReserveCommand(date, start, end)`

## Step 7 — Application Service (Use Case)
- Implement `BookingApplicationService`
    - persist booking via `BookingRepository`
    - publish event via `BookingEventPublisher`
- Unit tests with mocked repository & event publisher

## Step 8 — Inbound Adapter (REST)
- DTOs: `ReserveRequest`, `ReserveResponse`
- Create `BookingController` with `POST /api/bookings`
- Validate request input
- Controller test using MockMvc

## Step 9 — Outbound Adapter (Persistence)
- JPA entity: `BookingJpaEntity`
- Mapper between domain model and JPA entity
- Spring Data repository
- Implement `BookingRepositoryAdapter`
- Test using `@DataJpaTest`

## Step 10 — Kafka Integration (Outbound Adapter)
- Add Kafka dependency
- Define `BookingCreatedEvent` (domain or application layer)
- Implement `KafkaBookingEventPublisher` adapter
- Convert event to JSON and send to Kafka topic `booking-created`
- Add configuration for Kafka producer
- Write integration test using Embedded Kafka (optional)

## Step 11 — Configuration / Wiring
- `@Configuration` providing beans for domain + application services
- Bind adapters to ports
- Minimal context test

## Step 12 — Manual testing
- Configure application.yaml to handle DB(Postgres) and Kafka
- Create docker-compose with all needed dependencies
- Call REST endpoint and ensure that all works correctly

## Step 13 — End-to-end Booking Scenario Test
- Configure application-test.yaml
- End-to-end integration test for booking flow
- Repository integration tests with @DataJpaTest
- Kafka event publishing integration tests with @EmbeddedKafka
- REST API integration tests with @SpringBootTest

## Step 14 — Additional Endpoints
- `GET /api/bookings`
- `DELETE /api/bookings/{id}`
- Integration tests for the created endpoints

## Step 15 — Refactoring
- Ensure domain layer has zero dependencies on Spring, REST, Kafka, or JPA
- Clean mapping separation

## Step 16 — Optional Extensions
- Implement Kafka consumer adapter that logs or processes events
- Add OpenAPI documentation
- Add Docker Compose with Kafka + Postgres

## Step 17 — Documentation & Deployment
- Create README
- Add Postman collection
- Produce Docker images
