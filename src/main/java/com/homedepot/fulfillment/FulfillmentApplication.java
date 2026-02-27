package com.homedepot.fulfillment;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Home Depot Order Fulfillment System.
 */
@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Home Depot Order Fulfillment System API",
        version = "1.0.0",
        description = "Comprehensive order fulfillment system for managing products, inventory, orders, and warehouse operations",
        contact = @Contact(
            name = "Home Depot Engineering",
            email = "support@homedepot.com"
        )
    )
)
public class FulfillmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(FulfillmentApplication.class, args);
    }
}
