# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Tennis Court Booking Application** built using Spring Boot 3.5.7 and Java 21, following **Hexagonal Architecture** (Ports & Adapters) principles. The project emphasizes clean separation between domain logic and infrastructure concerns.

**Important**: The original package name 'com.tennis.court-booking' is invalid; this project uses 'com.tennis.court_booking' instead (with underscore).

## Build & Development Commands

### Build & Test
```bash
# Clean build directory
./gradlew clean

# Build the project (compiles, runs tests, creates artifacts)
./gradlew build

# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run specific test class
./gradlew test --tests com.tennis.court_booking.domain.entity.BookingTest

# Run specific test method
./gradlew test --tests com.tennis.court_booking.domain.entity.BookingTest.shouldCreateValidBooking

# Check code quality (runs tests and other verification tasks)
./gradlew check

# Assemble test classes only
./gradlew testClasses
```

### Running the Application
```bash
# Run the Spring Boot application
./gradlew bootRun

# Build and run the application JAR
./gradlew build
java -jar build/libs/court-booking-0.0.1-SNAPSHOT.jar
```

### Useful Development Commands
```bash
# Show all available tasks
./gradlew tasks

# Show project dependencies
./gradlew dependencies

# Show buildscript dependencies
./gradlew buildDependents
```

## Architecture & Design

### Hexagonal Architecture Implementation

This project follows **hexagonal architecture** with strict dependency rules: dependencies flow inward toward the domain core. The domain layer is completely framework-agnostic.

```
┌─────────────────────────────────────────────────────────────────┐
│                      ADAPTERS LAYER ✓ COMPLETE                  │
│  REST ✓ │ JPA Repositories ✓ │ Kafka Event Publishers ✓       │
├─────────────────────────────────────────────────────────────────┤
│              APPLICATION LAYER ✓ COMPLETE                       │
│            Use Cases │ Application Services │ Ports             │
├─────────────────────────────────────────────────────────────────┤
│                  DOMAIN LAYER (CORE) ✓ COMPLETE                 │
│      Entities │ Value Objects │ Services │ Policies │ Excp.     │
├─────────────────────────────────────────────────────────────────┤
│              CONFIGURATION ✓ COMPLETE                           │
│        Spring Wiring │ Bean Definitions                         │
└─────────────────────────────────────────────────────────────────┘
```

### Current Implementation Status

**Completed** (Steps 1-11):
- **Domain Layer** (Steps 1-5):
  - Domain entities (`Booking`)
  - Value objects (`TimeSlot`)
  - Business policies (`OpeningHoursPolicy`, `OverlappingReservationsPolicy`)
  - Domain exceptions (`BusinessException`, `InvalidTimeSlotException`)
  - Domain services (`BookingDomainService`)
  - Domain events (`BookingCreatedEvent`)

- **Application Layer** (Steps 6-7):
  - **Ports** (Step 6):
    - Inbound port: `BookingUseCase` with `ReserveCommand` and `BookingResponse`
    - Outbound ports: `BookingRepository`, `BookingEventPublisher`
  - **Application Service** (Step 7):
    - `BookingApplicationService` implementing use case orchestration
  - **Mappers**:
    - `TimeSlotMapper` for command-to-domain conversions
    - `BookingMapper` for domain-to-DTO conversions

- **Adapter Layer** (Steps 8-10):
  - **REST Adapter (Inbound)** (Step 8):
    - `BookingController` with POST `/api/bookings` endpoint
    - REST DTOs: `ReserveBookingRequest`, `ReserveBookingResponse`, `ErrorResponse`
    - `GlobalExceptionHandler` for centralized error handling
  - **Persistence Adapter (Outbound)** (Step 9):
    - `BookingJpaEntity` for database persistence
    - `BookingJpaRepository` (Spring Data JPA interface)
    - `BookingPersistenceMapper` for domain/JPA entity conversions
    - `BookingRepositoryAdapter` implementing `BookingRepository` port
  - **Event Adapter (Outbound)** (Step 10):
    - `BookingEventPublisherAdapter` implementing `BookingEventPublisher` port
    - `BookingCreatedKafkaEvent` (Kafka-specific DTO)
    - `BookingEventMapper` for domain event to Kafka DTO conversions
    - Spring Kafka integration for async event publishing

- **Configuration Layer** (Step 11):
  - `BookingConfiguration`: Spring configuration class wiring all dependencies
  - Configures business policies with operating hours (8:00-20:00)
  - Wires domain service, application service
  - Adapter implementations auto-detected via @Component scanning
  - Enables full dependency injection throughout the application

