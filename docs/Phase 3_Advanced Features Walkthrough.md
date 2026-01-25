# Phase 3: Advanced Features Walkthrough

## 1. Changes Made

- **Booking Service:**
    - Implemented Table management and reservation logic.
    - Added Availability checking for specific time slots.
    - PostgreSQL persistence with JPA Auditing.
- **Inventory Service:**
    - Implemented Stock management with **Optimistic Locking** (`@Version`).
    - **Outbox Pattern:** Implemented for reliable Kafka event publishing via a transactional event table.
    - **Kafka Integration:** Consumes `order.created` to reserve stock; publishes `inventory.reserved` or `inventory.unavailable`.
    - Automated retry logic with **Exponential Backoff** for failed outbox events.
- **Delivery Service:**
    - **PostGIS Integration:** Implemented geospatial logic for courier tracking and delivery zone validation.
    - **Nearest Neighbor Search:** Automated courier assignment using PostGIS `<->` operator for optimized lookup.
    - **Kafka Integration:** Consumes `order.created` for delivery initialization and `kitchen.ready` for courier assignment.

## 2. How to Run

### Step 1: Ensure PostGIS is available
The `delivery-service` requires PostGIS. The `docker-compose.yml` should be using a PostGIS-enabled Postgres image.

### Step 2: Build Project
```bash
./mvnw clean install -DskipTests
```

### Step 3: Run Advanced Services
Run each of these in a separate terminal:

**1. Advanced Features:**
```bash
./mvnw spring-boot:run -pl services/booking-service
./mvnw spring-boot:run -pl services/inventory-service
./mvnw spring-boot:run -pl services/delivery-service
```

## 3. Verification Steps

### 1. Booking Service
- **Register Table:** `POST /api/v1/bookings/tables`
- **Create Booking:** `POST /api/v1/bookings`
- **Check Availability:** `GET /api/v1/bookings/tables` (verify availability is updated).

### 2. Inventory Service (Outbox Pattern)
- **Place Order:** Triggered via `order-service`.
- **Verify Reservation:** Check `stock_reservation` and `inventory_item` tables in `inventory_db`.
- **Outbox Check:** Verify `outbox_event` table. Successfully published events should have status `PUBLISHED`.
- **Stock Release:** Test by cancelling an order (Compensation flow in Saga).

### 3. Delivery Service (Geospatial)
- **Register Courier:** `POST /api/v1/deliveries/couriers` with longitude/latitude.
- **Trigger Assignment:** Mark a kitchen order as `READY` (via `kitchen-service` API).
- **Verify Assignment:** 
    - Check `delivery` table in `delivery_db`.
    - Courier status should change to `BUSY`.
    - Nearest available courier should be selected.
- **Status Updates:** `PATCH /api/v1/deliveries/{orderId}/status?status=DELIVERED` (Courier becomes `AVAILABLE` again).
