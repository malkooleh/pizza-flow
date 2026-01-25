# Phase 4.1: Resilience Walkthrough

This phase focused on making the PizzaFlow platform "cloud-hardened" by implementing advanced resilience patterns using **Resilience4j** and **Spring Cloud Gateway**.

## 1. Changes Made

### 🛡️ API Gateway Resilience
- **Circuit Breaker**: Integrated `spring-cloud-starter-circuitbreaker-reactor-resilience4j`.
    - Configured for all 7 microservices.
    - **Fail-fast behavior**: When a service is down or slow, the gateway returns a fallback response immediately instead of hanging.
- **Graceful Degradation (Fallbacks)**:
    - Implemented `FallbackController`.
    - **Catalog Fallback**: Returns a JSON with an empty product list and a "Service Unavailable" message to keep the UI functional.
    - **Standard Fallback**: Returns a generic user-friendly message for other services.
- **Request Rate Limiting**:
    - Integrated Redis-based `RequestRateLimiter`.
    - **Smart KeyResolver**: Identifies users by **Principal Name** (OIDC `sub` claim) if authenticated, falling back to **IP Address** for anonymous users.
    - **Custom Tiers**: High throughput for Kitchen updates, strict limits for Bookings/Payments to prevent abuse.

### 🔗 Service-to-Service Resilience
- **Centralized Configuration**: Moved all Resilience4j settings to the global `application.yml` in the `config-repo`.
- **Annotation-based Resilience**:
    - **@Retry**: Implemented with exponential backoff (e.g., used in `ExternalPaymentGateway` to handle 20% random failures).
    - **@Bulkhead**: Implemented to limit concurrent connections to external providers, protecting the service from resource exhaustion.

## 2. Configuration Highlights

### Resilience4j Default Settings
```yaml
resilience4j:
  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 500ms
        enableExponentialBackoff: true
  circuitbreaker:
    configs:
      default:
        failureRateThreshold: 50
        waitDurationInOpenState: 5s
        slidingWindowSize: 10
```

### Rate Limiting (API Gateway)
```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10
      redis-rate-limiter.burstCapacity: 20
      key-resolver: "#{@userKeyResolver}"
```

## 3. Verification Steps

### 1. Test Circuit Breaker
- Stop a service (e.g., `catalog-service`).
- Call `GET http://localhost:8080/api/v1/catalog/products`.
- Verify you receive the fallback JSON instead of a 500 or timeout.

### 2. Test Rate Limiting
- Use a tool like `bombardier` or a simple loop to call the Gateway rapidly.
- Verify that after exceeding the burst capacity, the Gateway returns `429 Too Many Requests`.
- Check Redis to see the rate limiter keys (`KEYS *rate_limiter*`).

### 3. Test Retries (Payment)
- Observe `payment-service` logs during a payment request.
- If the mock "External Gateway" fails, you will see `Contacting External Payment Provider...` log repeated up to 3 times before the fallback is triggered.
