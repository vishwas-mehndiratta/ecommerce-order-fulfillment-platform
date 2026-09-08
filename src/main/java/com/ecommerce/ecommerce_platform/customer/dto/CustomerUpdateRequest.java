package com.ecommerce.ecommerce_platform.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerUpdateRequest {

	@NotBlank(message = "First name is required")
	@Size(max = 100, message = "First name must not exceed 100 characters")
	private String firstName;

	@NotBlank(message = "Last name is required")
	@Size(max = 100, message = "Last name must not exceed 100 characters")
	private String lastName;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	@Size(max = 255, message = "Email must not exceed 255 characters")
	private String email;

	@Size(max = 20, message = "Phone must not exceed 20 characters")
	private String phone;
	
}