**To Be Implemented** (Steps 12+):
- Integration tests
- Additional REST endpoints (GET, DELETE)

### Package Structure

```
src/main/java/com/tennis/court_booking/
├── CourtBookingApplication.java       # Spring Boot entry point
├── config/                            # Spring configuration
│   └── BookingConfiguration.java      # Wires all dependencies with @Configuration
├── domain/                            # Pure domain logic (no Spring dependencies)
│   ├── entity/
│   │   └── Booking.java               # Aggregate root with identity-based equality
│   ├── valueobject/
│   │   └── TimeSlot.java              # Immutable value object with validation
│   ├── policy/
│   │   ├── OpeningHoursPolicy.java    # Validates against operating hours
│   │   └── OverlappingReservationsPolicy.java  # Prevents double bookings
│   ├── service/
│   │   └── BookingDomainService.java  # Orchestrates booking reservation logic
│   ├── event/
│   │   └── BookingCreatedEvent.java   # Domain event for booking creation
│   └── exception/
│       ├── BusinessException.java      # Business rule violations
│       └── InvalidTimeSlotException.java  # Value object validation failures
├── application/                       # Application layer (orchestrates use cases)
│   ├── port/
│   │   ├── in/                        # Inbound ports (use case interfaces)
│   │   │   ├── BookingUseCase.java    # Reserve booking use case interface
│   │   │   ├── ReserveCommand.java    # Command DTO for reservation
│   │   │   └── BookingResponse.java   # Response DTO for booking
│   │   └── out/                       # Outbound ports (infrastructure interfaces)
│   │       ├── BookingRepository.java      # Repository interface
│   │       └── BookingEventPublisher.java  # Event publisher interface
│   ├── mapper/                        # Mappers for DTO/domain conversions
│   │   ├── TimeSlotMapper.java        # Maps ReserveCommand to TimeSlot
│   │   └── BookingMapper.java         # Maps Booking to DTOs/events
│   └── service/
│       └── BookingApplicationService.java  # Use case implementation
└── adapter/                           # Adapter layer (infrastructure implementations)
    ├── in/                            # Inbound adapters (driving)
    │   └── web/                       # REST API adapter
    │       ├── controller/
    │       │   └── BookingController.java  # REST controller for bookings
    │       ├── dto/
    │       │   ├── ReserveBookingRequest.java   # REST request DTO
    │       │   ├── ReserveBookingResponse.java  # REST response DTO
    │       │   └── ErrorResponse.java           # Error response DTO
    │       └── exception/
    │           └── GlobalExceptionHandler.java  # Global REST exception handler
    └── out/                           # Outbound adapters (driven)
        ├── persistence/               # JPA persistence adapter
        │   ├── BookingRepositoryAdapter.java    # Implements BookingRepository port
        │   ├── entity/
        │   │   └── BookingJpaEntity.java        # JPA entity for database
        │   ├── repository/
        │   │   └── BookingJpaRepository.java    # Spring Data JPA repository
        │   └── mapper/
        │       └── BookingPersistenceMapper.java  # Maps between domain and JPA entities
        └── event/                     # Kafka event publishing adapter
            ├── BookingEventPublisherAdapter.java  # Implements BookingEventPublisher port
            ├── dto/
            │   └── BookingCreatedKafkaEvent.java  # Kafka-specific event DTO
            └── mapper/
                └── BookingEventMapper.java        # Maps domain events to Kafka DTOs
```

### Domain Model Principles

#### 1. Entities vs Value Objects

**Entities** (identified by ID):
- `Booking`: Aggregate root with `Long id` (nullable for new, unpersisted bookings)
- Equality based on ID only (`@EqualsAndHashCode(of = "id")`)
- Contains value objects and enforces invariants
- ID is null for new domain objects; assigned by persistence layer upon save

**Value Objects** (identified by attributes):
- `TimeSlot`: Immutable (`@Value`), no ID
- Equality based on all fields (date, start, end)
- Contains domain logic (e.g., `overlaps()` method)

#### 2. Validation Strategy

**Constructor Validation (Fail-Fast)**:
- All domain objects validate their state at construction time
- Invalid objects cannot be created
- Throws specific exceptions immediately

```java
// TimeSlot validates in constructor
new TimeSlot(date, start, end); // Throws InvalidTimeSlotException if invalid

// Booking validates in constructor
new Booking(id, timeSlot); // Throws IllegalArgumentException if null
```

