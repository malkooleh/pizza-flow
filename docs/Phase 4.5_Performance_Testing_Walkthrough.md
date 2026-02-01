# Phase 4.5: Performance & Load Testing Walkthrough

This phase focused on verifying the system's stability and performance under high load using **k6**.

## 1. Load Testing Strategy 🚀

- **Tool**: [k6](https://k6.io/) (Developer-centric, scriptable load testing).
- **Location**: `infrastructure/k6/order-flow.js`.
- **Target Flow**: The critical user journey:
    1.  **Browse Catalog**: `GET /api/v1/catalog/products/PIZZA`
    2.  **Create Order**: `POST /api/v1/orders`
    3.  **Pay**: `POST /api/v1/payments`

## 2. The Test Script (`order-flow.js`)

The script is designed to simulate realistic traffic patterns.

### Configuration (`options`)
```javascript
export const options = {
  stages: [
    { duration: '30s', target: 50 }, // Ramp up to 50 users
    { duration: '1m', target: 50 },  // Stay at 50 users
    { duration: '30s', target: 0 },  // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<200'], // 95% of requests must complete below 200ms
    http_req_failed: ['rate<0.01'],    // <1% errors
  },
};
```

### Execution Logic
1.  **Catalog Check**: Verifies that products are returned (Status 200).
2.  **Order Creation**: Picks a random product and places an order.
3.  **Payment**: Simulates a payment transaction for the created order.
4.  **Checks**: Asserts that all steps return success codes (200/201).

## 3. Running the Test 🏃‍♂️

To run the load test locally (requires k6 installed):

```bash
cd infrastructure/k6
k6 run order-flow.js
```

## 4. Autoscaling (HPA) 📊

In a Kubernetes environment, this load test is used to trigger **Horizontal Pod Autoscaling (HPA)**.
- **Trigger**: CPU utilization > 70%.
- **Action**: K8s adds more replicas of `order-service` and `kitchen-service` to handle the load.
- **Observation**: Monitor Grafana dashboards during the test to see the pod count increase.
