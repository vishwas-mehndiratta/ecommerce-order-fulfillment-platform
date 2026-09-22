package com.ecommerce.ecommerce_platform.customer.specification;

import org.springframework.data.jpa.domain.Specification;

import com.ecommerce.ecommerce_platform.customer.entity.Customer;
import com.ecommerce.ecommerce_platform.customer.entity.CustomerStatus;

public final class CustomerSpecification {

	private CustomerSpecification() {
		// Utility class
	}

	public static Specification<Customer> hasFirstName(String firstName) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("firstName"), firstName);
	}

	public static Specification<Customer> hasLastName(String lastName) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("lastName"), lastName);
	}

	public static Specification<Customer> hasEmail(String email) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("email"), email);
	}

	public static Specification<Customer> hasCustomerNumber(String customerNumber) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("customerNumber"), customerNumber);
	}

	public static Specification<Customer> hasStatus(CustomerStatus status) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
	}

}