**Policy-Based Business Rules**:
- Business policies are separate, reusable objects
- Policies validate complex business rules
- Throws `BusinessException` with detailed context

```java
// Policy validation happens at application layer
openingHoursPolicy.validate(timeSlot);
overlappingPolicy.validate(timeSlot, existingBookings);
```

#### 3. Immutability

- **Value Objects**: Fully immutable using Lombok `@Value`
- **Entities**: Read-only (only getters, no setters)
- **Policies**: Immutable configuration

This ensures thread-safety and prevents accidental state mutations.

#### 4. Exception Handling

**Domain Exceptions**:
- `InvalidTimeSlotException`: Constructor validation failures
- `BusinessException`: Policy/business rule violations
- Both extend `RuntimeException` (unchecked)

**Error Messages Include Context**:
```
"Booking cannot start before opening time. Start: 07:00, Opening time: 08:00"
"The requested time slot overlaps with an existing booking. Requested: [2024-01-15 10:30-11:30], Existing booking ID: 2"
```

#### 5. Separation of Domain and Infrastructure Concerns

**ID Generation**:
- Domain entities can have null IDs for new, unpersisted objects
- ID generation is responsibility of the persistence layer (database auto-increment, sequences, etc.)
- Domain service creates `Booking` with null ID
- Persistence adapter assigns ID when saving to database
- This keeps domain layer pure and independent of infrastructure

**Validation Delegation**:
- Domain service delegates validation to business policies
- Policies validate their own parameters (fail-fast)
- Service doesn't duplicate null checks already done by policies
- This reduces redundancy and keeps service focused on orchestration

### Key Domain Logic

#### TimeSlot Overlap Detection

The `TimeSlot.overlaps(TimeSlot other)` method determines if two time slots conflict:
- Must be same date
- Time ranges intersect if: `start1 < end2 AND start2 < end1`
- Slots that touch exactly (e.g., 10:00-11:00 and 11:00-12:00) do NOT overlap
- See `TimeSlot.java:61-75` for implementation

#### Business Policies

**OpeningHoursPolicy**:
- Validates bookings fall within operating hours
- Configurable opening/closing times
- Prevents bookings before opening or after closing

**OverlappingReservationsPolicy**:
- Validates new bookings against existing reservations
- Uses `TimeSlot.overlaps()` to detect conflicts
- Takes list of existing bookings as parameter

#### Domain Service

**BookingDomainService**:
- Orchestrates the booking reservation process
- Coordinates validation using business policies
- Validates against opening hours policy first
- Then validates against overlapping reservations policy
- Returns a new `Booking` entity with null ID if all validations pass
- Throws `BusinessException` if any business rule is violated
- **ID generation is delegated to the persistence layer** (not domain responsibility)

**Usage Example**:
```java
BookingDomainService service = new BookingDomainService(
    openingHoursPolicy,
    overlappingReservationsPolicy
);

TimeSlot requestedSlot = new TimeSlot(date, start, end);
List<Booking> existingBookings = repository.findByDate(date);

Booking newBooking = service.reserve(requestedSlot, existingBookings);
// newBooking.getId() will be null - ID assigned by persistence layer
```

### Application Layer

#### Ports (Hexagonal Architecture Interfaces)

**Inbound Port (Primary/Driving)**:
- `BookingUseCase`: Defines the reserve booking use case
- `ReserveCommand`: Input DTO carrying date, start time, and end time
- `BookingResponse`: Output DTO with booking ID and time slot details

**Outbound Ports (Secondary/Driven)**:
- `BookingRepository`: Interface for persistence operations (findByDate, save, findById, delete)
- `BookingEventPublisher`: Interface for publishing domain events to external systems

#### Application Service

**BookingApplicationService**:
- Implements `BookingUseCase` interface
- Orchestrates the complete booking reservation flow
- Coordinates between domain service and outbound ports
- Responsibilities:
  1. Converts command DTOs to domain objects (`ReserveCommand` → `TimeSlot`)
  2. Retrieves existing bookings via `BookingRepository`
  3. Delegates business logic to `BookingDomainService`
  4. Persists the new booking via `BookingRepository` (ID assigned here)
  5. Creates and publishes `BookingCreatedEvent` via `BookingEventPublisher`
  6. Converts domain entity to response DTO (`Booking` → `BookingResponse`)

**Key Design Principles**:
- **Separation of Concerns**: Domain logic stays in domain service; orchestration in application service
- **Dependency Inversion**: Depends on port interfaces, not concrete implementations
- **DTO Translation**: Prevents domain entities from leaking to adapters
- **Transaction Boundary**: Application service defines the transactional scope (to be implemented in Step 11)

