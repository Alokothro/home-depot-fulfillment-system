package com.homedepot.fulfillment.controller;

import com.homedepot.fulfillment.dto.PickListResponse;
import com.homedepot.fulfillment.dto.ShipmentRequest;
import com.homedepot.fulfillment.entity.Shipment;
import com.homedepot.fulfillment.service.FulfillmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Fulfillment operations.
 */
@RestController
@RequestMapping("/api/fulfillment")
@RequiredArgsConstructor
@Tag(name = "Fulfillment", description = "Order fulfillment workflow APIs")
public class FulfillmentController {

    private final FulfillmentService fulfillmentService;

    @Operation(summary = "Generate pick list for warehouse")
    @GetMapping("/pick-list/{warehouseId}")
    public ResponseEntity<PickListResponse> generatePickList(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(fulfillmentService.generatePickList(warehouseId));
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
