package com.homedepot.fulfillment.controller;

import com.homedepot.fulfillment.entity.Customer;
import com.homedepot.fulfillment.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Customer management.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management APIs")
public class CustomerController {

    private final CustomerRepository customerRepository;

    @Operation(summary = "Get all customers")
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerRepository.findAll());
    }

    @Operation(summary = "Get customer by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found")));
    }

    @Operation(summary = "Create new customer")
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody @NonNull Customer customer) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(customerRepository.save(customer));
    }
}
