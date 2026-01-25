package com.pizzaflow.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    @GetMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("Service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/fallback/catalog")
    public Mono<String> catalogFallback() {
        // In a real scenario, this could return a default cached menu or a "limited
        // service" message
        return Mono.just("{\"products\": [], \"message\": \"Catalog service is currently unavailable.\"}");
    }
}
