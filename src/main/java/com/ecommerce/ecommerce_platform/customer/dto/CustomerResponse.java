package com.ecommerce.ecommerce_platform.customer.dto;

import java.time.LocalDateTime;

import com.ecommerce.ecommerce_platform.customer.entity.CustomerStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

	private Long id;
	private String customerNumber;
	private String firstName;
	private String lastName;
	private String email;
	private String phone;
	private CustomerStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
