package com.homedepot.fulfillment.entity;

/**
 * Enum representing the lifecycle status of an order.
 */
public enum OrderStatus {
    PENDING,      // Order created, awaiting processing
    PROCESSING,   // Order being prepared for shipment
    PACKED,       // Order packed and ready to ship
    SHIPPED,      // Order shipped to customer
    DELIVERED,    // Order delivered to customer
    CANCELLED     // Order cancelled
}
