package com.homedepot.fulfillment.service;

import com.homedepot.fulfillment.dto.BatchOrderResponse;
import com.homedepot.fulfillment.entity.Order;
import com.homedepot.fulfillment.entity.OrderItem;
import com.homedepot.fulfillment.entity.OrderStatus;
import com.homedepot.fulfillment.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for batch order processing logic.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchOrderService {

    private static final Logger logger = LoggerFactory.getLogger(BatchOrderService.class);
    private static final int BATCH_THRESHOLD = 4; // Minimum customers needed to create a batch

    private final OrderRepository orderRepository;

    /**
     * Get all orders with batching logic applied.
     * Returns a mix of batched orders (by department) and individual orders.
     */
    public List<BatchOrderResponse> getBatchedOrders() {
        logger.debug("Fetching and batching orders");

        // Get all pending and processing orders
        List<Order> pendingOrders = orderRepository.findByOrderStatusIn(
                Arrays.asList(OrderStatus.PENDING, OrderStatus.PROCESSING, OrderStatus.PARTIAL)
        );

        if (pendingOrders.isEmpty()) {
            return Collections.emptyList();
        }

        // Group items by department across all orders
        Map<String, List<OrderItemWithCustomer>> itemsByDepartment = new HashMap<>();

        for (Order order : pendingOrders) {
            for (OrderItem item : order.getOrderItems()) {
                String department = item.getProduct().getDepartment();

                if (department != null && !department.isEmpty()) {
                    itemsByDepartment.computeIfAbsent(department, k -> new ArrayList<>())
                            .add(new OrderItemWithCustomer(order, item));
                }
            }
        }

        // Determine which departments should be batched (4+ different customers)
        Map<String, Boolean> shouldBatch = new HashMap<>();
        for (Map.Entry<String, List<OrderItemWithCustomer>> entry : itemsByDepartment.entrySet()) {
            String department = entry.getKey();
            Set<Long> uniqueCustomers = entry.getValue().stream()
                    .map(oic -> oic.order.getCustomer().getCustomerId())
                    .collect(Collectors.toSet());

            shouldBatch.put(department, uniqueCustomers.size() >= BATCH_THRESHOLD);
        }

        // Track which order items have been added to batches
        Set<OrderItemKey> batchedItems = new HashSet<>();

        List<BatchOrderResponse> result = new ArrayList<>();

        // Create batched orders for departments that meet threshold
        for (Map.Entry<String, List<OrderItemWithCustomer>> entry : itemsByDepartment.entrySet()) {
            String department = entry.getKey();

            if (shouldBatch.get(department)) {
                BatchOrderResponse batch = createBatchOrder(department, entry.getValue());
                result.add(batch);

                // Mark these items as batched
                for (OrderItemWithCustomer oic : entry.getValue()) {
                    batchedItems.add(new OrderItemKey(oic.order.getOrderId(), oic.item.getProduct().getProductId()));
                }
            }
        }

        // Create individual orders for remaining items
        for (Order order : pendingOrders) {
            List<OrderItem> remainingItems = order.getOrderItems().stream()
                    .filter(item -> !batchedItems.contains(
                            new OrderItemKey(order.getOrderId(), item.getProduct().getProductId())
                    ))
                    .collect(Collectors.toList());

            if (!remainingItems.isEmpty()) {
                BatchOrderResponse individualOrder = createIndividualOrder(order, remainingItems);
                result.add(individualOrder);
            }
        }

        // Sort: batched orders first, then individual orders
        result.sort((a, b) -> {
            if (a.isBatched() && !b.isBatched()) return -1;
            if (!a.isBatched() && b.isBatched()) return 1;
            return 0;
        });

        logger.info("Created {} batched orders and {} individual orders",
                result.stream().filter(BatchOrderResponse::isBatched).count(),
                result.stream().filter(r -> !r.isBatched()).count());

        return result;
    }

    private BatchOrderResponse createBatchOrder(String department, List<OrderItemWithCustomer> items) {
        // Group by customer
        Map<Long, List<OrderItemWithCustomer>> itemsByCustomer = items.stream()
                .collect(Collectors.groupingBy(oic -> oic.order.getCustomer().getCustomerId()));

        List<BatchOrderResponse.CustomerBatchItem> customerItems = new ArrayList<>();

        for (Map.Entry<Long, List<OrderItemWithCustomer>> entry : itemsByCustomer.entrySet()) {
            List<OrderItemWithCustomer> customerOrderItems = entry.getValue();
            OrderItemWithCustomer first = customerOrderItems.get(0);

            List<BatchOrderResponse.BatchItem> batchItems = customerOrderItems.stream()
                    .map(oic -> BatchOrderResponse.BatchItem.builder()
                            .productId(oic.item.getProduct().getProductId())
                            .productName(oic.item.getProduct().getName())
                            .sku(oic.item.getProduct().getSku())
                            .quantity(oic.item.getQuantity())
                            .location(oic.item.getProduct().getWarehouseLocation())
                            .build())
                    .collect(Collectors.toList());

            customerItems.add(BatchOrderResponse.CustomerBatchItem.builder()
                    .customerId(first.order.getCustomer().getCustomerId())
                    .customerName(first.order.getCustomer().getFirstName() + " " +
                            first.order.getCustomer().getLastName())
                    .orderId(first.order.getOrderId())
                    .items(batchItems)
                    .build());
        }

        int totalItems = items.stream().mapToInt(oic -> oic.item.getQuantity()).sum();

        return BatchOrderResponse.builder()
                .batchId(department.toUpperCase().replace(" ", "-") + "-BATCH")
                .department(department)
                .customerCount(itemsByCustomer.size())
                .totalItems(totalItems)
                .isBatched(true)
                .customers(customerItems)
                .build();
    }

    private BatchOrderResponse createIndividualOrder(Order order, List<OrderItem> items) {
        String customerName = order.getCustomer().getFirstName() + " " +
                order.getCustomer().getLastName();

        // Determine primary department for display
        String primaryDepartment = items.stream()
                .map(item -> item.getProduct().getDepartment())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Mixed");

        int totalItems = items.stream().mapToInt(OrderItem::getQuantity).sum();

        // Remaining = total minus what's already been picked (relevant for PARTIAL orders)
        int remainingItems = items.stream()
                .mapToInt(item -> Math.max(0, item.getQuantity()
                        - (item.getPickedQuantity() != null ? item.getPickedQuantity() : 0)))
                .sum();

        // Format: HD-YYYYMMDD-NNNNN — unique, scannable, lookup-able
        String orderDate = order.getOrderDate() != null
                ? order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String orderNumber = String.format("HD-%s-%05d", orderDate, order.getOrderId());

        String orderDateIso = order.getOrderDate() != null ? order.getOrderDate().toString() : null;

        return BatchOrderResponse.builder()
                .isBatched(false)
                .orderId(order.getOrderId())
                .customerName(customerName)
                .orderNumber(orderNumber)
                .shippingMethod(order.getShippingMethod())
                .status(order.getOrderStatus().toString())
                .dueDate(formatDueDate(order.getOrderDate()))
                .department(primaryDepartment)
                .totalItems(totalItems)
                .remainingItems(remainingItems)
                .orderDateIso(orderDateIso)
                .build();
    }

    private String formatDueDate(LocalDateTime orderDate) {
        if (orderDate == null) {
            return "Due soon";
        }

        LocalDateTime dueDate = orderDate.plusDays(1);
        long hoursUntilDue = java.time.Duration.between(LocalDateTime.now(), dueDate).toHours();

        if (hoursUntilDue < 24) {
            return "Due in " + hoursUntilDue + " hour" + (hoursUntilDue != 1 ? "s" : "");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE h:mma");
            return dueDate.format(formatter);
        }
    }

    // Helper classes
    private static class OrderItemWithCustomer {
        Order order;
        OrderItem item;

        OrderItemWithCustomer(Order order, OrderItem item) {
            this.order = order;
            this.item = item;
        }
    }

    private static class OrderItemKey {
        Long orderId;
        Long productId;

        OrderItemKey(Long orderId, Long productId) {
            this.orderId = orderId;
            this.productId = productId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrderItemKey that = (OrderItemKey) o;
            return Objects.equals(orderId, that.orderId) && Objects.equals(productId, that.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(orderId, productId);
        }
    }
}
