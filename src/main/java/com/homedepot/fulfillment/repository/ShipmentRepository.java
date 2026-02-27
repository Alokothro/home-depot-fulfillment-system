package com.homedepot.fulfillment.repository;

import com.homedepot.fulfillment.entity.Order;
import com.homedepot.fulfillment.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Shipment entity operations.
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * Find shipment by order.
     */
    Optional<Shipment> findByOrder(Order order);

    /**
     * Find shipment by tracking number.
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}
