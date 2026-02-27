package com.homedepot.fulfillment.exception;

/**
 * Exception thrown when a warehouse is not found.
 */
public class WarehouseNotFoundException extends RuntimeException {

    public WarehouseNotFoundException(String message) {
        super(message);
    }

    public WarehouseNotFoundException(Long warehouseId) {
        super("Warehouse not found with ID: " + warehouseId);
    }
}
