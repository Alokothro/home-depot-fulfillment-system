package com.homedepot.fulfillment.service;

import com.homedepot.fulfillment.dto.ProductAvailabilityResponse;
import com.homedepot.fulfillment.entity.Inventory;
import com.homedepot.fulfillment.entity.Product;
import com.homedepot.fulfillment.exception.ProductNotFoundException;
import com.homedepot.fulfillment.repository.InventoryRepository;
import com.homedepot.fulfillment.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Product operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Get all products.
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        logger.debug("Fetching all products");
        return productRepository.findAll();
    }

    /**
     * Get product by ID.
     */
    @Transactional(readOnly = true)
    public Product getProductById(@NonNull Long id) {
        logger.debug("Fetching product with ID: {}", id);
        return productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /**
     * Get product by SKU.
     */
    @Transactional(readOnly = true)
    public Product getProductBySku(@NonNull String sku) {
        logger.debug("Fetching product with SKU: {}", sku);
        return productRepository.findBySku(sku)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
    }

    /**
     * Search products by name.
     */
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(@NonNull String name) {
        logger.debug("Searching products with name containing: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Get products by category.
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(@NonNull String category) {
        logger.debug("Fetching products in category: {}", category);
        return productRepository.findByCategory(category);
    }

    /**
     * Create new product.
     */
    public Product createProduct(@NonNull Product product) {
        logger.info("Creating new product with SKU: {}", product.getSku());

        if (productRepository.existsBySku(product.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + product.getSku() + " already exists");
        }

        Product saved = productRepository.save(product);
        logger.info("Product created successfully with ID: {}", saved.getProductId());
        return saved;
    }

    /**
     * Update existing product.
     */
    public Product updateProduct(@NonNull Long id, @NonNull Product productDetails) {
        logger.info("Updating product with ID: {}", id);

        Product product = getProductById(id);

        // Update fields
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setCategory(productDetails.getCategory());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setWarehouseLocation(productDetails.getWarehouseLocation());
        product.setWeight(productDetails.getWeight());
        product.setDimensions(productDetails.getDimensions());

        Product updated = productRepository.save(product);
        logger.info("Product updated successfully: {}", id);
        return updated;
    }

    /**
     * Delete product.
     */
    public void deleteProduct(@NonNull Long id) {
        logger.info("Deleting product with ID: {}", id);

        Product product = getProductById(id);
        productRepository.delete(product);

        logger.info("Product deleted successfully: {}", id);
    }

    /**
     * Get product availability across all warehouses.
     */
    @Transactional(readOnly = true)
    public ProductAvailabilityResponse getProductAvailability(@NonNull Long productId) {
        logger.debug("Fetching availability for product ID: {}", productId);

        Product product = getProductById(productId);
        List<Inventory> inventories = inventoryRepository.findByProduct(product);

        Integer totalQuantity = inventories.stream()
            .mapToInt(Inventory::getQuantity)
            .sum();

        List<ProductAvailabilityResponse.WarehouseStock> warehouseStocks = inventories.stream()
            .map(inv -> ProductAvailabilityResponse.WarehouseStock.builder()
                .warehouseId(inv.getWarehouse().getWarehouseId())
                .warehouseName(inv.getWarehouse().getName())
                .warehouseCity(inv.getWarehouse().getCity())
                .warehouseState(inv.getWarehouse().getState())
                .quantity(inv.getQuantity())
                .lowStock(inv.isLowStock())
                .build())
            .collect(Collectors.toList());

        return ProductAvailabilityResponse.builder()
            .productId(product.getProductId())
            .sku(product.getSku())
            .name(product.getName())
            .totalQuantity(totalQuantity)
            .warehouseStocks(warehouseStocks)
            .build();
    }
}
