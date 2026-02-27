package com.homedepot.fulfillment.controller;

import com.homedepot.fulfillment.dto.InventoryTransferRequest;
import com.homedepot.fulfillment.dto.LowStockAlertResponse;
import com.homedepot.fulfillment.dto.RestockRequest;
import com.homedepot.fulfillment.entity.Inventory;
import com.homedepot.fulfillment.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Inventory management.
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Get inventory by warehouse")
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<Inventory>> getInventoryByWarehouse(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(inventoryService.getInventoryByWarehouse(warehouseId));
    }

    @Operation(summary = "Get inventory levels for a product")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Inventory>> getInventoryByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProduct(productId));
    }

    @Operation(summary = "Restock inventory")
    @PutMapping("/restock")
    public ResponseEntity<Inventory> restockInventory(@Valid @RequestBody RestockRequest request) {
        return ResponseEntity.ok(inventoryService.restockInventory(request));
    }

    @Operation(summary = "Get low stock alerts")
    @GetMapping("/low-stock")
    public ResponseEntity<LowStockAlertResponse> getLowStockAlerts() {
        return ResponseEntity.ok(inventoryService.getLowStockAlerts());
    }

    @Operation(summary = "Transfer inventory between warehouses")
    @PostMapping("/transfer")
    public ResponseEntity<Void> transferInventory(@Valid @RequestBody InventoryTransferRequest request) {
        inventoryService.transferInventory(request);
        return ResponseEntity.ok().build();
    }
}
