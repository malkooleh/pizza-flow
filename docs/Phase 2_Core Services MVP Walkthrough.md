# Phase 2: Core Services MVP Walkthrough

## 1. Changes Made
- **Catalog Service:**
    - Implemented with MongoDB.
    - Added Redis Caching for products (TTL policies).
    - Refactored Categories to use Enums.
- **Order Service:**
    - Implemented basic Order Lifecycle.
    - Integrated Kafka Producer (`order.created`).
    - PostgreSQL persistence.
- **Payment Service:**
    - Implemented Mock Payment Gateway (Random success/failure).
    - Integrated Kafka Consumer (`order.created`) and Producer (`payment.completed`).
    - PostgreSQL persistence.
- **Kitchen Service:**
    - Implemented Kitchen Display System (KDS) logic.
    - **Dual Storage Strategy:** Active orders in **Redis** for speed, History in **PostgreSQL**.
    - Integrated Kafka Consumer (`payment.completed`).
    - **WebSocket** broadcasting for real-time updates.

## 2. How to Run

### Step 1: Ensure Infrastructure is Running
```bash
cd infrastructure/docker
docker-compose up -d
```
*Ensure Postgres, Mongo, Redis, Kafka, and Keycloak are healthy.*

### Step 2: Build Project
```bash
./mvnw clean install -DskipTests
```

### Step 3: Run Services
Run each of these in a separate terminal:

**1. Foundation (If not running):**
```bash
./mvnw spring-boot:run -pl services/discovery-service
./mvnw spring-boot:run -pl services/config-service
./mvnw spring-boot:run -pl services/api-gateway
```

**2. Core Services:**
```bash
./mvnw spring-boot:run -pl services/catalog-service
./mvnw spring-boot:run -pl services/order-service
./mvnw spring-boot:run -pl services/payment-service
./mvnw spring-boot:run -pl services/kitchen-service
```

## 3. Verification Steps

### 1. Catalog Service (Caching)
- **Get Products:** `GET http://localhost:8080/api/v1/catalog/products`
- **Verify Cache:** Check Redis keys (`keys catalog*`).

### 2. End-to-End Order Flow (Saga)
1.  **Create Order:**
    - `POST /api/v1/orders` (Order Service)
    - Status: `CREATED`
2.  **Payment Processing (Async):**
    - Payment Service consumes `order.created`.
    - Processes mock payment (90% success).
    - Publishes `payment.completed`.
3.  **Kitchen Display (Async):**
    - Kitchen Service consumes `payment.completed`.
    - Creates Ticket in Redis/Postgres.
    - **WebSocket:** Broadcasts to `/topic/kitchen/updates`.

### 3. Kitchen Queue
- **View Queue:** `GET http://localhost:8080/api/v1/kitchen/queue`
- **Verify Redis:** Check `kitchen:active_orders` hash.
