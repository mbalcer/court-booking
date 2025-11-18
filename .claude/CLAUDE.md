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
┌─────────────────────────────────────────────────────────┐
│                   ADAPTERS LAYER                        │
│  REST ✓ COMPLETE │ JPA Repositories │ Kafka Publishers │
│  In-Memory Repo ✓ │ Logging Events ✓                   │
├─────────────────────────────────────────────────────────┤
│          APPLICATION LAYER ✓ COMPLETE                   │
│        Use Cases │ Application Services │ Ports         │
├─────────────────────────────────────────────────────────┤
│              DOMAIN LAYER (CORE) ✓ COMPLETE             │
│  Entities │ Value Objects │ Services │ Policies │ Excp. │
├─────────────────────────────────────────────────────────┤
│              CONFIGURATION ✓ COMPLETE                   │
│        Spring Wiring │ Bean Definitions                 │
└─────────────────────────────────────────────────────────┘
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

- **Adapter Layer** (Step 8):
  - **REST Adapter (Inbound)**:
    - `BookingController` with POST `/api/bookings` endpoint
    - REST DTOs: `ReserveBookingRequest`, `ReserveBookingResponse`, `ErrorResponse`
    - `GlobalExceptionHandler` for centralized error handling
  - **Stub Adapters (Outbound)** - Temporary implementations for development:
    - `InMemoryBookingRepository`: Thread-safe in-memory repository implementation
    - `LoggingEventPublisher`: Logs events instead of publishing to message broker

- **Configuration Layer** (Step 11):
  - `BookingConfiguration`: Spring configuration class wiring all dependencies
  - Configures business policies with operating hours (8:00-20:00)
  - Wires domain service, application service, and stub adapters
  - Enables dependency injection for REST controllers

**To Be Implemented** (Steps 9-10, 12+):
- Persistence adapters (JPA entities, Spring Data repositories) - Step 9
- Event publishing adapters (Kafka) - Step 10
- Integration tests - Step 12
- Additional REST endpoints (GET, DELETE) - Step 13

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
        ├── persistence/               # Persistence adapters
        │   └── InMemoryBookingRepository.java  # In-memory stub (temporary)
        └── event/                     # Event publishing adapters
            └── LoggingEventPublisher.java      # Logging stub (temporary)
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

### Configuration Layer (Spring Wiring)

The configuration layer is responsible for wiring all dependencies together using Spring's dependency injection. This follows the **Inversion of Control (IoC)** principle and enables the hexagonal architecture to function as a cohesive application.

#### BookingConfiguration

**Purpose**: Central configuration class that defines Spring beans and wires dependencies.

**Key Beans**:
- `openingHoursPolicy()`: Configures court operating hours (8:00 AM - 8:00 PM)
- `overlappingReservationsPolicy()`: Configures overlap detection policy
- `bookingDomainService()`: Wires domain service with business policies
- `bookingRepository()`: Provides repository implementation (currently in-memory stub)
- `bookingEventPublisher()`: Provides event publisher implementation (currently logging stub)
- `bookingUseCase()`: Wires application service with all dependencies

**Configuration Principles**:
- **Constructor Injection**: All beans use constructor-based dependency injection
- **Interface-Based Wiring**: Beans are wired through interfaces (ports), not concrete types
- **Explicit Configuration**: Uses `@Bean` methods for clear, explicit bean definitions
- **Separation of Concerns**: Configuration is separated from business logic
- **Testability**: Constructor injection makes all components easily testable

**Stub Implementations** (Temporary):

Until Steps 9-10 are implemented, the configuration uses stub implementations for outbound adapters:

1. **InMemoryBookingRepository**:
   - Thread-safe in-memory storage using `ConcurrentHashMap`
   - Auto-incrementing ID generation using `AtomicLong`
   - Implements all repository operations (save, findById, findByDate, delete)
   - Provides utility methods for testing (clear, count)
   - Will be replaced with JPA repository adapter in Step 9

2. **LoggingEventPublisher**:
   - Logs events using SLF4J instead of publishing to message broker
   - Useful for development and debugging
   - Shows event flow without requiring external infrastructure
   - Will be replaced with Kafka publisher adapter in Step 10

**Dependency Flow**:
```
BookingController (REST)
    ↓ depends on
BookingUseCase (interface)
    ↑ implemented by
BookingApplicationService
    ↓ depends on
    ├── BookingRepository (interface) ← InMemoryBookingRepository
    ├── BookingEventPublisher (interface) ← LoggingEventPublisher
    └── BookingDomainService
        ↓ depends on
        ├── OpeningHoursPolicy
        └── OverlappingReservationsPolicy
```

**Benefits**:
- **Loose Coupling**: Components depend on interfaces, not implementations
- **Easy Testing**: Mock implementations can easily replace real ones
- **Flexibility**: Implementations can be swapped without changing business logic
- **Clear Architecture**: Configuration makes dependency graph explicit
- **Progressive Development**: Stub implementations allow incremental feature development

**Usage**:
The application can now be started with `./gradlew bootRun` and will have a fully functional REST API at `http://localhost:8080/api/bookings`, backed by in-memory storage and event logging.

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
- **Adapter Layer (REST)**:
  - `BookingControllerTest`: 8 tests with MockMvc (constructor validation, successful booking creation, error handling for all exception types, request/response mapping)
  - `GlobalExceptionHandlerTest`: 8 tests (exception handling, error response structure, message preservation, timestamp validation)

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

# Run a specific test class
./gradlew test --tests com.tennis.court_booking.domain.valueobject.TimeSlotTest
./gradlew test --tests com.tennis.court_booking.application.service.BookingApplicationServiceTest
./gradlew test --tests com.tennis.court_booking.adapter.in.web.controller.BookingControllerTest
```

## Dependencies

**Core**:
- Spring Boot 3.5.7
- Spring Web (REST APIs)
- Spring Data JPA (persistence)
- Spring Validation (bean validation)

**Database**:
- H2 (in-memory database for development)

**Utilities**:
- Lombok (reduces boilerplate: `@Value`, `@Getter`, `@EqualsAndHashCode`)

**Testing**:
- JUnit 5 (Jupiter)
- Mockito (mocking framework for unit tests)
- Spring Boot Test (integration tests, when implemented)

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

The domain layer (Steps 1-5), application layer (Steps 6-7), REST adapter (Step 8), and configuration (Step 11) are now complete. The application is fully functional with in-memory storage and event logging. The next phases are:

1. ~~**REST Adapter** (Step 8): Controllers and DTOs for HTTP API~~ ✓ COMPLETE
2. **Persistence Adapter** (Step 9): JPA entities and repository implementations
3. **Event Adapter** (Step 10): Kafka event publishing
4. ~~**Configuration** (Step 11): Wire dependencies with Spring `@Configuration`~~ ✓ COMPLETE
5. **Integration Tests** (Step 12): End-to-end testing with all layers
6. **Additional Endpoints** (Step 13): GET and DELETE operations
7. **Refactoring & Polish** (Step 14): Clean up, ensure best practices

**Current Application State**:
- ✅ Fully functional REST API at `/api/bookings`
- ✅ All business logic implemented and tested
- ✅ Dependency injection configured
- ✅ In-memory storage (thread-safe)
- ✅ Event logging for debugging
- ⏳ Persistence to database (Step 9)
- ⏳ Event publishing to Kafka (Step 10)

## References

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/3.5.7/reference/)
- [Gradle Build Tool](https://docs.gradle.org)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
