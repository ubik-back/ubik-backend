package com.ubik.motelmanagement.domain.model;

import java.util.List;

/**
 * Modelo de dominio para Room (Habitación)
 */
public record Room(
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
    /** Constructor para creación de nuevas habitaciones (sin ID) */
    public static Room createNew(
            Long motelId,
            String number,
            String roomType,
            Double price,
            String description,
            List<String> imageUrls,
            Double latitude,
            Double longitude
    ) {
        return new Room(
                null, motelId, number, roomType, price, description,
                true, imageUrls,
                latitude, longitude,
                null, null, null, null,
                null
        );
    }

    /** Constructor para actualización */
    public Room withUpdatedInfo(
            String number,
            String roomType,
            Double price,
            String description,
            Boolean isAvailable,
            List<String> imageUrls,
            Double latitude,
            Double longitude
    ) {
        return new Room(
                this.id, this.motelId, number, roomType, price, description,
                isAvailable, imageUrls,
                latitude, longitude,
                this.motelName, this.motelAddress, this.motelCity, this.motelPhoneNumber,
                this.serviceIds
        );
    }

    /** Constructor para añadir info del motel */
    public Room withMotelInfo(String motelName, String motelAddress, String motelCity, String motelPhoneNumber) {
        return new Room(
                this.id, this.motelId, this.number, this.roomType, this.price,
                this.description, this.isAvailable, this.imageUrls,
                this.latitude, this.longitude,
                motelName, motelAddress, motelCity, motelPhoneNumber,
                this.serviceIds
        );
    }

    /** Constructor para añadir serviceIds */
    public Room withServiceIds(List<Long> serviceIds) {
        return new Room(
                this.id, this.motelId, this.number, this.roomType, this.price,
                this.description, this.isAvailable, this.imageUrls,
                this.latitude, this.longitude,
                this.motelName, this.motelAddress, this.motelCity, this.motelPhoneNumber,
                serviceIds
        );
    }
}