**Usage Example**:
```java
BookingApplicationService service = new BookingApplicationService(
    bookingRepository,
    eventPublisher,
    domainService
);

ReserveCommand command = new ReserveCommand(
    LocalDate.of(2024, 1, 15),
    LocalTime.of(10, 0),
    LocalTime.of(11, 0)
);

BookingResponse response = service.reserve(command);
// Response contains: id=1, date=2024-01-15, startTime=10:00, endTime=11:00
```

#### Mappers

**TimeSlotMapper**:
- Converts `ReserveCommand` DTO to `TimeSlot` value object
- Static utility class with private constructor
- Validates command is not null before conversion
- Delegates validation to `TimeSlot` constructor

**BookingMapper**:
- Converts `Booking` entity to `BookingResponse` DTO (for API responses)
- Converts `Booking` entity to `BookingCreatedEvent` (for event publishing)
- Static utility class with private constructor
- Validates booking is not null before conversion
- Ensures consistent data across response and event

**Key Design Principles**:
- **Stateless Utility Classes**: Mappers are stateless with only static methods
- **Single Responsibility**: Each mapper handles specific conversions
- **Separation of Concerns**: Keeps conversion logic out of application services
- **Consistency**: Same booking produces consistent response and event data
- **Null Safety**: All mappers validate inputs before conversion

**Usage Examples**:
```java
// Convert command to domain object
TimeSlot timeSlot = TimeSlotMapper.toTimeSlot(command);

// Convert domain object to response DTO
BookingResponse response = BookingMapper.toBookingResponse(booking);

// Convert domain object to event
BookingCreatedEvent event = BookingMapper.toBookingCreatedEvent(booking);
```

### REST Adapter (Inbound Adapter)

The REST adapter is the **inbound (driving) adapter** that exposes HTTP endpoints for the booking application. It translates HTTP requests into application use case calls and converts responses back to HTTP format.

#### REST Controller

**BookingController**:
- Exposes REST endpoints for booking operations
- Depends on `BookingUseCase` interface (not concrete implementation)
- Handles HTTP request/response mapping
- Returns appropriate HTTP status codes (201 Created for successful bookings)
- Thin adapter layer with no business logic

**Endpoint**:
- `POST /api/bookings`: Create a new court booking reservation

#### REST DTOs

**ReserveBookingRequest**:
- Receives JSON request data from HTTP clients
- Contains: `date`, `start`, `end`
- Uses `@NoArgsConstructor` and `@AllArgsConstructor` for JSON deserialization

**ReserveBookingResponse**:
- Returns JSON response data to HTTP clients
- Contains: `id`, `date`, `startTime`, `endTime`
- Mapped from `BookingResponse` returned by application layer

**ErrorResponse**:
- Standardized error response structure
- Contains: `timestamp`, `status`, `error`, `message`, `path`
- Used by exception handler for all error responses

#### Global Exception Handler

**GlobalExceptionHandler**:
- Centralized exception handling using `@RestControllerAdvice`
- Translates domain exceptions into appropriate HTTP responses
- Exception mappings:
  - `BusinessException` → HTTP 400 (Bad Request)
  - `InvalidTimeSlotException` → HTTP 400 (Bad Request)
  - `IllegalArgumentException` → HTTP 400 (Bad Request)
  - Generic `Exception` → HTTP 500 (Internal Server Error)
- Preserves original error messages from domain layer
- Includes timestamp and request path in all error responses

**Key Design Principles**:
- **Separation of Concerns**: Controller only handles HTTP concerns; no business logic
- **Dependency Inversion**: Depends on use case interface, not concrete service
- **DTO Translation**: Maps between REST DTOs and application layer DTOs
- **Centralized Error Handling**: Single location for all HTTP error responses
- **Consistent Error Format**: All errors follow the same response structure

**Usage Example**:
```bash
# Create a new booking
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-15",
    "start": "10:00",
    "end": "11:00"
  }'

# Success response (HTTP 201)
{
  "id": 1,
  "date": "2024-01-15",
  "startTime": "10:00:00",
  "endTime": "11:00:00"
}

# Error response (HTTP 400)
{
  "timestamp": "2024-01-15T09:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Booking cannot start before opening time. Start: 07:00, Opening time: 08:00",
  "path": "/api/bookings"
}
```

### Persistence Adapter (Outbound Adapter)

