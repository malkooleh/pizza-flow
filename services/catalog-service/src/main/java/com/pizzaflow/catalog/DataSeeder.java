package com.pizzaflow.catalog;

import com.pizzaflow.catalog.domain.Product;
import com.pizzaflow.catalog.domain.ProductCategory;
import com.pizzaflow.catalog.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            Product margherita = Product.builder()
                    .name("Pizza Margherita")
                    .description("Classic tomato and mozzarella")
                    .price(new BigDecimal("12.99"))
                    .category(ProductCategory.PIZZA)
                    .imageUrl("https://example.com/margherita.jpg")
                    .ingredients(Arrays.asList("Tomato Sauce", "Mozzarella", "Basil"))
                    .available(true)
                    .build();

            Product pepperoni = Product.builder()
                    .name("Pizza Pepperoni")
                    .description("Spicy pepperoni with cheese")
                    .price(new BigDecimal("14.99"))
                    .category(ProductCategory.PIZZA)
                    .imageUrl("https://example.com/pepperoni.jpg")
                    .ingredients(Arrays.asList("Tomato Sauce", "Mozzarella", "Pepperoni"))
                    .available(true)
                    .build();
            
            Product coke = Product.builder()
                    .name("Coca Cola")
                    .description("Refreshing cola drink")
                    .price(new BigDecimal("2.50"))
                    .category(ProductCategory.DRINK)
                    .imageUrl("https://example.com/coke.jpg")
                    .ingredients(Arrays.asList("Water", "Sugar", "Secret Formula"))
                    .available(true)
                    .build();

            productRepository.saveAll(Arrays.asList(margherita, pepperoni, coke));
            log.info("Catalog data seeded!");
        }
    }
}
