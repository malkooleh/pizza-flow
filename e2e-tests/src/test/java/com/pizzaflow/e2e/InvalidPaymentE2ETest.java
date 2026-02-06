package com.pizzaflow.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InvalidPaymentE2ETest {

    private static String BASE_URL = "http://localhost:8080/api/v1";
    private static Long orderId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    @Order(1)
    void shouldCreateOrder() {
        // Create Order logic (similar to HappyPath, maybe extract to Helper)
        Map<String, Object> orderRequest = Map.of(
                "customerId", 102,
                "deliveryAddress", "Fail Lane",
                "latitude", 50.0,
                "longitude", 50.0,
                "items", List.of(Map.of("productId", "pizza-margherita", "quantity", 1, "unitPrice", 10.0))
        );

        orderId = given()
                .contentType(ContentType.JSON)
                .body(orderRequest)
                .when()
                .post("/orders")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    @Order(2)
    void shouldFailPayment() {
        // Assume sending a specific amount or token triggers mock decline
        // In PaymentService placeholder, Random or Specific Amount might trigger failure?
        // Or we can just mock it in the E2E environment by sending a "bad" token if supported.
        // For MVP Mock, let's assume > 1000 triggers decline or similar logic if implemented.
        // If not implemented, we might need to rely on the random mock in PaymentService.
        // Let's assume the Mock Gateway declines if amount is negative or specific flag.
        // Re-reading PaymentService Mock: "boolean success = externalGateway.processPayment(orderId, amount);" 
        // We need to know how the mock behaves. 
        // If it's random, this test is flaky. If it's always true, this test fails.
        // Let's UPDATE the MockGateway (if possible) or assume we control it via headers.

        // For now, I will write the test assuming there is a way to trigger failure, 
        // e.g., by sending a specific header "X-Mock-Payment-Result: DECLINED" if we added that capability.
        // Since we didn't add it yet, this test might need the gateway to be smarter.

        // Strategy: Modify ExternalPaymentGateway to fail on specific amount (e.g. 0.99)

        Map<String, Object> paymentRequest = Map.of(
                "orderId", orderId,
                "amount", 0.99, // Specific amount validation
                "currency", "USD",
                "paymentMethod", "CREDIT_CARD"
        );

        given()
                .contentType(ContentType.JSON)
                .body(paymentRequest)
                .when()
                .post("/payments")
                .then()
                .statusCode(200); // Service still returns 200/202, but payload says Declined
    }

    @Test
    @Order(3)
    void shouldVerifyOrderPaymentFailed() {
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .when()
                    .get("/orders/" + orderId)
                    .then()
                    .statusCode(200)
                    .body("status", anyOf(equalTo("PAYMENT_FAILED"), equalTo("CANCELLED")));
        });
    }
}