The persistence adapter is the **outbound (driven) adapter** that implements the `BookingRepository` port defined in the application layer. It translates between domain entities and JPA entities, providing database persistence using Spring Data JPA.

#### JPA Entity

**BookingJpaEntity**:
- JPA entity for persisting bookings to the database
- Maps to the `bookings` table
- Contains embedded time slot data (date, startTime, endTime) rather than a separate table
- Uses auto-generated ID with `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- Separate from domain entity to maintain hexagonal architecture boundaries
- Identity-based equality (two entities are equal if they have the same ID)
- Provides constructor for creating new entities without ID (for inserts)

**Fields**:
- `id`: Primary key (auto-generated)
- `date`: Booking date (`@Column(name = "booking_date", nullable = false)`)
- `startTime`: Start time (`@Column(name = "start_time", nullable = false)`)
- `endTime`: End time (`@Column(name = "end_time", nullable = false)`)

#### Spring Data JPA Repository

**BookingJpaRepository**:
- Extends `JpaRepository<BookingJpaEntity, Long>`
- Provides standard CRUD operations automatically
- Custom query method: `findByDate(LocalDate date)` - Spring Data JPA implements this automatically based on naming convention
- Marked with `@Repository` annotation
- This is a Spring-specific infrastructure interface

#### Persistence Mapper

**BookingPersistenceMapper**:
- Static utility class for converting between domain and JPA entities
- **Domain to JPA**: `toJpaEntity(Booking)` - converts domain `Booking` to `BookingJpaEntity`
  - Handles both new bookings (null ID) and existing bookings (with ID)
  - Extracts time slot data into separate JPA entity fields
- **JPA to Domain**: `toDomainEntity(BookingJpaEntity)` - converts `BookingJpaEntity` to domain `Booking`
  - Reconstructs `TimeSlot` value object from JPA entity fields
  - Delegates validation to `TimeSlot` constructor
- Private constructor throws `UnsupportedOperationException` (utility class pattern)
- Validates all inputs (null checks)
- Propagates domain exceptions (e.g., `InvalidTimeSlotException` from `TimeSlot` constructor)

#### Repository Adapter

**BookingRepositoryAdapter**:
- Implements the `BookingRepository` port from the application layer
- Marked with `@Component` for Spring dependency injection
- Delegates all persistence operations to `BookingJpaRepository`
- Uses `BookingPersistenceMapper` to convert between domain and JPA entities
- Maintains separation between domain and infrastructure concerns

**Implemented Operations**:
- `findByDate(LocalDate)`: Retrieves all bookings for a specific date, converts to domain entities
- `save(Booking)`: Persists a booking, returns saved booking with assigned ID
- `findById(Long)`: Retrieves a single booking by ID, returns `Optional<Booking>`
- `delete(Long)`: Deletes a booking by ID

**Validation**:
- All methods validate parameters (null checks)
- Throws `IllegalArgumentException` for invalid inputs
- Delegates domain validation to mapper and domain constructors

**Key Design Principles**:
- **Hexagonal Architecture**: Implements port interface, keeping domain layer independent
- **Separation of Concerns**: Pure adapter with no business logic
- **Dependency Inversion**: Application layer depends on port interface, not on this adapter
- **DTO Translation**: Converts between domain and persistence representations
- **Single Responsibility**: Only handles persistence concerns
- **Framework Isolation**: Domain entities remain pure Java; JPA annotations only in adapter layer

**Usage Example**:
```java
// Injected by Spring
@Component
public class BookingApplicationService implements BookingUseCase {
    private final BookingRepository bookingRepository; // Interface, not concrete class

    public BookingApplicationService(BookingRepository bookingRepository, ...) {
        this.bookingRepository = bookingRepository; // Spring injects BookingRepositoryAdapter
    }

