package com.homedepot.fulfillment.service;

import com.homedepot.fulfillment.dto.OrderRequest;
import com.homedepot.fulfillment.dto.OrderResponse;
import com.homedepot.fulfillment.entity.*;
import com.homedepot.fulfillment.exception.CustomerNotFoundException;
import com.homedepot.fulfillment.exception.InsufficientInventoryException;
import com.homedepot.fulfillment.exception.InvalidOrderStatusException;
import com.homedepot.fulfillment.exception.OrderNotFoundException;
import com.homedepot.fulfillment.exception.ProductNotFoundException;
import com.homedepot.fulfillment.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service layer for Order processing operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryService inventoryService;

    @Value("${fulfillment.tax.rate:0.07}")
    private double taxRate;

    @Value("${fulfillment.shipping.free-threshold:50.00}")
    private double shippingFreeThreshold;

    @Value("${fulfillment.shipping.standard-cost:5.00}")
    private double shippingStandardCost;

    /**
     * Get all orders.
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        logger.debug("Fetching all orders");
        return orderRepository.findAll();
    }

    /**
     * Get order by ID.
     */
    @Transactional(readOnly = true)
    public Order getOrderById(@NonNull Long id) {
        logger.debug("Fetching order with ID: {}", id);
        return orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }

    /**
     * Get orders by customer ID.
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(@NonNull Long customerId) {
        logger.debug("Fetching orders for customer ID: {}", customerId);
        return orderRepository.findByCustomerCustomerIdOrderByOrderDateDesc(customerId);
    }

    /**
     * Create new order.
     */
    public OrderResponse createOrder(@NonNull OrderRequest request) {
        logger.info("Creating new order for customer ID: {}", request.getCustomerId());

        // Validate customer
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));

        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setShippingMethod(request.getShippingMethod() != null ? request.getShippingMethod() : "Standard");

        BigDecimal subtotal = BigDecimal.ZERO;

        // Process each order item
        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(itemRequest.getProductId()));

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());

            order.addOrderItem(orderItem);

            // Calculate subtotal
            subtotal = subtotal.add(orderItem.getLineTotal());
        }

        // Calculate tax and shipping
        order.setSubtotal(subtotal);
        order.setTax(subtotal.multiply(BigDecimal.valueOf(taxRate)).setScale(2, RoundingMode.HALF_UP));

        BigDecimal shippingCost = subtotal.compareTo(BigDecimal.valueOf(shippingFreeThreshold)) >= 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(shippingStandardCost);
        order.setShippingCost(shippingCost);

        order.setTotalAmount(order.getSubtotal().add(order.getTax()).add(order.getShippingCost()));

        // Assign to nearest warehouse with available stock
        Warehouse assignedWarehouse = findWarehouseForOrder(order);
        order.setWarehouse(assignedWarehouse);

        // Reserve inventory
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.decreaseInventory(
                Objects.requireNonNull(item.getProduct()),
                Objects.requireNonNull(assignedWarehouse),
                Objects.requireNonNull(item.getQuantity())
            );
        }

        // Save order
        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with ID: {}", savedOrder.getOrderId());

        return mapToOrderResponse(savedOrder);
    }

    /**
     * Update order status.
     */
    public OrderResponse updateOrderStatus(@NonNull Long orderId, @NonNull OrderStatus newStatus) {
        logger.info("Updating order {} status to {}", orderId, newStatus);

        Order order = getOrderById(orderId);
        OrderStatus currentStatus = order.getOrderStatus();

        // Validate status transition
        validateStatusTransition(currentStatus, newStatus);

        order.setOrderStatus(newStatus);
        Order updated = orderRepository.save(order);

        logger.info("Order status updated successfully");
        return mapToOrderResponse(updated);
    }

    /**
     * Cancel order.
     */
    public void cancelOrder(@NonNull Long orderId) {
        logger.info("Cancelling order ID: {}", orderId);

        Order order = getOrderById(orderId);

        // Can only cancel if PENDING or PROCESSING
        if (order.getOrderStatus() != OrderStatus.PENDING &&
            order.getOrderStatus() != OrderStatus.PROCESSING) {
            throw new InvalidOrderStatusException(
                "Cannot cancel order in status: " + order.getOrderStatus());
        }

        // Return inventory to warehouse
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.increaseInventory(
                Objects.requireNonNull(item.getProduct()),
                Objects.requireNonNull(order.getWarehouse()),
                Objects.requireNonNull(item.getQuantity())
            );
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        logger.info("Order cancelled successfully");
    }

    /**
     * Find warehouse for order based on inventory availability.
     * Simplified logic - in production, would use geolocation for nearest warehouse.
     */
    private Warehouse findWarehouseForOrder(@NonNull Order order) {
        logger.debug("Finding warehouse for order");

        // Get all warehouses with capacity
        List<Warehouse> warehouses = warehouseRepository.findWarehousesWithCapacity();

        if (warehouses.isEmpty()) {
            throw new InsufficientInventoryException("No warehouses available with capacity");
        }

        // Find warehouse that has all products in stock
        for (Warehouse warehouse : warehouses) {
            boolean hasAllProducts = true;

            for (OrderItem item : order.getOrderItems()) {
                List<Inventory> inventories = inventoryRepository.findWarehousesWithProductInStock(
                    item.getProduct().getProductId(),
                    item.getQuantity()
                );

                boolean warehouseHasProduct = inventories.stream()
                    .anyMatch(inv -> inv.getWarehouse().getWarehouseId().equals(warehouse.getWarehouseId()));

                if (!warehouseHasProduct) {
                    hasAllProducts = false;
                    break;
                }
            }

            if (hasAllProducts) {
                logger.debug("Assigned order to warehouse: {}", warehouse.getName());
                return warehouse;
            }
        }

        throw new InsufficientInventoryException(
            "No warehouse found with sufficient inventory for all order items");
    }

    /**
     * Validate order status transition.
     */
    private void validateStatusTransition(@NonNull OrderStatus current, @NonNull OrderStatus next) {
        // Simplified validation - can be expanded with state machine pattern
        if (current == OrderStatus.CANCELLED || current == OrderStatus.DELIVERED) {
            throw new InvalidOrderStatusException(
                "Cannot change status from " + current);
        }

        if (next == OrderStatus.CANCELLED) {
            if (current != OrderStatus.PENDING && current != OrderStatus.PROCESSING) {
                throw new InvalidOrderStatusException(
                    "Can only cancel orders in PENDING or PROCESSING status");
            }
        }
    }

    /**
     * Map Order entity to OrderResponse DTO.
     */
    private OrderResponse mapToOrderResponse(@NonNull Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getOrderItems().stream()
            .map(item -> OrderResponse.OrderItemResponse.builder()
                .orderItemId(item.getOrderItemId())
                .productId(item.getProduct().getProductId())
                .productName(item.getProduct().getName())
                .productSku(item.getProduct().getSku())
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .lineTotal(item.getLineTotal())
                .build())
            .collect(Collectors.toList());

        return OrderResponse.builder()
            .orderId(order.getOrderId())
            .customerId(order.getCustomer().getCustomerId())
            .customerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName())
            .warehouseId(order.getWarehouse() != null ? order.getWarehouse().getWarehouseId() : null)
            .warehouseName(order.getWarehouse() != null ? order.getWarehouse().getName() : null)
            .orderDate(order.getOrderDate())
            .orderStatus(order.getOrderStatus())
            .subtotal(order.getSubtotal())
            .tax(order.getTax())
            .shippingCost(order.getShippingCost())
            .totalAmount(order.getTotalAmount())
            .shippingMethod(order.getShippingMethod())
            .trackingNumber(order.getTrackingNumber())
            .items(items)
            .createdAt(order.getOrderDate())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
}
