package com.homedepot.fulfillment.repository;

import com.homedepot.fulfillment.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Customer entity operations.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customer by email.
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Check if customer exists by email.
     */
    boolean existsByEmail(String email);
}
