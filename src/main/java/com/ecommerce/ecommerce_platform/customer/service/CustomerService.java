package com.ecommerce.ecommerce_platform.customer.service;

import java.util.List;

import com.ecommerce.ecommerce_platform.customer.dto.CustomerRequest;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerResponse;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerUpdateRequest;

public interface CustomerService {

	CustomerResponse createCustomer(CustomerRequest request);

	List<CustomerResponse> getAllCustomers();

	CustomerResponse getCustomerByCustomerNumber(String customerNumber);

	CustomerResponse updateCustomer(String customerNumber, CustomerUpdateRequest request);
}