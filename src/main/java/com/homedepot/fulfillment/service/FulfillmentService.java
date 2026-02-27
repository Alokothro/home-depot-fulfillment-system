package com.homedepot.fulfillment.service;

import com.homedepot.fulfillment.dto.PickListResponse;
import com.homedepot.fulfillment.dto.ShipmentRequest;
import com.homedepot.fulfillment.entity.*;
import com.homedepot.fulfillment.exception.InvalidOrderStatusException;
import com.homedepot.fulfillment.exception.OrderNotFoundException;
import com.homedepot.fulfillment.exception.WarehouseNotFoundException;
import com.homedepot.fulfillment.repository.OrderRepository;
import com.homedepot.fulfillment.repository.ShipmentRepository;
import com.homedepot.fulfillment.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for Fulfillment operations (pick, pack, ship).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FulfillmentService {

    private static final Logger logger = LoggerFactory.getLogger(FulfillmentService.class);

    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    private final ShipmentRepository shipmentRepository;

    /**
     * Generate pick list for warehouse workers.
     */
    @Transactional(readOnly = true)
    public PickListResponse generatePickList(Long warehouseId) {
        logger.info("Generating pick list for warehouse ID: {}", warehouseId);

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));

        // Get all pending/processing orders for this warehouse
        List<Order> orders = orderRepository.findPendingOrdersByWarehouse(warehouseId);

        List<PickListResponse.PickListItem> items = new ArrayList<>();

        for (Order order : orders) {
            for (OrderItem orderItem : order.getOrderItems()) {
                PickListResponse.PickListItem item = PickListResponse.PickListItem.builder()
                    .orderId(order.getOrderId())
                    .productId(orderItem.getProduct().getProductId())
                    .productSku(orderItem.getProduct().getSku())
                    .productName(orderItem.getProduct().getName())
                    .warehouseLocation(orderItem.getProduct().getWarehouseLocation())
                    .quantityToPick(orderItem.getQuantity())
                    .customerName(order.getCustomer().getFirstName() + " " +
                                order.getCustomer().getLastName())
                    .build();

                items.add(item);
            }
        }

        PickListResponse response = PickListResponse.builder()
            .warehouseId(warehouse.getWarehouseId())
            .warehouseName(warehouse.getName())
            .generatedAt(LocalDateTime.now())
            .totalOrders(orders.size())
            .items(items)
            .build();

        logger.info("Pick list generated with {} items from {} orders", items.size(), orders.size());
        return response;
    }

    /**
     * Mark order as packed.
     */
    public void packOrder(Long orderId) {
        logger.info("Packing order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Validate order is in PROCESSING status
        if (order.getOrderStatus() != OrderStatus.PROCESSING) {
            // If PENDING, move to PROCESSING first
            if (order.getOrderStatus() == OrderStatus.PENDING) {
                order.setOrderStatus(OrderStatus.PROCESSING);
            } else {
                throw new InvalidOrderStatusException(
                    "Cannot pack order in status: " + order.getOrderStatus());
            }
        }

        order.setOrderStatus(OrderStatus.PACKED);
        orderRepository.save(order);

        logger.info("Order packed successfully");
    }

    /**
     * Ship order with tracking information.
     */
    public Shipment shipOrder(ShipmentRequest request) {
        logger.info("Shipping order ID: {} with carrier: {}", request.getOrderId(), request.getCarrier());

        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(request.getOrderId()));

        // Validate order is PACKED
        if (order.getOrderStatus() != OrderStatus.PACKED) {
            throw new InvalidOrderStatusException(
                "Cannot ship order in status: " + order.getOrderStatus() +
                ". Order must be PACKED first.");
        }

        // Generate tracking number
        String trackingNumber = generateTrackingNumber(order);
        order.setTrackingNumber(trackingNumber);
        order.setOrderStatus(OrderStatus.SHIPPED);

        // Create shipment record
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setCarrier(request.getCarrier());
        shipment.setTrackingNumber(trackingNumber);
        shipment.setEstimatedDelivery(request.getEstimatedDelivery());
        shipment.setShipmentStatus(ShipmentStatus.IN_TRANSIT);

        orderRepository.save(order);
        Shipment savedShipment = shipmentRepository.save(shipment);

        logger.info("Order shipped successfully with tracking number: {}", trackingNumber);
        return savedShipment;
    }

    /**
     * Confirm delivery.
     */
    public void confirmDelivery(Long orderId) {
        logger.info("Confirming delivery for order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getOrderStatus() != OrderStatus.SHIPPED) {
            throw new InvalidOrderStatusException(
                "Cannot mark as delivered - order status is: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        // Update shipment status
        shipmentRepository.findByOrder(order).ifPresent(shipment -> {
            shipment.setShipmentStatus(ShipmentStatus.DELIVERED);
            shipment.setActualDelivery(LocalDateTime.now());
            shipmentRepository.save(shipment);
        });

        logger.info("Order delivery confirmed");
    }

    /**
     * Generate tracking number in format: HD-{warehouseId}-{orderId}-{timestamp}.
     */
    private String generateTrackingNumber(Order order) {
        long timestamp = System.currentTimeMillis();
        Long warehouseId = order.getWarehouse() != null ? order.getWarehouse().getWarehouseId() : 0;

        return String.format("HD-%d-%d-%d", warehouseId, order.getOrderId(), timestamp);
    }
}
