package com.homedepot.fulfillment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for warehouse pick list generation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickListResponse {

    private Long warehouseId;
    private String warehouseName;
    private LocalDateTime generatedAt;
    private Integer totalOrders;
    private List<PickListItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PickListItem {
        private Long orderId;
        private Long productId;
        private String productSku;
        private String productName;
        private String warehouseLocation;
        private Integer quantityToPick;
        private String customerName;
    }
}
