package com.homedepot.fulfillment.controller;

import com.homedepot.fulfillment.dto.BatchOrderResponse;
import com.homedepot.fulfillment.dto.OrderPickListResponse;
import com.homedepot.fulfillment.dto.PickListResponse;
import com.homedepot.fulfillment.dto.PickProgressRequest;
import com.homedepot.fulfillment.dto.ShipmentRequest;
import com.homedepot.fulfillment.entity.Shipment;
import com.homedepot.fulfillment.service.BatchOrderService;
import com.homedepot.fulfillment.service.FulfillmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Fulfillment operations.
 */
@RestController
@RequestMapping("/api/fulfillment")
@RequiredArgsConstructor
@Tag(name = "Fulfillment", description = "Order fulfillment workflow APIs")
public class FulfillmentController {

    private final FulfillmentService fulfillmentService;
    private final BatchOrderService batchOrderService;

    @Operation(summary = "Get batched orders for picking", description = "Returns orders grouped by department when 4+ customers need items from same department")
    @GetMapping("/batched-orders")
    public ResponseEntity<List<BatchOrderResponse>> getBatchedOrders() {
        return ResponseEntity.ok(batchOrderService.getBatchedOrders());
    }

    @Operation(summary = "Generate pick list for warehouse")
    @GetMapping("/pick-list/{warehouseId}")
    public ResponseEntity<PickListResponse> generatePickList(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(fulfillmentService.generatePickList(warehouseId));
    }

    @Operation(summary = "Get pick list for a single order")
    @GetMapping("/order/{orderId}/pick-list")
    public ResponseEntity<OrderPickListResponse> getOrderPickList(@PathVariable Long orderId) {
        return ResponseEntity.ok(fulfillmentService.getOrderPickList(orderId));
    }

    @Operation(summary = "Save per-item pick progress for an order")
    @PutMapping("/order/{orderId}/pick-progress")
    public ResponseEntity<Void> savePickProgress(@PathVariable Long orderId,
                                                  @RequestBody PickProgressRequest request) {
        fulfillmentService.savePickProgress(orderId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Mark order as partially fulfilled (shortage)")
    @PutMapping("/partial/{orderId}")
    public ResponseEntity<Void> markPartial(@PathVariable Long orderId) {
        fulfillmentService.markPartial(orderId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Mark order as packed")
    @PutMapping("/pack/{orderId}")
    public ResponseEntity<Void> packOrder(@PathVariable Long orderId) {
        fulfillmentService.packOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Ship order with tracking information")
    @PutMapping("/ship/{orderId}")
    public ResponseEntity<Shipment> shipOrder(@Valid @RequestBody ShipmentRequest request) {
        return ResponseEntity.ok(fulfillmentService.shipOrder(request));
    }

    @Operation(summary = "Confirm delivery of order")
    @PutMapping("/deliver/{orderId}")
    public ResponseEntity<Void> confirmDelivery(@PathVariable Long orderId) {
        fulfillmentService.confirmDelivery(orderId);
        return ResponseEntity.ok().build();
    }
}
