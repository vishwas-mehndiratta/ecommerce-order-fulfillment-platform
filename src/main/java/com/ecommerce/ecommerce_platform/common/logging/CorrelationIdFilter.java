package com.ecommerce.ecommerce_platform.common.logging;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

	private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
	private static final String MDC_CORRELATION_ID = "correlationId";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// Get correlation ID sent by the client
		String correlationId = request.getHeader(CORRELATION_ID_HEADER);

		// Generate a new ID if client did not provide one
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = UUID.randomUUID().toString();
		}

		try {
			// Store correlation ID in MDC.
			// All logs generated during this request can access this value.
			MDC.put(MDC_CORRELATION_ID, correlationId);

			// Return the same correlation ID to the client
			response.setHeader(CORRELATION_ID_HEADER, correlationId);

			// Continue request processing
			filterChain.doFilter(request, response);

		} finally {
			// Important: remove MDC value after request completes
			// because application servers reuse threads.
			MDC.remove(MDC_CORRELATION_ID);
		}
	}
}