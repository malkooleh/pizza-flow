package com.pizzaflow.catalog.service;

import com.pizzaflow.catalog.domain.Product;
import com.pizzaflow.catalog.domain.ProductCategory;
import com.pizzaflow.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.pizzaflow.catalog.dto.ProductRequest;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ProductRepository productRepository;

    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Cacheable(value = "product", key = "#id")
    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    @Cacheable(value = "products_category", key = "#category")
    public List<Product> getProductsByCategory(ProductCategory category) {
        return productRepository.findByCategory(category);
    }

    @CacheEvict(value = {"products", "product", "products_category"}, allEntries = true)
    public Product createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .ingredients(request.getIngredients())
                .available(request.isAvailable())
                .build();
        return productRepository.save(product);
    }

    @CacheEvict(value = {"products", "product", "products_category"}, allEntries = true)
    public Optional<Product> updateProduct(String id, ProductRequest request) {
        return productRepository.findById(id).map(existing -> {
            existing.setName(request.getName());
            existing.setDescription(request.getDescription());
            existing.setPrice(request.getPrice());
            existing.setCategory(request.getCategory());
            existing.setImageUrl(request.getImageUrl());
            existing.setIngredients(request.getIngredients());
            existing.setAvailable(request.isAvailable());
            return productRepository.save(existing);
        });
    }

    @CacheEvict(value = {"products", "product", "products_category"}, allEntries = true)
    public boolean deleteProduct(String id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
