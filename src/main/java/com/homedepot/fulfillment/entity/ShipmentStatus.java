package com.homedepot.fulfillment.entity;

/**
 * Enum representing shipment status.
 */
public enum ShipmentStatus {
    PENDING,      // Shipment created but not yet picked up
    IN_TRANSIT,   // Shipment in transit
    OUT_FOR_DELIVERY, // Out for delivery
    DELIVERED,    // Successfully delivered
    FAILED,       // Delivery failed
    RETURNED      // Returned to sender
}