    public BookingResponse reserve(ReserveCommand command) {
        // Application service uses the port interface
        List<Booking> existingBookings = bookingRepository.findByDate(command.date());
        // ... domain logic ...
        Booking savedBooking = bookingRepository.save(newBooking);
        // ...
    }
}
```

### Event Adapter (Outbound Adapter)

The event adapter is the **outbound (driven) adapter** that implements the `BookingEventPublisher` port defined in the application layer. It translates domain events into Kafka-specific messages and publishes them asynchronously using Spring Kafka.

#### Kafka Event DTO

**BookingCreatedKafkaEvent**:
- Kafka-specific DTO for serialization to JSON
- Contains: `bookingId`, `date`, `startTime`, `endTime`
- Uses Jackson annotations for JSON serialization (`@JsonProperty`, `@JsonFormat`)
- Separate from domain event to maintain infrastructure independence
- Allows message format evolution without affecting domain layer
- Uses snake_case field names for external API consistency

**Key Design Principles**:
- **Separation of Concerns**: Domain events remain pure; Kafka concerns isolated in adapter
- **Versioning**: DTO can evolve independently for backward/forward compatibility
- **No Framework Pollution**: Domain layer has no Kafka or Jackson dependencies

#### Event Mapper

**BookingEventMapper**:
- Static utility class for converting domain events to Kafka DTOs
- **Domain to Kafka**: `toKafkaEvent(BookingCreatedEvent)` - converts domain event to Kafka DTO
- Private constructor throws `UnsupportedOperationException` (utility class pattern)
- Validates all inputs (null checks)
- Simple structural conversion with no business logic

#### Event Publisher Adapter

**BookingEventPublisherAdapter**:
- Implements the `BookingEventPublisher` port from the application layer
- Marked with `@Component` for Spring dependency injection
- Uses Spring `KafkaTemplate` to publish messages asynchronously
- Uses booking ID as message key for consistent partitioning
- Configurable topic name via `@Value` with default fallback
- Logs success and failure outcomes (non-blocking)

**Publishing Flow**:
1. Validates input event (null check)
2. Converts domain event to Kafka DTO using `BookingEventMapper`
3. Publishes to Kafka topic asynchronously with booking ID as key
4. Handles success/failure callbacks with appropriate logging
5. Does not throw exceptions on publish failure (loose coupling)

**Configuration**:
- Topic name: `kafka.topic.booking-created` (default: `booking-created`)
- Async publishing with `CompletableFuture` callbacks
- Message key based on booking ID for partitioning

**Key Design Principles**:
- **Hexagonal Architecture**: Implements port interface, keeping domain layer independent
- **Separation of Concerns**: Pure adapter with no business logic
- **Dependency Inversion**: Application layer depends on port interface, not on this adapter
- **Async Publishing**: Non-blocking event publishing with callbacks
- **Observability**: Comprehensive logging for monitoring and troubleshooting
- **Loose Coupling**: Failures don't propagate to prevent cascade failures

**Usage Example**:
```java
// Injected by Spring
@Component
public class BookingApplicationService implements BookingUseCase {
    private final BookingEventPublisher eventPublisher; // Interface, not concrete class

    public BookingApplicationService(..., BookingEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher; // Spring injects BookingEventPublisherAdapter
    }

    public BookingResponse reserve(ReserveCommand command) {
        // ... domain logic and persistence ...

        // Publish event asynchronously
        BookingCreatedEvent event = BookingMapper.toBookingCreatedEvent(savedBooking);
        eventPublisher.publish(event);

        return BookingMapper.toBookingResponse(savedBooking);
    }
}
```

### Testing Approach

**Unit Tests** (no Spring context required):
- Domain layer is fully testable without framework
- Pure JUnit 5 tests with comprehensive coverage
- Tests run in milliseconds (no database/network)

**Test Coverage**:
- **Domain Layer**:
  - `BookingTest`: 13 tests (entity creation, validation with null ID support, equality)
  - `TimeSlotTest`: 16 tests (validation, overlap detection)
  - `OpeningHoursPolicyTest`: 9 tests (policy validation, boundaries)
  - `OverlappingReservationsPolicyTest`: 17 tests (overlap scenarios)
  - `BookingDomainServiceTest`: 16 tests (service orchestration, policy delegation, domain scenarios)
- **Application Layer**:
  - `BookingApplicationServiceTest`: 16 tests with Mockito (constructor validation, successful flow, exception handling, mocked repository & event publisher)
  - `TimeSlotMapperTest`: 10 tests (successful mapping, null handling, invalid data propagation, consistency)
  - `BookingMapperTest`: 13 tests (response mapping, event mapping, null handling, consistency)
- **Adapter Layer (Inbound - REST)**:
  - `BookingControllerTest`: 8 tests with MockMvc (constructor validation, successful booking creation, error handling for all exception types, request/response mapping)
  - `GlobalExceptionHandlerTest`: 8 tests (exception handling, error response structure, message preservation, timestamp validation)
- **Adapter Layer (Outbound - Persistence)**:
  - `BookingJpaEntityTest`: 12 tests (entity creation with/without ID, equality based on ID, hashCode, toString)
  - `BookingPersistenceMapperTest`: 14 tests (bidirectional mapping, null handling, invalid data propagation, round-trip consistency)
  - `BookingRepositoryAdapterTest`: 16 tests with Mockito (constructor validation, all CRUD operations, null parameter validation, proper delegation to JPA repository)
- **Adapter Layer (Outbound - Event)**:
  - `BookingEventMapperTest`: 10 tests (successful conversion, null handling, data preservation, utility class instantiation)
  - `BookingEventPublisherAdapterTest`: 11 tests with Mockito (constructor validation, successful publishing, correct topic/key/value, multiple publishes, failure handling)

**Running Specific Tests**:
```bash
# Run domain layer tests only
./gradlew test --tests "com.tennis.court_booking.domain.*"

