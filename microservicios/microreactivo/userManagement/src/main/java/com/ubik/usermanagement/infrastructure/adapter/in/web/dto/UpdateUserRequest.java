package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para actualización de perfil de usuario
 */
public record UpdateUserRequest(
        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phoneNumber,

        Boolean anonymous,

        @Email(message = "Email must be valid")
        String email,

        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        BigDecimal longitude,

        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        BigDecimal latitude,

        @Past(message = "Birth date must be in the past")
        LocalDate birthDate
) {
}