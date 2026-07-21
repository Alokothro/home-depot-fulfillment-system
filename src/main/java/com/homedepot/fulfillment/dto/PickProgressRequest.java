package com.homedepot.fulfillment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body for saving per-item pick progress on an order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickProgressRequest {

    private List<ItemUpdate> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemUpdate {
        private Long productId;
        private int pickedQuantity;
    }
}