# Run application layer tests only
./gradlew test --tests "com.tennis.court_booking.application.*"

# Run adapter layer tests only
./gradlew test --tests "com.tennis.court_booking.adapter.*"

# Run policy tests
./gradlew test --tests "com.tennis.court_booking.domain.policy.*"

# Run service tests (both domain and application)
./gradlew test --tests "com.tennis.court_booking.domain.service.*"
./gradlew test --tests "com.tennis.court_booking.application.service.*"

# Run mapper tests
./gradlew test --tests "com.tennis.court_booking.application.mapper.*"

# Run REST controller tests
./gradlew test --tests "com.tennis.court_booking.adapter.in.web.controller.*"

# Run persistence adapter tests
./gradlew test --tests "com.tennis.court_booking.adapter.out.persistence.*"

# Run event adapter tests
./gradlew test --tests "com.tennis.court_booking.adapter.out.event.*"

# Run a specific test class
./gradlew test --tests com.tennis.court_booking.domain.valueobject.TimeSlotTest
./gradlew test --tests com.tennis.court_booking.application.service.BookingApplicationServiceTest
./gradlew test --tests com.tennis.court_booking.adapter.in.web.controller.BookingControllerTest
./gradlew test --tests com.tennis.court_booking.adapter.out.persistence.BookingRepositoryAdapterTest
./gradlew test --tests com.tennis.court_booking.adapter.out.event.BookingEventPublisherAdapterTest
./gradlew test --tests com.tennis.court_booking.adapter.out.event.mapper.BookingEventMapperTest
```

## Dependencies

**Core**:
- Spring Boot 3.5.7
- Spring Web (REST APIs)
- Spring Data JPA (persistence)
- Spring Kafka (event publishing)
- Spring Validation (bean validation)

**Database**:
- H2 (in-memory database for development)

**Messaging**:
- Spring Kafka (asynchronous event publishing to Kafka topics)

**Utilities**:
- Lombok (reduces boilerplate: `@Value`, `@Getter`, `@EqualsAndHashCode`)

**Testing**:
- JUnit 5 (Jupiter)
- Mockito (mocking framework for unit tests)
- Spring Boot Test (integration tests, when implemented)
- Spring Kafka Test (Kafka testing utilities)

## Code Conventions

### Lombok Usage

- `@Value`: Immutable value objects (generates constructor, getters, equals, hashCode, toString)
- `@Getter`: Read-only entities (generates getters only)
- `@EqualsAndHashCode(of = "id")`: Identity-based equality for entities

### Domain Layer Rules

1. **No Framework Dependencies**: Domain code must remain pure Java
2. **Constructor Validation**: All invariants enforced at construction
3. **Immutability Preferred**: Use `@Value` for value objects, final fields for entities
4. **Explicit Null Checks**: Validate and fail fast with clear messages
5. **Policy Pattern**: Extract business rules into separate policy objects
6. **Descriptive Exceptions**: Include context in error messages

### When Adding New Features

**To add a new domain entity**:
1. Create in `domain/entity/` package
2. Use `@Getter` and `@EqualsAndHashCode(of = "id")` for entities
3. Validate in constructor
4. Write comprehensive unit tests

**To add a new value object**:
1. Create in `domain/valueobject/` package
2. Use `@Value` for immutability
3. Validate all constraints in constructor
4. Implement domain logic methods as needed
5. Write tests covering all validation and logic

**To add a new business policy**:
1. Create in `domain/policy/` package
2. Use `@Value` if immutable configuration
3. Implement `validate()` method throwing `BusinessException`
4. Write tests for all business rule scenarios

**To add a new domain service**:
1. Create in `domain/service/` package
2. Accept policies and dependencies via constructor
3. Validate constructor parameters (no nulls)
4. Orchestrate business logic and policies
5. Return domain entities or throw exceptions
6. Write comprehensive scenario tests

**To add a new application service (use case)**:
1. Define inbound port interface in `application/port/in/`
2. Define command and response DTOs in same package
3. Define required outbound ports in `application/port/out/`
4. Create service implementation in `application/service/`
5. Implement use case interface
6. Validate constructor dependencies (no nulls)
7. Coordinate between domain service and outbound ports
8. Use mappers for DTO-to-domain and domain-to-DTO conversions
9. Write comprehensive unit tests using Mockito for port mocks

**To add a new mapper**:
1. Create in `application/mapper/` package
2. Make it a utility class with private constructor throwing `UnsupportedOperationException`
3. Use static methods for all conversions
4. Validate all inputs (check for nulls)
5. Keep conversions simple and focused (no business logic)
6. Write comprehensive unit tests covering:
   - Successful mapping scenarios
   - Null parameter handling
   - Exception propagation from constructors
   - Consistency across multiple conversions
7. Name methods clearly: `toTimeSlot()`, `toBookingResponse()`, `toBookingCreatedEvent()`

**To add a new REST endpoint**:
1. Create REST DTOs in `adapter/in/web/dto/` package
2. Use `@NoArgsConstructor` and `@AllArgsConstructor` for request DTOs (JSON deserialization)
3. Add endpoint method to `BookingController` (or create new controller)
4. Map REST DTOs to application layer command/response DTOs
5. Call the appropriate use case interface method
6. Return `ResponseEntity` with appropriate HTTP status code
7. No business logic in controller (only HTTP concerns and DTO mapping)
8. Write comprehensive tests using `@WebMvcTest` and MockMvc:
   - Successful request/response scenarios
   - Exception handling (verify HTTP status and error response)
   - Request/response mapping validation
   - Edge cases and boundary conditions

### Application Layer Rules

1. **No Domain Logic**: Application services only orchestrate; domain services contain business logic
2. **Depend on Interfaces**: Only depend on port interfaces, never on concrete adapter implementations
3. **DTO Boundaries**: Use command/response DTOs to prevent domain entities from leaking to adapters
4. **Use Mappers**: Delegate DTO/domain conversions to mapper classes; keep services focused on orchestration
5. **Constructor Injection**: Accept all dependencies via constructor for testability
6. **Exception Propagation**: Let domain exceptions bubble up; don't catch and swallow them
7. **Transaction Demarcation**: Application service methods define transactional boundaries (via Spring `@Transactional` when configured)

### Adapter Layer Rules

1. **No Business Logic**: Adapters only translate between external formats and application layer
2. **Depend on Use Case Interfaces**: Controllers depend on use case interfaces, not concrete services
3. **Thin Adapter Pattern**: Keep adapters as thin as possible; all orchestration in application layer
4. **DTO Mapping**: Map between adapter-specific DTOs and application layer DTOs
5. **HTTP Concerns Only**: Controllers handle HTTP-specific concerns (status codes, headers, etc.)
6. **Exception Translation**: Use exception handlers to translate domain exceptions to HTTP responses
7. **No Direct Domain Access**: Never bypass application layer to call domain services directly
8. **Validation**: Basic format validation can occur in adapters, but business rules stay in domain

## Next Steps in Implementation

The domain layer (Steps 1-5), application layer (Steps 6-7), all adapters (Steps 8-10), and configuration (Step 11) are now complete. The application is fully functional with database persistence and event publishing. The next phases are:

1. ~~**REST Adapter** (Step 8): Controllers and DTOs for HTTP API~~ ✓ COMPLETE
2. ~~**Persistence Adapter** (Step 9): JPA entities and repository implementations~~ ✓ COMPLETE
3. ~~**Event Adapter** (Step 10): Kafka event publishing~~ ✓ COMPLETE
4. ~~**Configuration** (Step 11): Wire dependencies with Spring `@Configuration`~~ ✓ COMPLETE
5. **Integration Tests** (Step 12): End-to-end testing with all layers
6. **Additional Endpoints** (Step 13): GET and DELETE operations
7. **Refactoring & Polish** (Step 14): Clean up, ensure best practices

**Current Application State**:
- ✅ Fully functional REST API at `/api/bookings`
- ✅ All business logic implemented and tested
- ✅ Dependency injection configured
- ✅ JPA persistence with H2 database
- ✅ Kafka event publishing for domain events
- ⏳ Integration tests (Step 12)
- ⏳ Additional CRUD endpoints (Step 13)

## References

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/3.5.7/reference/)
- [Gradle Build Tool](https://docs.gradle.org)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
