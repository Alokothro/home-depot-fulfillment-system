package com.homedepot.fulfillment.service;

import com.homedepot.fulfillment.dto.RestockRequest;
import com.homedepot.fulfillment.entity.Inventory;
import com.homedepot.fulfillment.entity.Product;
import com.homedepot.fulfillment.entity.Warehouse;
import com.homedepot.fulfillment.exception.InsufficientInventoryException;
import com.homedepot.fulfillment.exception.ProductNotFoundException;
import com.homedepot.fulfillment.exception.WarehouseNotFoundException;
import com.homedepot.fulfillment.repository.InventoryRepository;
import com.homedepot.fulfillment.repository.ProductRepository;
import com.homedepot.fulfillment.repository.WarehouseRepository;
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
 * Unit tests for InventoryService.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product testProduct;
    private Warehouse testWarehouse;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setSku("TEST-001");
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));

        testWarehouse = new Warehouse();
        testWarehouse.setWarehouseId(1L);
        testWarehouse.setName("Test Warehouse");
        testWarehouse.setCapacity(10000);
        testWarehouse.setCurrentUtilization(1000);

        testInventory = new Inventory();
        testInventory.setInventoryId(1L);
        testInventory.setProduct(testProduct);
        testInventory.setWarehouse(testWarehouse);
        testInventory.setQuantity(100);
        testInventory.setMinimumStockLevel(10);
    }

    @Test
    void getInventoryByWarehouse_ShouldReturnInventoryList() {
        // Arrange
        List<Inventory> inventories = Arrays.asList(testInventory);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(inventoryRepository.findByWarehouse(testWarehouse)).thenReturn(inventories);

        // Act
        List<Inventory> result = inventoryService.getInventoryByWarehouse(1L);

        // Assert
        assertEquals(1, result.size());
        verify(inventoryRepository, times(1)).findByWarehouse(testWarehouse);
    }

    @Test
    void getInventoryByWarehouse_WhenWarehouseNotExists_ShouldThrowException() {
        // Arrange
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(WarehouseNotFoundException.class, () -> {
            inventoryService.getInventoryByWarehouse(999L);
        });
    }

    @Test
    void restockInventory_WhenInventoryExists_ShouldIncreaseQuantity() {
        // Arrange
        RestockRequest request = new RestockRequest(1L, 1L, 50);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(inventoryRepository.findByProductAndWarehouse(testProduct, testWarehouse))
            .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);

        // Act
        Inventory result = inventoryService.restockInventory(request);

        // Assert
        assertNotNull(result);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    void decreaseInventory_WhenSufficientStock_ShouldDecrease() {
        // Arrange
        when(inventoryRepository.findByProductAndWarehouse(testProduct, testWarehouse))
            .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);

        // Act
        inventoryService.decreaseInventory(testProduct, testWarehouse, 10);

        // Assert
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void decreaseInventory_WhenInsufficientStock_ShouldThrowException() {
        // Arrange
        testInventory.setQuantity(5);
        when(inventoryRepository.findByProductAndWarehouse(testProduct, testWarehouse))
            .thenReturn(Optional.of(testInventory));

        // Act & Assert
        assertThrows(InsufficientInventoryException.class, () -> {
            inventoryService.decreaseInventory(testProduct, testWarehouse, 10);
        });
    }

    @Test
    void increaseInventory_WhenInventoryExists_ShouldIncrease() {
        // Arrange
        when(inventoryRepository.findByProductAndWarehouse(testProduct, testWarehouse))
            .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);

        // Act
        inventoryService.increaseInventory(testProduct, testWarehouse, 20);

        // Assert
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void getLowStockAlerts_ShouldReturnLowStockItems() {
        // Arrange
        testInventory.setQuantity(5); // Below minimum of 10
        List<Inventory> lowStockItems = Arrays.asList(testInventory);
        when(inventoryRepository.findLowStockItems()).thenReturn(lowStockItems);

        // Act
        var result = inventoryService.getLowStockAlerts();

        // Assert
        assertEquals(1, result.getTotalLowStockItems());
        assertEquals(1, result.getItems().size());
    }
}
