package com.pizzaflow.catalog.service;

import com.pizzaflow.catalog.domain.Product;
import com.pizzaflow.catalog.domain.ProductCategory;
import com.pizzaflow.catalog.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    void getAllProducts_ShouldReturnProducts() {
        // Arrange
        Product p = new Product();
        p.setId("p1");
        p.setName("Pizza");
        when(productRepository.findAll()).thenReturn(List.of(p));

        // Act
        List<Product> result = catalogService.getAllProducts();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Pizza");
        verify(productRepository).findAll();
    }

    @Test
    void getProductsByCategory_ShouldReturnFilteredProducts() {
        // Arrange
        ProductCategory category = ProductCategory.PIZZA;
        Product p = new Product();
        p.setId("p1");
        p.setCategory(category);
        when(productRepository.findByCategory(category)).thenReturn(List.of(p));

        // Act
        List<Product> result = catalogService.getProductsByCategory(category);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(category);
        verify(productRepository).findByCategory(category);
    }
}
