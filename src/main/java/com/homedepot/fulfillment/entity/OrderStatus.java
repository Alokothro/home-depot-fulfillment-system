package com.homedepot.fulfillment.entity;

/**
 * Enum representing the lifecycle status of an order.
 */
public enum OrderStatus {
    PENDING,      // Order created, awaiting processing
    PROCESSING,   // Order currently being picked
    PARTIAL,      // Associate picked what was available; shortage noted
    PACKED,       // All items picked and packed
    SHIPPED,      // Order shipped to customer
    DELIVERED,    // Order delivered to customer
    CANCELLED     // Order cancelled
}
