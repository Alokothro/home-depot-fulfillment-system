package com.homedepot.fulfillment.exception;

/**
 * Exception thrown when an invalid order status transition is attempted.
 */
public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException(String message) {
        super(message);
    }
}
