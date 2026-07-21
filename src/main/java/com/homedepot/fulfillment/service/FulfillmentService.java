package com.homedepot.fulfillment.service;

import com.homedepot.fulfillment.dto.OrderPickListResponse;
import com.homedepot.fulfillment.dto.PickListResponse;
import com.homedepot.fulfillment.dto.PickProgressRequest;
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
import org.springframework.lang.NonNull;
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
    public PickListResponse generatePickList(@NonNull Long warehouseId) {
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
     * Build the pick list for a single order (used by the associate picking screen).
     */
    @Transactional(readOnly = true)
    public OrderPickListResponse getOrderPickList(@NonNull Long orderId) {
        logger.info("Building pick list for order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        List<OrderPickListResponse.PickLine> lines = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            lines.add(OrderPickListResponse.PickLine.builder()
                .productId(product.getProductId())
                .sku(product.getSku())
                .productName(product.getName())
                .department(product.getDepartment())
                .location(product.getWarehouseLocation())
                .quantity(item.getQuantity())
                .pickedQuantity(item.getPickedQuantity() != null ? item.getPickedQuantity() : 0)
                .build());
        }

        int totalItems = lines.stream().mapToInt(OrderPickListResponse.PickLine::getQuantity).sum();

        return OrderPickListResponse.builder()
            .orderId(order.getOrderId())
            .orderNumber(String.format("HD-%s-%05d",
                    order.getOrderDate() != null
                        ? order.getOrderDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                        : java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")),
                    order.getOrderId()))
            .customerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName())
            .status(order.getOrderStatus().toString())
            .shippingMethod(order.getShippingMethod())
            .totalItems(totalItems)
            .lines(lines)
            .build();
    }

    /**
     * Save per-item pick progress so it survives a back-out and re-open.
     */
    public void savePickProgress(@NonNull Long orderId, @NonNull PickProgressRequest request) {
        logger.info("Saving pick progress for order {}", orderId);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        for (PickProgressRequest.ItemUpdate update : request.getItems()) {
            order.getOrderItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(update.getProductId()))
                .findFirst()
                .ifPresent(item -> item.setPickedQuantity(
                    Math.min(update.getPickedQuantity(), item.getQuantity())));
        }
        orderRepository.save(order);
    }

    /**
     * Mark order as partially fulfilled (associate picked what was available; shortage remains).
     */
    public void markPartial(@NonNull Long orderId) {
        logger.info("Marking order {} as partial", orderId);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getOrderStatus() == OrderStatus.PENDING
                || order.getOrderStatus() == OrderStatus.PROCESSING) {
            order.setOrderStatus(OrderStatus.PARTIAL);
            orderRepository.save(order);
            logger.info("Order {} marked as PARTIAL", orderId);
        }
        // Already PARTIAL or beyond — no-op
    }

    /**
     * Mark order as packed.
     */
    public void packOrder(@NonNull Long orderId) {
        logger.info("Packing order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Accept PENDING, PROCESSING, or PARTIAL as valid starting statuses
        OrderStatus current = order.getOrderStatus();
        if (current != OrderStatus.PROCESSING && current != OrderStatus.PARTIAL) {
            if (current == OrderStatus.PENDING) {
                order.setOrderStatus(OrderStatus.PROCESSING);
            } else {
                throw new InvalidOrderStatusException(
                    "Cannot pack order in status: " + current);
            }
        }

        order.setOrderStatus(OrderStatus.PACKED);
        orderRepository.save(order);

        logger.info("Order packed successfully");
    }

    /**
     * Ship order with tracking information.
     */
    public Shipment shipOrder(@NonNull ShipmentRequest request) {
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
    public void confirmDelivery(@NonNull Long orderId) {
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
    private String generateTrackingNumber(@NonNull Order order) {
        long timestamp = System.currentTimeMillis();
        Long warehouseId = order.getWarehouse() != null ? order.getWarehouse().getWarehouseId() : 0;

        return String.format("HD-%d-%d-%d", warehouseId, order.getOrderId(), timestamp);
    }
}
