package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import java.util.List;

/**
 * DTO de respuesta para Room — incluye info del motel, servicios y coordenadas
 */
public record RoomResponse(
        Long id,
        Long motelId,
        String number,
        String roomType,
        Double price,
        String description,
        Boolean isAvailable,
        List<String> imageUrls,
        Double latitude,
        Double longitude,
        String motelName,
        String motelAddress,
        String motelCity,
        String motelPhoneNumber,
        List<Long> serviceIds
) {
}