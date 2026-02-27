package com.homedepot.fulfillment.exception;

/**
 * Exception thrown when there is insufficient inventory to fulfill an order.
 */
public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(String message) {
        super(message);
    }

    public InsufficientInventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
