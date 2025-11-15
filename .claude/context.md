# Application Context
We are building a tennis court booking system using **Hexagonal Architecture** in **Spring Boot**.  
End-user capabilities:
- create a court booking
- list bookings for a selected day
- cancel an existing booking

Business rules:
- booking cannot be created outside opening hours
- booking cannot overlap with an existing booking
- each booking must have a unique identifier
- whenever a booking is successfully created, publish a `BookingCreatedEvent` to Kafka so another system (e.g., email sender or analytics service) can use it.

Project goals:
- demonstrate real benefits of Hexagonal Architecture
- allow testing domain logic without Spring or a database
- support easy replacement of adapters (REST, DB, messaging)
- integrate **Kafka** as an outbound adapter (notification event publishing)

---