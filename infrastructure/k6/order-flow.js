import http from 'k6/http';
import { check, sleep } from 'k6';

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

const BASE_URL = 'http://localhost:8080/api/v1'; // API Gateway

export default function () {
  // 1. Browse Catalog (Get Pizza)
  const catalogRes = http.get(`${BASE_URL}/catalog/products/PIZZA`);
  check(catalogRes, {
    'catalog status is 200': (r) => r.status === 200,
    'got pizzas': (r) => r.json().length > 0,
  });

  if (catalogRes.status !== 200) {
    sleep(1);
    return;
  }
  
  const products = catalogRes.json();
  const randomProduct = products[Math.floor(Math.random() * products.length)];

  // 2. Create Order
  const orderPayload = JSON.stringify({
    customerId: 'user-123', // In a real test, this would be dynamic
    items: [
      {
        productId: randomProduct.id,
        quantity: 1
      }
    ],
    deliveryAddress: {
      street: "123 Main St",
      city: "New York",
      zipCode: "10001"
    }
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const orderRes = http.post(`${BASE_URL}/orders`, orderPayload, params);
  
  const orderCheck = check(orderRes, {
    'order created status is 201': (r) => r.status === 201,
  });

  if (!orderCheck) {
     console.log(`Order creation failed: ${orderRes.status} ${orderRes.body}`);
     sleep(1);
     return;
  }

  const orderId = orderRes.json('id');

  // 3. Make Payment (Mock)
  // Note: specific payment endpoint logic depends on how the client triggers it.
  // Assuming the client calls payment service directly via gateway.
  const paymentPayload = JSON.stringify({
    orderId: orderId,
    amount: orderRes.json('totalAmount'),
    currency: "USD",
    paymentMethod: "CREDIT_CARD"
  });

  const paymentRes = http.post(`${BASE_URL}/payments`, paymentPayload, params);
  check(paymentRes, {
    'payment status is 201/200': (r) => r.status === 201 || r.status === 200,
  });

  sleep(1);
}
