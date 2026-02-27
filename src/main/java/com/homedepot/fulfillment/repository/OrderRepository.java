package com.homedepot.fulfillment.repository;

import com.homedepot.fulfillment.entity.Customer;
import com.homedepot.fulfillment.entity.Order;
import com.homedepot.fulfillment.entity.OrderStatus;
import com.homedepot.fulfillment.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Order entity operations.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find orders by customer.
     */
    List<Order> findByCustomer(Customer customer);

    /**
     * Find orders by customer ID ordered by date descending.
     */
    List<Order> findByCustomerCustomerIdOrderByOrderDateDesc(Long customerId);

    /**
     * Find orders by status.
     */
    List<Order> findByOrderStatus(OrderStatus status);

    /**
     * Find orders by warehouse.
     */
    List<Order> findByWarehouse(Warehouse warehouse);

    /**
     * Find pending orders for a specific warehouse (for pick list generation).
     */
    @Query("SELECT o FROM Order o WHERE o.warehouse.warehouseId = :warehouseId " +
           "AND o.orderStatus IN ('PENDING', 'PROCESSING')")
    List<Order> findPendingOrdersByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * Find orders within a date range.
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findOrdersByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Count orders by status.
     */
    Long countByOrderStatus(OrderStatus status);
}
