package com.homedepot.fulfillment.repository;

import com.homedepot.fulfillment.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity operations.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find product by SKU.
     */
    Optional<Product> findBySku(String sku);

    /**
     * Search products by name containing the given keyword (case-insensitive).
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Find products by category.
     */
    List<Product> findByCategory(String category);

    /**
     * Find products with stock quantity greater than zero.
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0")
    List<Product> findInStockProducts();

    /**
     * Check if product exists by SKU.
     */
    boolean existsBySku(String sku);
}
