package com.ubik.motelmanagement.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo de dominio para Motel
 * Representa la entidad de negocio independiente de la infraestructura
 */
public record Motel(
        Long id,
        String name,
        String address,
        String phoneNumber,
        String description,
        String city,
        Long propertyId,
        LocalDateTime dateCreated,
        List<String> imageUrls,
        Double latitude,   
        Double longitude   
) {
    // Constructor para creación de nuevos moteles (sin ID)
    public static Motel createNew(
            String name,
            String address,
            String phoneNumber,
            String description,
            String city,
            Long propertyId,
            List<String> imageUrls,
            Double latitude,
            Double longitude
    ) {
        return new Motel(
                null, 
                name, 
                address, 
                phoneNumber, 
                description, 
                city, 
                propertyId, 
                LocalDateTime.now(), 
                imageUrls,
                latitude,
                longitude
        );
    }

    // Constructor para actualización (mantiene ID y fecha de creación)
    public Motel withUpdatedInfo(
            String name,
            String address,
            String phoneNumber,
            String description,
            String city,
            List<String> imageUrls,
            Double latitude,
            Double longitude
    ) {
        return new Motel(
                this.id, 
                name, 
                address, 
                phoneNumber, 
                description, 
                city, 
                this.propertyId, 
                this.dateCreated, 
                imageUrls,
                latitude,
                longitude
        );
    }
}