package com.homedepot.fulfillment.repository;

import com.homedepot.fulfillment.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Warehouse entity operations.
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    /**
     * Find warehouse by name.
     */
    Optional<Warehouse> findByName(String name);

    /**
     * Find warehouses by state.
     */
    List<Warehouse> findByState(String state);

    /**
     * Find warehouses with available capacity.
     */
    @Query("SELECT w FROM Warehouse w WHERE w.currentUtilization < w.capacity")
    List<Warehouse> findWarehousesWithCapacity();

    /**
     * Find warehouse by zip code (for proximity-based order assignment).
     */
    Optional<Warehouse> findByZipCode(String zipCode);
}
