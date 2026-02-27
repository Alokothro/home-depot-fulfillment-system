package com.homedepot.fulfillment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for product availability across warehouses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAvailabilityResponse {

    private Long productId;
    private String sku;
    private String name;
    private Integer totalQuantity;
    private List<WarehouseStock> warehouseStocks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WarehouseStock {
        private Long warehouseId;
        private String warehouseName;
        private String warehouseCity;
        private String warehouseState;
        private Integer quantity;
        private boolean lowStock;
    }
}
