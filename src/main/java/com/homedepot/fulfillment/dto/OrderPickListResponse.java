package com.homedepot.fulfillment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO describing a single order's pick list for the warehouse picking screen.
 * Mapped inside the service transaction so no lazy Hibernate proxies are serialized.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPickListResponse {

    private Long orderId;
    private String orderNumber;
    private String customerName;
    private String status;
    private String shippingMethod;
    private int totalItems;
    private List<PickLine> lines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PickLine {
        private Long productId;
        private String sku;
        private String productName;
        private String department;
        private String location;
        private int quantity;
    }
}
