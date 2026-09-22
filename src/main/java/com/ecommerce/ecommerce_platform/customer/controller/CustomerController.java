package com.ecommerce.ecommerce_platform.customer.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.ecommerce_platform.common.exception.ErrorResponse;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerRequest;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerResponse;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerStatusRequest;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerUpdateRequest;
import com.ecommerce.ecommerce_platform.customer.entity.CustomerStatus;
import com.ecommerce.ecommerce_platform.customer.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@Operation(summary = "Create a customer", description = "Creates a new customer with an automatically generated customer number")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Customer created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "Customer already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) {
		return customerService.createCustomer(request);
	}

	@GetMapping
	@Operation(summary = "Search customers", description = "Search customers using filters, pagination and sorting")
	public ResponseEntity<Page<CustomerResponse>> getCustomers(@RequestParam(required = false) String firstName,
			@RequestParam(required = false) String lastName, @RequestParam(required = false) String email,
			@RequestParam(required = false) String customerNumber,
			@RequestParam(required = false) CustomerStatus status,
			@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		return ResponseEntity
				.ok(customerService.getCustomers(firstName, lastName, email, customerNumber, status, pageable));
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

	@PatchMapping("/{customerNumber}/status")
	public ResponseEntity<CustomerResponse> updateCustomerStatus(@PathVariable String customerNumber,
			@Valid @RequestBody CustomerStatusRequest request) {

		return ResponseEntity.ok(customerService.updateCustomerStatus(customerNumber, request));
	}

	@DeleteMapping("/{customerNumber}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCustomer(@PathVariable String customerNumber) {

		customerService.deleteCustomer(customerNumber);
	}
}
