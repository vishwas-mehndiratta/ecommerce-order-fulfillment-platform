package com.ecommerce.ecommerce_platform.customer.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.ecommerce_platform.customer.dto.CustomerRequest;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerResponse;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerStatusRequest;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerUpdateRequest;
import com.ecommerce.ecommerce_platform.customer.entity.Customer;
import com.ecommerce.ecommerce_platform.customer.entity.CustomerStatus;
import com.ecommerce.ecommerce_platform.customer.exception.CustomerNotFoundException;
import com.ecommerce.ecommerce_platform.customer.exception.DuplicateResourceException;
import com.ecommerce.ecommerce_platform.customer.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_platform.customer.repository.CustomerRepository;
import com.ecommerce.ecommerce_platform.customer.service.CustomerService;
import com.ecommerce.ecommerce_platform.customer.specification.CustomerSpecification;

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

		log.info("Creating new customer");

		//String email = request.getEmail().trim().toLowerCase();

		if (customerRepository.existsByEmailIgnoreCase(request.getEmail())) {
			log.warn("Customer creation failed. Email already exists");
			throw new DuplicateResourceException("Customer with email already exists");
		}

		String customerNumber = customerNumberGenerator.generate();
		log.debug("Generated customer number: {}", customerNumber);

		Customer customer = new Customer();

		customer.setCustomerNumber(customerNumber);
		customer.setFirstName(request.getFirstName());
		customer.setLastName(request.getLastName());
		customer.setEmail(request.getEmail());
		customer.setPhone(request.getPhone());
		customer.setStatus(CustomerStatus.ACTIVE);

		Customer savedCustomer = customerRepository.save(customer);
		log.info("Customer created successfully. customerNumber={}", savedCustomer.getCustomerNumber());

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
	public Page<CustomerResponse> getCustomers(String firstName, String lastName, String email, String customerNumber,
			CustomerStatus status, Pageable pageable) {

		Specification<Customer> specification = null;

		if (firstName != null && !firstName.isBlank()) {
			specification = addSpecification(specification, CustomerSpecification.hasFirstName(firstName));
		}

		if (lastName != null && !lastName.isBlank()) {
			specification = addSpecification(specification, CustomerSpecification.hasLastName(lastName));
		}

		if (email != null && !email.isBlank()) {
			specification = addSpecification(specification, CustomerSpecification.hasEmail(email));
		}

		if (customerNumber != null && !customerNumber.isBlank()) {
			specification = addSpecification(specification, CustomerSpecification.hasCustomerNumber(customerNumber));
		}

		if (status != null) {
			specification = addSpecification(specification, CustomerSpecification.hasStatus(status));
		}

		return customerRepository.findAll(specification, pageable).map(this::mapToResponse);
	}

	private Specification<Customer> addSpecification(Specification<Customer> current,
			Specification<Customer> newSpecification) {

		if (current == null) {
			return newSpecification;
		}

		return current.and(newSpecification);
	}

	@Override
	public CustomerResponse getCustomerByCustomerNumber(String customerNumber) {
		log.info("Fetching customer. customerNumber={}", customerNumber);

		Customer customer = customerRepository.findByCustomerNumber(customerNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerNumber));
		log.debug("Customer found. customerNumber={}", customerNumber);

		return mapToResponse(customer);
	}

	@Override
	@Transactional
	public CustomerResponse updateCustomer(String customerNumber, CustomerUpdateRequest request) {

		log.info("Updating customer. customerNumber={}", customerNumber);

		Customer customer = customerRepository.findByCustomerNumber(customerNumber).orElseThrow(() -> {
			log.warn("Customer update failed. Customer not found. customerNumber={}", customerNumber);
			return new ResourceNotFoundException("Customer not found: " + customerNumber);
		});

		String email = request.getEmail().trim().toLowerCase();

		if (customerRepository.existsByEmailAndCustomerNumberNot(email, customerNumber)) {
			log.warn("Customer update failed. Email already associated with another customer. customerNumber={}",
					customerNumber);
			throw new DuplicateResourceException("Another customer already exists with email: " + email);
		}

		customer.setFirstName(request.getFirstName().trim());
		customer.setLastName(request.getLastName().trim());
		customer.setEmail(email);
		customer.setPhone(request.getPhone());

		Customer updatedCustomer = customerRepository.save(customer);
		log.info("Customer updated successfully. customerNumber={}", updatedCustomer.getCustomerNumber());

		return mapToResponse(updatedCustomer);
	}

	@Override
	public CustomerResponse updateCustomerStatus(String customerNumber, CustomerStatusRequest request) {

		log.info("Updating status for customerNumber: {}", customerNumber);

		Customer customer = customerRepository.findByCustomerNumber(customerNumber)
				.orElseThrow(() -> new CustomerNotFoundException(customerNumber));

		customer.setStatus(request.status());

		Customer savedCustomer = customerRepository.save(customer);

		log.info("Customer status updated successfully. customerNumber={}, status={}", customerNumber,
				request.status());

		return mapToResponse(savedCustomer);
	}

	@Override
	public void deleteCustomer(String customerNumber) {

		log.info("Soft deleting customer: {}", customerNumber);

		Customer customer = customerRepository.findByCustomerNumber(customerNumber)
				.orElseThrow(() -> new CustomerNotFoundException(customerNumber));

		customer.setStatus(CustomerStatus.DELETED);

		customerRepository.save(customer);

		log.info("Customer soft deleted successfully: {}", customerNumber);
	}

}
