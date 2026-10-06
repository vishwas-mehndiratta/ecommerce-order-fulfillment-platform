package com.ecommerce.ecommerce_platform.customer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ecommerce.ecommerce_platform.customer.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

	Optional<Customer> findByCustomerNumber(String customerNumber);

	Optional<Customer> findByEmail(String email);

	boolean existsByCustomerNumber(String customerNumber);

	boolean existsByEmail(String email);

	boolean existsByEmailAndCustomerNumberNot(String email, String customerNumber);
	
	boolean existsByEmailIgnoreCase(String email);
}