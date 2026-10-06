package com.ecommerce.ecommerce_platform.customer.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private CustomerNumberGenerator customerNumberGenerator;

	@InjectMocks
	private CustomerServiceImpl customerService;

	@Captor
	private ArgumentCaptor<Customer> customerCaptor;

	private CustomerRequest customerRequest;
	private CustomerUpdateRequest customerUpdateRequest;
	private Customer customer;
	private Pageable pageable;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@BeforeEach
	void setUp() {
		createdAt = LocalDateTime.of(2026, 9, 23, 10, 15);
		updatedAt = LocalDateTime.of(2026, 9, 23, 12, 45);
		pageable = PageRequest.of(0, 10);

		customerRequest = CustomerRequest.builder().firstName("Ravi").lastName("Bhatt")
				.email("Ravi@Example.com ").phone("9876543210").build();

		customerUpdateRequest = new CustomerUpdateRequest();
		customerUpdateRequest.setFirstName("  Ravindra  ");
		customerUpdateRequest.setLastName("  Bhattacharya  ");
		customerUpdateRequest.setEmail("  Updated@Example.com  ");
		customerUpdateRequest.setPhone("9999999999");

		customer = buildCustomer("CUST-000001", "Ravi", "Bhatt", "ravi@example.com", CustomerStatus.ACTIVE);
	}

	@Test
	void shouldCreateCustomerSuccessfully() {
		when(customerRepository.existsByEmail("ravi@example.com")).thenReturn(false);
		when(customerNumberGenerator.generate()).thenReturn("CUST-000001");
		when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
			Customer savedCustomer = invocation.getArgument(0);
			savedCustomer.setId(1L);
			savedCustomer.setCreatedAt(createdAt);
			savedCustomer.setUpdatedAt(updatedAt);
			return savedCustomer;
		});

		CustomerResponse response = customerService.createCustomer(customerRequest);

		verify(customerRepository).save(customerCaptor.capture());
		Customer savedCustomer = customerCaptor.getValue();
		assertThat(savedCustomer.getCustomerNumber()).isEqualTo("CUST-000001");
		assertThat(savedCustomer.getFirstName()).isEqualTo("Ravi");
		assertThat(savedCustomer.getLastName()).isEqualTo("Bhatt");
		assertThat(savedCustomer.getEmail()).isEqualTo("Ravi@Example.com ");
		assertThat(savedCustomer.getPhone()).isEqualTo("9876543210");
		assertThat(savedCustomer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getCustomerNumber()).isEqualTo("CUST-000001");
		assertThat(response.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
		assertThat(response.getCreatedAt()).isEqualTo(createdAt);
		assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
	}

	@Test
	void shouldThrowExceptionWhenCreatingCustomerWithDuplicateEmail() {
		when(customerRepository.existsByEmail("ravi@example.com")).thenReturn(true);

		assertThatThrownBy(() -> customerService.createCustomer(customerRequest))
				.isInstanceOf(DuplicateResourceException.class)
				.hasMessage("Customer with email already exists");

		verify(customerNumberGenerator, never()).generate();
		verify(customerRepository, never()).save(any(Customer.class));
	}

	@Test
	void shouldGetCustomersWithoutFilters() {
		Page<Customer> customerPage = new PageImpl<>(List.of(customer), pageable, 1);
		when(customerRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Customer>>isNull(), eq(pageable)))
				.thenReturn(customerPage);

		Page<CustomerResponse> responsePage = customerService.getCustomers(null, null, null, null, null, pageable);

		assertThat(responsePage.getTotalElements()).isEqualTo(1);
		assertThat(responsePage.getContent()).hasSize(1);
		assertThat(responsePage.getContent().getFirst().getCustomerNumber()).isEqualTo("CUST-000001");
		verify(customerRepository).findAll(org.mockito.ArgumentMatchers.<Specification<Customer>>isNull(), eq(pageable));
	}

	@Test
	void shouldGetCustomersWithFilters() {
		Page<Customer> customerPage = new PageImpl<>(List.of(customer), pageable, 1);
		when(customerRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Customer>>any(), eq(pageable)))
				.thenReturn(customerPage);

		Page<CustomerResponse> responsePage = customerService.getCustomers("Ravi", "Bhatt", "ravi@example.com",
				"CUST-000001", CustomerStatus.ACTIVE, pageable);

		assertThat(responsePage.getContent()).hasSize(1);
		assertThat(responsePage.getContent().getFirst().getEmail()).isEqualTo("ravi@example.com");
		verify(customerRepository).findAll(org.mockito.ArgumentMatchers.<Specification<Customer>>any(), eq(pageable));
	}

	@Test
	void shouldGetCustomerByCustomerNumberSuccessfully() {
		when(customerRepository.findByCustomerNumber("CUST-000001")).thenReturn(Optional.of(customer));

		CustomerResponse response = customerService.getCustomerByCustomerNumber("CUST-000001");

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getCustomerNumber()).isEqualTo("CUST-000001");
		assertThat(response.getFirstName()).isEqualTo("Ravi");
	}

	@Test
	void shouldThrowExceptionWhenCustomerNumberDoesNotExist() {
		when(customerRepository.findByCustomerNumber("CUST-999999")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> customerService.getCustomerByCustomerNumber("CUST-999999"))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Customer not found: CUST-999999");
	}

	@Test
	void shouldUpdateCustomerSuccessfully() {
		when(customerRepository.findByCustomerNumber("CUST-000001")).thenReturn(Optional.of(customer));
		when(customerRepository.existsByEmailAndCustomerNumberNot("updated@example.com", "CUST-000001"))
				.thenReturn(false);
		when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CustomerResponse response = customerService.updateCustomer("CUST-000001", customerUpdateRequest);

		verify(customerRepository).save(customerCaptor.capture());
		Customer updatedCustomer = customerCaptor.getValue();
		assertThat(updatedCustomer.getFirstName()).isEqualTo("Ravindra");
		assertThat(updatedCustomer.getLastName()).isEqualTo("Bhattacharya");
		assertThat(updatedCustomer.getEmail()).isEqualTo("updated@example.com");
		assertThat(updatedCustomer.getPhone()).isEqualTo("9999999999");
		assertThat(response.getFirstName()).isEqualTo("Ravindra");
		assertThat(response.getLastName()).isEqualTo("Bhattacharya");
		assertThat(response.getEmail()).isEqualTo("updated@example.com");
	}

	@Test
	void shouldThrowExceptionWhenUpdatingMissingCustomer() {
		when(customerRepository.findByCustomerNumber("CUST-999999")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> customerService.updateCustomer("CUST-999999", customerUpdateRequest))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Customer not found: CUST-999999");

		verify(customerRepository, never()).existsByEmailAndCustomerNumberNot(any(), any());
		verify(customerRepository, never()).save(any(Customer.class));
	}

	@Test
	void shouldThrowExceptionWhenUpdatingCustomerWithDuplicateEmail() {
		when(customerRepository.findByCustomerNumber("CUST-000001")).thenReturn(Optional.of(customer));
		when(customerRepository.existsByEmailAndCustomerNumberNot("updated@example.com", "CUST-000001"))
				.thenReturn(true);

		assertThatThrownBy(() -> customerService.updateCustomer("CUST-000001", customerUpdateRequest))
				.isInstanceOf(DuplicateResourceException.class)
				.hasMessage("Another customer already exists with email: updated@example.com");

		verify(customerRepository, never()).save(any(Customer.class));
	}

	@Test
	void shouldUpdateCustomerStatusSuccessfully() {
		CustomerStatusRequest request = new CustomerStatusRequest(CustomerStatus.SUSPENDED);
		when(customerRepository.findByCustomerNumber("CUST-000001")).thenReturn(Optional.of(customer));
		when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CustomerResponse response = customerService.updateCustomerStatus("CUST-000001", request);

		verify(customerRepository).save(customerCaptor.capture());
		assertThat(customerCaptor.getValue().getStatus()).isEqualTo(CustomerStatus.SUSPENDED);
		assertThat(response.getStatus()).isEqualTo(CustomerStatus.SUSPENDED);
	}

	@Test
	void shouldThrowExceptionWhenUpdatingStatusForMissingCustomer() {
		CustomerStatusRequest request = new CustomerStatusRequest(CustomerStatus.INACTIVE);
		when(customerRepository.findByCustomerNumber("CUST-999999")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> customerService.updateCustomerStatus("CUST-999999", request))
				.isInstanceOf(CustomerNotFoundException.class)
				.hasMessage("Customer not found: CUST-999999");

		verify(customerRepository, never()).save(any(Customer.class));
	}

	@Test
	void shouldSoftDeleteCustomerSuccessfully() {
		when(customerRepository.findByCustomerNumber("CUST-000001")).thenReturn(Optional.of(customer));
		when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

		customerService.deleteCustomer("CUST-000001");

		verify(customerRepository).save(customerCaptor.capture());
		assertThat(customerCaptor.getValue().getStatus()).isEqualTo(CustomerStatus.DELETED);
	}

	@Test
	void shouldThrowExceptionWhenDeletingMissingCustomer() {
		when(customerRepository.findByCustomerNumber("CUST-999999")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> customerService.deleteCustomer("CUST-999999"))
				.isInstanceOf(CustomerNotFoundException.class)
				.hasMessage("Customer not found: CUST-999999");

		verify(customerRepository, never()).save(any(Customer.class));
	}

	private Customer buildCustomer(String customerNumber, String firstName, String lastName, String email,
			CustomerStatus status) {
		Customer customer = new Customer();
		customer.setId(1L);
		customer.setCustomerNumber(customerNumber);
		customer.setFirstName(firstName);
		customer.setLastName(lastName);
		customer.setEmail(email);
		customer.setPhone("9876543210");
		customer.setStatus(status);
		customer.setCreatedAt(createdAt);
		customer.setUpdatedAt(updatedAt);
		customer.setVersion(0L);
		return customer;
	}
}
