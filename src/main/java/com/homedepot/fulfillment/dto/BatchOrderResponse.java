package com.homedepot.fulfillment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for batched orders.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchOrderResponse {

    private String batchId; // e.g., "GARDEN-BATCH-1"
    private String department; // e.g., "Garden Center"
    private int customerCount; // Number of customers in this batch
    private int totalItems; // Total items across all customers
    private boolean isBatched; // true if batched, false if individual order

    // For batched orders
    private List<CustomerBatchItem> customers;

    // For individual orders (when not batched)
    private Long orderId;
    private String customerName;
    private String orderNumber;
    private String shippingMethod;
    private String status;
    private String dueDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerBatchItem {
        private Long customerId;
        private String customerName;
        private Long orderId;
        private List<BatchItem> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchItem {
        private Long productId;
        private String productName;
        private String sku;
        private int quantity;
        private String location; // warehouse location like "E-10-1"
    }
}
