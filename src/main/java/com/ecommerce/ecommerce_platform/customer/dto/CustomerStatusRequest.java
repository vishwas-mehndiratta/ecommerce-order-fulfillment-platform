package com.ecommerce.ecommerce_platform.customer.dto;

import com.ecommerce.ecommerce_platform.customer.entity.CustomerStatus;

import jakarta.validation.constraints.NotNull;

public record CustomerStatusRequest(@NotNull CustomerStatus status) {
}
