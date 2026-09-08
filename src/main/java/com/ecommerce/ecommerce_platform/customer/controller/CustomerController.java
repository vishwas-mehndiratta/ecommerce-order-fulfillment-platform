package com.ecommerce.ecommerce_platform.customer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.ecommerce_platform.customer.dto.CustomerRequest;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerResponse;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerUpdateRequest;
import com.ecommerce.ecommerce_platform.customer.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) {

		return customerService.createCustomer(request);
	}

	@GetMapping
	public ResponseEntity<List<CustomerResponse>> getAllCustomers() {

		return ResponseEntity.ok(customerService.getAllCustomers());
	}

	@GetMapping("/{customerNumber}")
	public ResponseEntity<CustomerResponse> getCustomer(@PathVariable String customerNumber) {

		return ResponseEntity.ok(customerService.getCustomerByCustomerNumber(customerNumber));
	}

	@PutMapping("/{customerNumber}")
	public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable String customerNumber,
			@Valid @RequestBody CustomerUpdateRequest request) {

		return ResponseEntity.ok(customerService.updateCustomer(customerNumber, request));
	}
}
