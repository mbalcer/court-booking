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
│                   ADAPTERS (TODO)                       │
│  REST Controllers │ JPA Repositories │ Kafka Publishers │
├─────────────────────────────────────────────────────────┤
│              APPLICATION LAYER (TODO)                   │
│        Use Cases │ Application Services │ Ports         │
├─────────────────────────────────────────────────────────┤
│              DOMAIN LAYER (CORE) ✓ COMPLETE             │
│  Entities │ Value Objects │ Services │ Policies │ Excp. │
└─────────────────────────────────────────────────────────┘
```

### Current Implementation Status

**Completed** (Steps 1-5):
- Domain entities (`Booking`)
- Value objects (`TimeSlot`)
- Business policies (`OpeningHoursPolicy`, `OverlappingReservationsPolicy`)
- Domain exceptions (`BusinessException`, `InvalidTimeSlotException`)
- Domain services (`BookingDomainService`)

**To Be Implemented** (Steps 6+):
- Ports (interfaces for repositories, event publishers, use cases)
- Application services
- REST adapters (controllers, DTOs)
- Persistence adapters (JPA entities, Spring Data repositories)
- Event publishing adapters (Kafka)
- Configuration and wiring

### Package Structure

```
src/main/java/com/tennis/court_booking/
├── CourtBookingApplication.java       # Spring Boot entry point
└── domain/                            # Pure domain logic (no Spring dependencies)
    ├── entity/
    │   └── Booking.java               # Aggregate root with identity-based equality
    ├── valueobject/
    │   └── TimeSlot.java              # Immutable value object with validation
    ├── policy/
    │   ├── OpeningHoursPolicy.java    # Validates against operating hours
    │   └── OverlappingReservationsPolicy.java  # Prevents double bookings
    ├── service/
    │   └── BookingDomainService.java  # Orchestrates booking reservation logic
    └── exception/
        ├── BusinessException.java      # Business rule violations
        └── InvalidTimeSlotException.java  # Value object validation failures
```

### Domain Model Principles

#### 1. Entities vs Value Objects

**Entities** (identified by ID):
- `Booking`: Aggregate root with `Long id`
- Equality based on ID only (`@EqualsAndHashCode(of = "id")`)
- Contains value objects and enforces invariants

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
- Returns a new `Booking` entity if all validations pass
- Throws `BusinessException` if any business rule is violated
- Generates unique booking IDs using `AtomicLong`

**Usage Example**:
```java
BookingDomainService service = new BookingDomainService(
    openingHoursPolicy,
    overlappingReservationsPolicy
);

TimeSlot requestedSlot = new TimeSlot(date, start, end);
List<Booking> existingBookings = repository.findByDate(date);

Booking newBooking = service.reserve(requestedSlot, existingBookings);
```

### Testing Approach

**Unit Tests** (no Spring context required):
- Domain layer is fully testable without framework
- Pure JUnit 5 tests with comprehensive coverage
- Tests run in milliseconds (no database/network)

**Test Coverage**:
- `BookingTest`: 13 tests (entity creation, validation, equality)
- `TimeSlotTest`: 16 tests (validation, overlap detection)
- `OpeningHoursPolicyTest`: 9 tests (policy validation, boundaries)
- `OverlappingReservationsPolicyTest`: 17 tests (overlap scenarios)
- `BookingDomainServiceTest`: 17 tests (service orchestration, policy coordination, domain scenarios)

**Running Specific Tests**:
```bash
# Run domain layer tests only
./gradlew test --tests "com.tennis.court_booking.domain.*"

# Run policy tests
./gradlew test --tests "com.tennis.court_booking.domain.policy.*"

# Run service tests
./gradlew test --tests "com.tennis.court_booking.domain.service.*"

# Run a specific test class
./gradlew test --tests com.tennis.court_booking.domain.valueobject.TimeSlotTest
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

## Next Steps in Implementation

The domain layer (Steps 1-5) is now complete. The next phases are:

1. **Ports**: Define interfaces (`BookingRepository`, `BookingUseCase`, `BookingEventPublisher`)
2. **Application Service**: Implement `BookingApplicationService` using domain service and ports
3. **REST Adapter**: Controllers and DTOs for HTTP API
4. **Persistence Adapter**: JPA entities and repository implementations
5. **Event Adapter**: Kafka event publishing
6. **Configuration**: Wire dependencies with Spring `@Configuration`
7. **Integration Tests**: End-to-end testing with all layers

## References

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/3.5.7/reference/)
- [Gradle Build Tool](https://docs.gradle.org)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
