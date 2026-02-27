package com.homedepot.fulfillment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for low stock alerts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LowStockAlertResponse {

    private Integer totalLowStockItems;
    private List<LowStockItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LowStockItem {
        private Long inventoryId;
        private Long productId;
        private String productSku;
        private String productName;
        private Long warehouseId;
        private String warehouseName;
        private Integer currentQuantity;
        private Integer minimumStockLevel;
        private Integer shortfall;
    }
}
