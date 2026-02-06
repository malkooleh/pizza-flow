# Phase 5: Comprehensive Testing Walkthrough

## Overview
Phase 5 focused on establishing a robust testing strategy for the PizzaFlow microservices architecture. We implemented a testing pyramid comprising Unit Tests (for business logic), Integration Tests (for infrastructure interactions), and End-to-End Tests (for full user journeys).

## ✅ 5.1 Unit Testing (JUnit 5 + Mockito)
We prioritized isolated testing of business logic to ensure correctness without external dependencies.

### Key Implementations
-   **Order Service**: Verified `createOrder` logic, event publishing, and State Machine transitions (`PENDING` -> `PAID`).
-   **Kitchen Service**: Tested queue management, Redis synchronization, and WebSocket broadcasting.
-   **Inventory Service**: Validated stock reservation logic, optimistic locking, and specific exception handling (`InsufficientStockException`).
-   **Delivery Service**: Covered courier assignment logic and geospatial zone validation.
-   **Booking, Catalog, Payment**: Added coverage for booking constraints, caching abstraction, and mock payment gateway logic.

### Technology Stack
-   **JUnit 5**: Standard testing framework.
-   **Mockito**: For mocking external dependencies (`Repositories`, `KafkaTemplate`, `StateMachine`).
-   **AssertJ**: For fluent and readable assertions.

## ✅ 5.2 Integration Testing (Testcontainers)
We utilized **Testcontainers** to spin up ephemeral Docker containers for valid infrastructure testing.

### Infrastructure Setup
-   **AbstractIntegrationTest**: A shared base class in `order-service` (and reusable pattern) that manages the lifecycle of:
    -   `postgres:16-alpine`
    -   `mongo:6.0`
    -   `confluentinc/cp-kafka:7.4.0`
-   **Configuration**: Automatically injects container properties (`spring.datasource.url`, `spring.kafka.bootstrap-servers`) into the Spring Context.

### Verification Scenarios
-   **OrderServiceIntegrationTest**:
    -   Starts the full Spring Boot context.
    -   Creates an Order via the Service layer.
    -   Verifies persistence in the real (containerized) PostgreSQL database.
-   **KitchenServiceIntegrationTest**:
    -   Verifies Order creation from `PaymentEvent`.
    -   Checks **Redis** Queue persistence (KDS visibility).
    -   Validates Redis state updates upon status changes.

## ✅ 5.3 End-to-End (E2E) Testing
We created a dedicated **`e2e-tests`** module to validate critical user flows from the outside in.

### The "Happy Path" Scenario
A generic test suite (`HappyPathE2ETest.java`) validates the core business revenue driver:
1.  **Catalog**: Browse products and select a Pizza.
2.  **Order**: Place an order for the selected product.
3.  **Payment**: Successfully pay for the order.
4.  **Verification**: Poll for the Order status to update to `PAID`.

### The "Payment Failure" Scenario
We validated the system's resilience to declined payments (`InvalidPaymentE2ETest.java`):
1.  **Order**: Create Order (201).
2.  **Payment**: Attempt payment with a trigger amount (`0.99`).
3.  **Verification**: Poll for Order status to update to `PAYMENT_FAILED` or `CANCELLED`.

### Tooling
-   **RestAssured**: For fluent HTTP API testing.
-   **Awaitility**: For asynchronous state verification (polling eventual consistency).
-   **Maven Module**: `e2e-tests` is a separate module to prevent polluting production service logic.

---

# Phase 5: Testing Guide & Operations 🧪

This guide details how to execute the comprehensive test suite for PizzaFlow, including Unit, Integration, and End-to-End (E2E) tests.

## 1. Unit Tests (Fast & Isolated)
**Dependencies**: Java 21, Maven.
**Scope**: Business logic in isolation (Mocks only).

### Running all unit tests:
```powershell
./mvnw clean test
```

### Running specific service unit tests:
```powershell
./mvnw -pl services/order-service test
```

---

## 2. Integration Tests (Infrastructure Verified)
**Dependencies**: Docker Desktop (must be running).
**Scope**: Database, Kafka, Redis interactions using **Testcontainers**.

### Running integration tests:
The standard `mvn test` command includes files ending in `*Test.java`. We follow the convention:
- Unit Tests: `*Test.java`
- Integration Tests: `*IntegrationTest.java`

To run **ONLY** integration tests (if you configured failsafe plugin, otherwise they run with unit tests):
```powershell
./mvnw verify
```

**Note**: The first run will download Docker images (`postgres:16-alpine`, `mongo:6.0`, `confluentinc/cp-kafka`, `redis:7.0`). This may take time.

---

## 3. End-to-End (E2E) Tests (Full User Journey)
**Dependencies**: Docker Desktop, Running PizzaFlow Environment.
**Scope**: Validates the system from the outside (RestAssured -> API Gateway).

### Step 1: Start the Environment
Ensure your local environment is running via Docker Compose (or Helm):
```powershell
docker-compose up -d
```
*Wait until all services are healthy (approx. 2-3 mins).*

### Step 2: Run E2E Suite
Execute the tests in the `e2e-tests` module:
```powershell
./mvnw test -pl e2e-tests
```

### Scenarios Covered
1.  **Happy Path**: 
    - Browse Catalog -> Create Order -> Pay (Success) -> Verify `PAID` status.
2.  **Payment Failure Path**:
    - Create Order -> Pay with amount `0.99` (Trigger Decline) -> Verify `PAYMENT_FAILED` status.

---

## Troubleshooting
-   **Docker Connectivity**: If Testcontainers fails, ensure your Docker Desktop is running and exposed to the WS/system.
-   **Port Conflicts**: Ensure ports 5432, 6379, 9092 are not occupied by local instances if you are running tests that try to bind default ports (Testcontainers uses random high ports usually).
-   **E2E Flakiness**: Use `await()` with timeouts (e.g., 10s) for asynchronous state propagation (Kafka consumers need time).
