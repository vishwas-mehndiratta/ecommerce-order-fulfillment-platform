package com.ecommerce.ecommerce_platform.customer.service.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomerNumberGenerator {

	private static final String CUSTOMER_NUMBER_SEQUENCE = "SELECT nextval('customer_number_seq')";

	private final JdbcTemplate jdbcTemplate;

	public CustomerNumberGenerator(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public String generate() {

		Long sequenceValue = jdbcTemplate.queryForObject(CUSTOMER_NUMBER_SEQUENCE, Long.class);

		return String.format("CUST-%06d", sequenceValue);
	}

}
