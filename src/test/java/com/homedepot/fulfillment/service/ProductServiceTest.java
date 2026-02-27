package com.homedepot.fulfillment.service;

import com.homedepot.fulfillment.entity.Product;
import com.homedepot.fulfillment.exception.ProductNotFoundException;
import com.homedepot.fulfillment.repository.InventoryRepository;
import com.homedepot.fulfillment.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setSku("TEST-001");
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setCategory("Test Category");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(100);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct, new Product());
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_WhenExists_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        Product result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("TEST-001", result.getSku());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> {
            productService.getProductById(999L);
        });
    }

    @Test
    void createProduct_WhenSkuUnique_ShouldSaveProduct() {
        // Arrange
        when(productRepository.existsBySku("TEST-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.createProduct(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals("TEST-001", result.getSku());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_WhenSkuExists_ShouldThrowException() {
        // Arrange
        when(productRepository.existsBySku("TEST-001")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(testProduct);
        });
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void searchProductsByName_ShouldReturnMatchingProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByNameContainingIgnoreCase("Test")).thenReturn(products);

        // Act
        List<Product> result = productService.searchProductsByName("Test");

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().contains("Test"));
    }

    @Test
    void updateProduct_WhenExists_ShouldUpdateAndReturn() {
        // Arrange
        Product updatedDetails = new Product();
        updatedDetails.setName("Updated Name");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setCategory("Updated Category");
        updatedDetails.setPrice(new BigDecimal("199.99"));
        updatedDetails.setStockQuantity(200);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.updateProduct(1L, updatedDetails);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void deleteProduct_WhenExists_ShouldDelete() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(testProduct);

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).delete(testProduct);
    }
}
