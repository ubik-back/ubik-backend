package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import java.util.List;

/**
 * DTO para actualizar un Motel existente
 */
public record UpdateMotelRequest(
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String name,

        @NotBlank(message = "La dirección es requerida")
        @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
        String address,

        @Size(max = 20, message = "El número de teléfono no puede exceder 20 caracteres")
        String phoneNumber,

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        String description,

        @NotBlank(message = "La ciudad es requerida")
        @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
        String city,

        @Size(max = 10, message = "No se pueden agregar más de 10 imágenes")
        List<@Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres") String> imageUrls,

        @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
        @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
        @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
        Double longitude
) {
}