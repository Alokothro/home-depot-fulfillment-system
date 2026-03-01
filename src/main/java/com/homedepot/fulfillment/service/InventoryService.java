package com.homedepot.fulfillment.service;

import com.homedepot.fulfillment.dto.InventoryTransferRequest;
import com.homedepot.fulfillment.dto.LowStockAlertResponse;
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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Inventory management operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    /**
     * Get inventory by warehouse.
     */
    @Transactional(readOnly = true)
    public List<Inventory> getInventoryByWarehouse(@NonNull Long warehouseId) {
        logger.debug("Fetching inventory for warehouse ID: {}", warehouseId);

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));

        return inventoryRepository.findByWarehouse(warehouse);
    }

    /**
     * Get inventory levels for a specific product.
     */
    @Transactional(readOnly = true)
    public List<Inventory> getInventoryByProduct(@NonNull Long productId) {
        logger.debug("Fetching inventory for product ID: {}", productId);

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        return inventoryRepository.findByProduct(product);
    }

    /**
     * Restock inventory.
     */
    public Inventory restockInventory(@NonNull RestockRequest request) {
        logger.info("Restocking product ID {} at warehouse ID {} with quantity {}",
            request.getProductId(), request.getWarehouseId(), request.getQuantity());

        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
            .orElseThrow(() -> new WarehouseNotFoundException(request.getWarehouseId()));

        // Find or create inventory record
        Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse)
            .orElse(new Inventory());

        if (inventory.getInventoryId() == null) {
            inventory.setProduct(product);
            inventory.setWarehouse(warehouse);
            inventory.setQuantity(0);
        }

        // Add quantity
        inventory.setQuantity(inventory.getQuantity() + request.getQuantity());
        inventory.setLastRestocked(LocalDateTime.now());

        // Update warehouse utilization
        warehouse.setCurrentUtilization(warehouse.getCurrentUtilization() + request.getQuantity());
        warehouseRepository.save(warehouse);

        Inventory saved = inventoryRepository.save(inventory);
        logger.info("Inventory restocked successfully. New quantity: {}", saved.getQuantity());
        return saved;
    }

    /**
     * Get low stock alerts.
     */
    @Transactional(readOnly = true)
    public LowStockAlertResponse getLowStockAlerts() {
        logger.debug("Fetching low stock alerts");

        List<Inventory> lowStockItems = inventoryRepository.findLowStockItems();

        List<LowStockAlertResponse.LowStockItem> items = lowStockItems.stream()
            .map(inv -> LowStockAlertResponse.LowStockItem.builder()
                .inventoryId(inv.getInventoryId())
                .productId(inv.getProduct().getProductId())
                .productSku(inv.getProduct().getSku())
                .productName(inv.getProduct().getName())
                .warehouseId(inv.getWarehouse().getWarehouseId())
                .warehouseName(inv.getWarehouse().getName())
                .currentQuantity(inv.getQuantity())
                .minimumStockLevel(inv.getMinimumStockLevel())
                .shortfall(inv.getMinimumStockLevel() - inv.getQuantity())
                .build())
            .collect(Collectors.toList());

        return LowStockAlertResponse.builder()
            .totalLowStockItems(items.size())
            .items(items)
            .build();
    }

    /**
     * Transfer inventory between warehouses.
     */
    public void transferInventory(@NonNull InventoryTransferRequest request) {
        logger.info("Transferring {} units of product ID {} from warehouse {} to warehouse {}",
            request.getQuantity(), request.getProductId(),
            request.getFromWarehouseId(), request.getToWarehouseId());

        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        Warehouse fromWarehouse = warehouseRepository.findById(request.getFromWarehouseId())
            .orElseThrow(() -> new WarehouseNotFoundException(request.getFromWarehouseId()));

        Warehouse toWarehouse = warehouseRepository.findById(request.getToWarehouseId())
            .orElseThrow(() -> new WarehouseNotFoundException(request.getToWarehouseId()));

        // Get source inventory
        Inventory fromInventory = inventoryRepository.findByProductAndWarehouse(product, fromWarehouse)
            .orElseThrow(() -> new InsufficientInventoryException(
                "No inventory found for product " + product.getSku() + " at warehouse " + fromWarehouse.getName()));

        // Check if sufficient quantity available
        if (fromInventory.getQuantity() < request.getQuantity()) {
            throw new InsufficientInventoryException(
                "Insufficient inventory at warehouse " + fromWarehouse.getName() +
                ". Available: " + fromInventory.getQuantity() + ", Required: " + request.getQuantity());
        }

        // Decrease from source
        fromInventory.setQuantity(fromInventory.getQuantity() - request.getQuantity());
        fromWarehouse.setCurrentUtilization(fromWarehouse.getCurrentUtilization() - request.getQuantity());

        // Get or create destination inventory
        Inventory toInventory = inventoryRepository.findByProductAndWarehouse(product, toWarehouse)
            .orElse(new Inventory());

        if (toInventory.getInventoryId() == null) {
            toInventory.setProduct(product);
            toInventory.setWarehouse(toWarehouse);
            toInventory.setQuantity(0);
        }

        // Increase at destination
        toInventory.setQuantity(toInventory.getQuantity() + request.getQuantity());
        toInventory.setLastRestocked(LocalDateTime.now());
        toWarehouse.setCurrentUtilization(toWarehouse.getCurrentUtilization() + request.getQuantity());

        // Save all changes
        inventoryRepository.save(fromInventory);
        inventoryRepository.save(toInventory);
        warehouseRepository.save(fromWarehouse);
        warehouseRepository.save(toWarehouse);

        logger.info("Inventory transfer completed successfully");
    }

    /**
     * Decrease inventory (internal method used by order service).
     */
    public void decreaseInventory(@NonNull Product product, @NonNull Warehouse warehouse, @NonNull Integer quantity) {
        logger.debug("Decreasing inventory for product {} at warehouse {} by {}",
            product.getSku(), warehouse.getName(), quantity);

        Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse)
            .orElseThrow(() -> new InsufficientInventoryException(
                "No inventory found for product " + product.getSku() + " at warehouse " + warehouse.getName()));

        if (inventory.getQuantity() < quantity) {
            throw new InsufficientInventoryException(
                "Insufficient inventory for product " + product.getSku() +
                ". Available: " + inventory.getQuantity() + ", Required: " + quantity);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        warehouse.setCurrentUtilization(warehouse.getCurrentUtilization() - quantity);

        inventoryRepository.save(inventory);
        warehouseRepository.save(warehouse);

        logger.debug("Inventory decreased. New quantity: {}", inventory.getQuantity());
    }

    /**
     * Increase inventory (internal method used for order cancellation).
     */
    public void increaseInventory(@NonNull Product product, @NonNull Warehouse warehouse, @NonNull Integer quantity) {
        logger.debug("Increasing inventory for product {} at warehouse {} by {}",
            product.getSku(), warehouse.getName(), quantity);

        Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse)
            .orElseThrow(() -> new IllegalArgumentException(
                "No inventory record found for product " + product.getSku() + " at warehouse " + warehouse.getName()));

        inventory.setQuantity(inventory.getQuantity() + quantity);
        warehouse.setCurrentUtilization(warehouse.getCurrentUtilization() + quantity);

        inventoryRepository.save(inventory);
        warehouseRepository.save(warehouse);

        logger.debug("Inventory increased. New quantity: {}", inventory.getQuantity());
    }
}
