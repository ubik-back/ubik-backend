package com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper;

import com.ubik.motelmanagement.domain.model.Room;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateRoomRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.RoomResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateRoomRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RoomDtoMapper {

    public Room toDomain(CreateRoomRequest request) {
        if (request == null) return null;
        return new Room(
                null,
                request.motelId(),
                request.number(),
                request.roomType(),
                request.price(),
                request.description(),
                true,
                request.imageUrls() != null ? new ArrayList<>(request.imageUrls()) : new ArrayList<>(),
                request.latitude(),
                request.longitude(),
                null, null, null, null,
                request.serviceIds() != null ? new ArrayList<>(request.serviceIds()) : new ArrayList<>()
        );
    }

    public Room toDomain(UpdateRoomRequest request) {
        if (request == null) return null;
        return new Room(
                null, null,
                request.number(),
                request.roomType(),
                request.price(),
                request.description(),
                request.isAvailable(),
                request.imageUrls() != null ? new ArrayList<>(request.imageUrls()) : new ArrayList<>(),
                request.latitude(),
                request.longitude(),
                null, null, null, null,
                request.serviceIds() != null ? new ArrayList<>(request.serviceIds()) : new ArrayList<>()
        );
    }

    public RoomResponse toResponse(Room room) {
        if (room == null) return null;
        return new RoomResponse(
                room.id(),
                room.motelId(),
                room.number(),
                room.roomType(),
                room.price(),
                room.description(),
                room.isAvailable(),
                room.imageUrls(),
                room.latitude(),
                room.longitude(),
                room.motelName(),
                room.motelAddress(),
                room.motelCity(),
                room.motelPhoneNumber(),
                room.serviceIds()
        );
    }
}