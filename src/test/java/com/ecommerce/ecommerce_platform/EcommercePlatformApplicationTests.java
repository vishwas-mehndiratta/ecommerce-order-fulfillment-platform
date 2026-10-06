package com.ecommerce.ecommerce_platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.ecommerce.ecommerce_platform.customer.repository.CustomerRepository;
import com.ecommerce.ecommerce_platform.customer.service.impl.CustomerNumberGenerator;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
				+ "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
				+ "org.springframework.boot.data.jpa.autoconfigure.JpaRepositoriesAutoConfiguration,"
				+ "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration" })
class EcommercePlatformApplicationTests {

	@MockitoBean
	private CustomerRepository customerRepository;

	@MockitoBean
	private CustomerNumberGenerator customerNumberGenerator;

	@Test
	void contextLoads() {
	}

}
