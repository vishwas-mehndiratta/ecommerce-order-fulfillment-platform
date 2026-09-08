package com.ecommerce.ecommerce_platform.customer.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.ecommerce_platform.customer.dto.CustomerRequest;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerResponse;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerUpdateRequest;
import com.ecommerce.ecommerce_platform.customer.entity.Customer;
import com.ecommerce.ecommerce_platform.customer.entity.CustomerStatus;
import com.ecommerce.ecommerce_platform.customer.exception.DuplicateResourceException;
import com.ecommerce.ecommerce_platform.customer.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_platform.customer.repository.CustomerRepository;
import com.ecommerce.ecommerce_platform.customer.service.CustomerService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

	private final CustomerRepository customerRepository;
	private final CustomerNumberGenerator customerNumberGenerator;

	public CustomerServiceImpl(CustomerRepository customerRepository, CustomerNumberGenerator customerNumberGenerator) {
		this.customerRepository = customerRepository;
		this.customerNumberGenerator = customerNumberGenerator;
	}

	@Override
	public CustomerResponse createCustomer(CustomerRequest request) {

		String email = request.getEmail().trim().toLowerCase();

		if (customerRepository.existsByEmail(email)) {
			throw new DuplicateResourceException("Customer with email already exists");
		}

		String customerNumber = customerNumberGenerator.generate();

		Customer customer = new Customer();

		customer.setCustomerNumber(customerNumber);
		customer.setFirstName(request.getFirstName());
		customer.setLastName(request.getLastName());
		customer.setEmail(request.getEmail());
		customer.setPhone(request.getPhone());
		customer.setStatus(CustomerStatus.ACTIVE);

		Customer savedCustomer = customerRepository.save(customer);

		return mapToResponse(savedCustomer);
	}

	private CustomerResponse mapToResponse(Customer customer) {

		CustomerResponse response = new CustomerResponse();

		response.setId(customer.getId());
		response.setCustomerNumber(customer.getCustomerNumber());
		response.setFirstName(customer.getFirstName());
		response.setLastName(customer.getLastName());
		response.setEmail(customer.getEmail());
		response.setPhone(customer.getPhone());
		response.setStatus(customer.getStatus());
		response.setCreatedAt(customer.getCreatedAt());
		response.setUpdatedAt(customer.getUpdatedAt());

		return response;
	}

	@Override
	public List<CustomerResponse> getAllCustomers() {

		return customerRepository.findAll().stream().map(this::mapToResponse).toList();
	}

	@Override
	public CustomerResponse getCustomerByCustomerNumber(String customerNumber) {

		Customer customer = customerRepository.findByCustomerNumber(customerNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerNumber));

		return mapToResponse(customer);
	}

	@Override
	@Transactional
	public CustomerResponse updateCustomer(String customerNumber, CustomerUpdateRequest request) {

		Customer customer = customerRepository.findByCustomerNumber(customerNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerNumber));

		String email = request.getEmail().trim().toLowerCase();

		if (customerRepository.existsByEmailAndCustomerNumberNot(email, customerNumber)) {

			throw new DuplicateResourceException("Another customer already exists with email: " + email);
		}

		customer.setFirstName(request.getFirstName().trim());
		customer.setLastName(request.getLastName().trim());
		customer.setEmail(email);
		customer.setPhone(request.getPhone());

		Customer updatedCustomer = customerRepository.save(customer);

		return mapToResponse(updatedCustomer);
	}

}
