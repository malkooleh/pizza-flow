package com.pizzaflow.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HappyPathE2ETest {

    private static String BASE_URL = "http://localhost:8080/api/v1";
    private static Long orderId;
    private static String productId;
    private static Float price;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    @Order(1)
    void shouldGetPizzaProductFromCatalog() {
        List<Map<String, Object>> products = given()
                .when()
                .get("/catalog/products/PIZZA")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .extract().body().jsonPath().getList("$");

        Map<String, Object> firstProduct = products.get(0);
        productId = (String) firstProduct.get("id");
        // Handle different number types (Integer/Float/Double) safely
        price = ((Number) firstProduct.get("price")).floatValue();

        System.out.println("Selected Product: " + productId + " Price: " + price);
    }

    @Test
    @Order(2)
    void shouldCreateOrder() {
        Map<String, Object> item = Map.of(
                "productId", productId,
                "quantity", 2,
                "unitPrice", price
        );

        Map<String, Object> orderRequest = Map.of(
                "customerId", 101, // Test Customer
                "deliveryAddress", "123 E2E Blvd",
                "latitude", 40.7128,
                "longitude", -74.0060,
                "items", List.of(item)
        );

        orderId = given()
                .contentType(ContentType.JSON)
                .body(orderRequest)
                .when()
                .post("/orders")
                .then()
                .statusCode(201)
                .body("status", equalTo("PENDING"))
                .extract().path("id");

        System.out.println("Created Order ID: " + orderId);
    }

    @Test
    @Order(3)
    void shouldPayForOrder() {
        Float totalAmount = price * 2;
        Map<String, Object> paymentRequest = Map.of(
                "orderId", orderId,
                "amount", totalAmount,
                "currency", "USD",
                "paymentMethod", "CREDIT_CARD"
        );

        given()
                .contentType(ContentType.JSON)
                .body(paymentRequest)
                .when()
                .post("/payments")
                .then()
                .statusCode(200); // Or 201 depending on impl
        //.body("status", equalTo("APPROVED"));
    }

    @Test
    @Order(4)
    void shouldVerifyOrderTransformedToPaid() {
        // Poll until status becomes PAID
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .when()
                    .get("/orders/" + orderId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("PAID"));
        });
    }
}
