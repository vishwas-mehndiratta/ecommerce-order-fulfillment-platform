package com.ecommerce.ecommerce_platform.customer.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.ecommerce_platform.customer.dto.CustomerRequest;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerResponse;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerStatusRequest;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerUpdateRequest;
import com.ecommerce.ecommerce_platform.customer.entity.CustomerStatus;

public interface CustomerService {

	CustomerResponse createCustomer(CustomerRequest request);

	Page<CustomerResponse> getCustomers(String firstName, String lastName, String email, String customerNumber,
			CustomerStatus status, Pageable pageable);

	CustomerResponse getCustomerByCustomerNumber(String customerNumber);

	CustomerResponse updateCustomer(String customerNumber, CustomerUpdateRequest request);

	CustomerResponse updateCustomerStatus(String customerNumber, CustomerStatusRequest request);

	void deleteCustomer(String customerNumber);
}