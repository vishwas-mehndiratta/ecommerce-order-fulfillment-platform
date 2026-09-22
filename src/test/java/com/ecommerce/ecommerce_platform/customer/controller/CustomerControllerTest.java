package com.ecommerce.ecommerce_platform.customer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.ecommerce_platform.customer.dto.CustomerRequest;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerResponse;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerStatusRequest;
import com.ecommerce.ecommerce_platform.customer.dto.CustomerUpdateRequest;
import com.ecommerce.ecommerce_platform.customer.entity.CustomerStatus;
import com.ecommerce.ecommerce_platform.customer.exception.CustomerNotFoundException;
import com.ecommerce.ecommerce_platform.customer.exception.DuplicateResourceException;
import com.ecommerce.ecommerce_platform.customer.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_platform.customer.service.CustomerService;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CustomerController.class)
public class CustomerControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private ObjectMapper objectMapper = new ObjectMapper();

	@MockitoBean
	private CustomerService customerService;

	@Test
	void shouldCreateCustomerSuccessfully() throws Exception {

		// Arrange
		CustomerRequest request = CustomerRequest.builder().firstName("Ravi").lastName("Bhatt")
				.email("ravi@example.com").phone("7897897890").build();

		CustomerResponse response = new CustomerResponse(1L, "CUST-000001", "Ravi", "Bhatt", "ravi@example.com",
				"7897897890", CustomerStatus.ACTIVE, null, null);

		when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(response);

		// Act & Assert
		mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.customerNumber").value("CUST-000001"))
				.andExpect(jsonPath("$.firstName").value("Ravi")).andExpect(jsonPath("$.lastName").value("Bhatt"))
				.andExpect(jsonPath("$.email").value("ravi@example.com"))
				.andExpect(jsonPath("$.phone").value("7897897890")).andExpect(jsonPath("$.status").value("ACTIVE"));

		verify(customerService, times(1)).createCustomer(any(CustomerRequest.class));
	}

	@Test
	void shouldFailToCreateCustomerWithInvalidRequest() throws Exception {
		// Arrange
		CustomerRequest request = CustomerRequest.builder().firstName("").lastName("Bhatt").email("invalid-email")
				.phone("123456789012345678901").build();

		// Act & Assert
		mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

		verify(customerService, never()).createCustomer(any(CustomerRequest.class));
	}

	@Test
	void shouldFailToCreateCustomerWithDuplicateEmail() throws Exception {
		// Arrange
		CustomerRequest request = CustomerRequest.builder().firstName("Ravi").lastName("Bhatt")
				.email("ravi@example.com").phone("7897897890").build();

		when(customerService.createCustomer(any(CustomerRequest.class)))
				.thenThrow(new DuplicateResourceException("Customer with email already exists"));

		// Act & Assert
		mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isConflict());

		verify(customerService, times(1)).createCustomer(any(CustomerRequest.class));
	}

	@Test
	void shouldGetCustomerSuccessfully() throws Exception {
		// Arrange
		CustomerResponse response = new CustomerResponse(1L, "CUST-000001", "Ravi", "Bhatt", "ravi@example.com",
				"7897897890", CustomerStatus.ACTIVE, null, null);

		when(customerService.getCustomerByCustomerNumber("CUST-000001")).thenReturn(response);

		// Act & Assert
		mockMvc.perform(get("/api/v1/customers/CUST-000001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.customerNumber").value("CUST-000001"))
				.andExpect(jsonPath("$.firstName").value("Ravi")).andExpect(jsonPath("$.lastName").value("Bhatt"));

		verify(customerService, times(1)).getCustomerByCustomerNumber("CUST-000001");
	}

	@Test
	void shouldGetCustomersSuccessfully() throws Exception {
		// Arrange
		CustomerResponse response = new CustomerResponse(1L, "CUST-000001", "Ravi", "Bhatt", "ravi@example.com",
				"7897897890", CustomerStatus.ACTIVE, null, null);

		when(customerService.getCustomers(eq("Ravi"), isNull(), isNull(), isNull(), eq(CustomerStatus.ACTIVE),
				any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of(response),
						PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "firstName")), 1));

		// Act & Assert
		mockMvc.perform(get("/api/v1/customers").param("firstName", "Ravi").param("status", "ACTIVE")
				.param("page", "0").param("size", "2").param("sort", "firstName,asc")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.content[0].customerNumber").value("CUST-000001"))
				.andExpect(jsonPath("$.content[0].firstName").value("Ravi"))
				.andExpect(jsonPath("$.totalElements").value(1));

		verify(customerService, times(1)).getCustomers(eq("Ravi"), isNull(), isNull(), isNull(), eq(CustomerStatus.ACTIVE),
				argThat(pageable -> pageable != null && pageable.getPageNumber() == 0 && pageable.getPageSize() == 2
						&& pageable.getSort().getOrderFor("firstName") != null
						&& pageable.getSort().getOrderFor("firstName").isAscending()));
	}

	@Test
	void shouldReturnEmptyPageWhenNoCustomersMatch() throws Exception {
		// Arrange
		when(customerService.getCustomers(eq("Missing"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(java.util.List.of(), PageRequest.of(0, 20), 0));

		// Act & Assert
		mockMvc.perform(get("/api/v1/customers").param("firstName", "Missing").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.content").isEmpty())
				.andExpect(jsonPath("$.totalElements").value(0));

		verify(customerService, times(1)).getCustomers(eq("Missing"), isNull(), isNull(), isNull(), isNull(),
				argThat(pageable -> pageable != null && pageable.getPageNumber() == 0 && pageable.getPageSize() == 20
						&& pageable.getSort().getOrderFor("createdAt") != null
						&& pageable.getSort().getOrderFor("createdAt").isDescending()));
	}

	@Test
	void shouldFailToGetNonExistentCustomer() throws Exception {
		// Arrange
		when(customerService.getCustomerByCustomerNumber("INVALID-NUMBER"))
				.thenThrow(new ResourceNotFoundException("Customer not found: INVALID-NUMBER"));

		// Act & Assert
		mockMvc.perform(get("/api/v1/customers/INVALID-NUMBER").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

		verify(customerService, times(1)).getCustomerByCustomerNumber("INVALID-NUMBER");
	}

	@Test
	void shouldUpdateCustomerSuccessfully() throws Exception {
		// Arrange
		CustomerUpdateRequest request = new CustomerUpdateRequest();
		request.setFirstName("Ravindra");
		request.setLastName("Bhattacharya");
		request.setEmail("ravindra@example.com");
		request.setPhone("9876543210");

		CustomerResponse response = new CustomerResponse(1L, "CUST-000001", "Ravindra", "Bhattacharya",
				"ravindra@example.com", "9876543210", CustomerStatus.ACTIVE, null, null);

		when(customerService.updateCustomer(eq("CUST-000001"), any(CustomerUpdateRequest.class))).thenReturn(response);

		// Act & Assert
		mockMvc.perform(put("/api/v1/customers/CUST-000001").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.firstName").value("Ravindra"))
				.andExpect(jsonPath("$.lastName").value("Bhattacharya"));

		verify(customerService, times(1)).updateCustomer(eq("CUST-000001"),
				argThat(customerRequest -> customerRequest != null
						&& "Ravindra".equals(customerRequest.getFirstName())
						&& "Bhattacharya".equals(customerRequest.getLastName())
						&& "ravindra@example.com".equals(customerRequest.getEmail())
						&& "9876543210".equals(customerRequest.getPhone())));
	}

	@Test
	void shouldFailToUpdateNonExistentCustomer() throws Exception {
		// Arrange
		CustomerUpdateRequest request = new CustomerUpdateRequest();
		request.setFirstName("Ravindra");
		request.setLastName("Bhattacharya");
		request.setEmail("ravindra@example.com");
		request.setPhone("9876543210");

		when(customerService.updateCustomer(eq("INVALID-NUMBER"), any(CustomerUpdateRequest.class)))
				.thenThrow(new ResourceNotFoundException("Customer not found: INVALID-NUMBER"));

		// Act & Assert
		mockMvc.perform(put("/api/v1/customers/INVALID-NUMBER").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isNotFound());

		verify(customerService, times(1)).updateCustomer(eq("INVALID-NUMBER"),
				argThat(customerRequest -> customerRequest != null
						&& "Ravindra".equals(customerRequest.getFirstName())
						&& "Bhattacharya".equals(customerRequest.getLastName())
						&& "ravindra@example.com".equals(customerRequest.getEmail())
						&& "9876543210".equals(customerRequest.getPhone())));
	}

	@Test
	void shouldFailToUpdateCustomerWithDuplicateEmail() throws Exception {
		// Arrange
		CustomerUpdateRequest request = new CustomerUpdateRequest();
		request.setFirstName("Ravindra");
		request.setLastName("Bhattacharya");
		request.setEmail("duplicate@example.com");
		request.setPhone("9876543210");

		when(customerService.updateCustomer(eq("CUST-000001"), any(CustomerUpdateRequest.class)))
				.thenThrow(new DuplicateResourceException("Another customer already exists with email: duplicate@example.com"));

		// Act & Assert
		mockMvc.perform(put("/api/v1/customers/CUST-000001").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.error").value("DUPLICATE_RESOURCE"));

		verify(customerService, times(1)).updateCustomer(eq("CUST-000001"),
				argThat(customerRequest -> customerRequest != null
						&& "duplicate@example.com".equals(customerRequest.getEmail())));
	}

	@Test
	void shouldFailToUpdateCustomerWithInvalidRequest() throws Exception {
		// Arrange
		CustomerUpdateRequest request = new CustomerUpdateRequest();
		request.setFirstName("");
		request.setLastName("Bhattacharya");
		request.setEmail("invalid-email");
		request.setPhone("123456789012345678901");

		// Act & Assert
		mockMvc.perform(put("/api/v1/customers/CUST-000001").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

		verify(customerService, never()).updateCustomer(eq("CUST-000001"), any(CustomerUpdateRequest.class));
	}

	@Test
	void shouldUpdateCustomerStatusSuccessfully() throws Exception {
		// Arrange
		CustomerStatusRequest request = new CustomerStatusRequest(CustomerStatus.INACTIVE);

		CustomerResponse response = new CustomerResponse(1L, "CUST-000001", "Ravi", "Bhatt", "ravi@example.com",
				"7897897890", CustomerStatus.INACTIVE, null, null);

		when(customerService.updateCustomerStatus("CUST-000001", request)).thenReturn(response);

		// Act & Assert
		mockMvc.perform(patch("/api/v1/customers/CUST-000001/status").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.status").value("INACTIVE"));

		verify(customerService, times(1)).updateCustomerStatus("CUST-000001", request);
	}

	@Test
	void shouldFailToUpdateStatusOfNonExistentCustomer() throws Exception {
		// Arrange
		CustomerStatusRequest request = new CustomerStatusRequest(CustomerStatus.INACTIVE);

		when(customerService.updateCustomerStatus("INVALID-NUMBER", request))
				.thenThrow(new CustomerNotFoundException("INVALID-NUMBER"));

		// Act & Assert
		mockMvc.perform(patch("/api/v1/customers/INVALID-NUMBER/status").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isNotFound());

		verify(customerService, times(1)).updateCustomerStatus("INVALID-NUMBER", request);
	}

	@Test
	void shouldFailToUpdateCustomerStatusWithInvalidRequest() throws Exception {
		// Act & Assert
		mockMvc.perform(patch("/api/v1/customers/CUST-000001/status").contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

		verify(customerService, never()).updateCustomerStatus(eq("CUST-000001"), any(CustomerStatusRequest.class));
	}

	@Test
	void shouldDeleteCustomerSuccessfully() throws Exception {
		// Arrange
		doNothing().when(customerService).deleteCustomer("CUST-000001");

		// Act & Assert
		mockMvc.perform(delete("/api/v1/customers/CUST-000001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());

		verify(customerService, times(1)).deleteCustomer("CUST-000001");
	}

	@Test
	void shouldFailToDeleteNonExistentCustomer() throws Exception {
		// Arrange
		doThrow(new CustomerNotFoundException("INVALID-NUMBER")).when(customerService).deleteCustomer("INVALID-NUMBER");

		// Act & Assert
		mockMvc.perform(delete("/api/v1/customers/INVALID-NUMBER").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

		verify(customerService, times(1)).deleteCustomer("INVALID-NUMBER");
	}

}
