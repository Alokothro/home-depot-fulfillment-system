package com.homedepot.fulfillment.repository;

import com.homedepot.fulfillment.entity.Inventory;
import com.homedepot.fulfillment.entity.Product;
import com.homedepot.fulfillment.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Inventory entity operations.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find inventory by product and warehouse.
     */
    Optional<Inventory> findByProductAndWarehouse(Product product, Warehouse warehouse);

    /**
     * Find all inventory for a specific product across all warehouses.
     */
    List<Inventory> findByProduct(Product product);

    /**
     * Find all inventory for a specific warehouse.
     */
    List<Inventory> findByWarehouse(Warehouse warehouse);

    /**
     * Find low stock items (quantity below minimum threshold).
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity < i.minimumStockLevel")
    List<Inventory> findLowStockItems();

    /**
     * Find warehouses that have a specific product in stock with sufficient quantity.
     */
    @Query("SELECT i FROM Inventory i WHERE i.product.productId = :productId AND i.quantity >= :requiredQuantity")
    List<Inventory> findWarehousesWithProductInStock(
        @Param("productId") Long productId,
        @Param("requiredQuantity") Integer requiredQuantity
    );

    /**
     * Check total quantity across all warehouses for a product.
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.product.productId = :productId")
    Integer getTotalQuantityForProduct(@Param("productId") Long productId);
}
