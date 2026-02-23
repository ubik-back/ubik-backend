package com.ubik.motelmanagement.infrastructure.adapter.out.persistence;

import com.ubik.motelmanagement.domain.model.Room;
import com.ubik.motelmanagement.domain.port.out.RoomRepositoryPort;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.RoomImageEntity;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.RoomServiceEntity;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.mapper.RoomMapper;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.MotelR2dbcRepository;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.RoomImageR2dbcRepository;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.RoomR2dbcRepository;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.RoomServiceR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adaptador de persistencia para Room con soporte de imágenes, coordenadas,
 * info del motel desnormalizada y servicios asociados.
 */
@Component
public class RoomPersistenceAdapter implements RoomRepositoryPort {

    private final RoomR2dbcRepository roomR2dbcRepository;
    private final RoomImageR2dbcRepository roomImageRepository;
    private final RoomServiceR2dbcRepository roomServiceRepository;
    private final MotelR2dbcRepository motelR2dbcRepository;
    private final RoomMapper roomMapper;

    public RoomPersistenceAdapter(
            RoomR2dbcRepository roomR2dbcRepository,
            RoomImageR2dbcRepository roomImageRepository,
            RoomServiceR2dbcRepository roomServiceRepository,
            MotelR2dbcRepository motelR2dbcRepository,
            RoomMapper roomMapper) {
        this.roomR2dbcRepository = roomR2dbcRepository;
        this.roomImageRepository = roomImageRepository;
        this.roomServiceRepository = roomServiceRepository;
        this.motelR2dbcRepository = motelR2dbcRepository;
        this.roomMapper = roomMapper;
    }

    @Override
    public Mono<Room> save(Room room) {
        return Mono.just(room)
                .map(roomMapper::toEntity)
                .flatMap(roomR2dbcRepository::save)
                .flatMap(savedEntity ->
                        saveImages(savedEntity.id(), room.imageUrls())
                                .then(syncServices(savedEntity.id(), room.serviceIds()))
                                .then(Mono.just(savedEntity))
                )
                .flatMap(this::loadRoomFull);
    }

    @Override
    public Mono<Room> findById(Long id) {
        return roomR2dbcRepository.findById(id)
                .flatMap(this::loadRoomFull);
    }

    @Override
    public Flux<Room> findAll() {
        return roomR2dbcRepository.findAll()
                .flatMap(this::loadRoomFull);
    }

    @Override
    public Flux<Room> findByMotelId(Long motelId) {
        return roomR2dbcRepository.findByMotelId(motelId)
                .flatMap(this::loadRoomFull);
    }

    @Override
    public Flux<Room> findAvailableByMotelId(Long motelId) {
        return roomR2dbcRepository.findByMotelIdAndIsAvailable(motelId, true)
                .flatMap(this::loadRoomFull);
    }

    @Override
    public Mono<Room> update(Room room) {
        return Mono.just(room)
                .map(roomMapper::toEntity)
                .flatMap(roomR2dbcRepository::save)
                .flatMap(savedEntity ->
                        roomImageRepository.deleteByRoomId(savedEntity.id().intValue())
                                .then(saveImages(savedEntity.id(), room.imageUrls()))
                                .then(syncServices(savedEntity.id(), room.serviceIds()))
                                .then(Mono.just(savedEntity))
                )
                .flatMap(this::loadRoomFull);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return roomImageRepository.deleteByRoomId(id.intValue())
                .then(roomServiceRepository.deleteByRoomId(id))
                .then(roomR2dbcRepository.deleteById(id));
    }

    @Override
    public Mono<Boolean> existsById(Long id) {
        return roomR2dbcRepository.existsById(id);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Carga una habitación con imágenes, serviceIds e info del motel.
     */
    private Mono<Room> loadRoomFull(
            com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.RoomEntity entity) {

        Mono<java.util.List<String>> imagesMono =
                roomImageRepository.findByRoomIdOrderByOrderIndexAsc(entity.id().intValue())
                        .map(RoomImageEntity::imageUrl)
                        .collectList();

        Mono<java.util.List<Long>> servicesMono =
                roomServiceRepository.findByRoomId(entity.id())
                        .map(RoomServiceEntity::serviceId)
                        .collectList();

        Mono<com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.MotelEntity> motelMono =
                motelR2dbcRepository.findById(entity.motelId());

        return Mono.zip(imagesMono, servicesMono, motelMono)
                .map(tuple -> {
                    Room room = roomMapper.toDomain(entity, tuple.getT1(), tuple.getT2());
                    var motel = tuple.getT3();
                    return room.withMotelInfo(
                            motel.name(),
                            motel.address(),
                            motel.city(),
                            motel.phoneNumber()
                    );
                })
                // Si el motel no existe (poco probable) devolvemos la room sin info del motel
                .switchIfEmpty(
                        Mono.zip(imagesMono, servicesMono)
                                .map(tuple -> roomMapper.toDomain(entity, tuple.getT1(), tuple.getT2()))
                );
    }

    /** Guarda las imágenes de una habitación */
    private Mono<Void> saveImages(Long roomId, java.util.List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return Mono.empty();
        AtomicInteger order = new AtomicInteger(1);
        return Flux.fromIterable(imageUrls)
                .map(url -> new RoomImageEntity(null, roomId.intValue(), url, order.getAndIncrement()))
                .flatMap(roomImageRepository::save)
                .then();
    }

    /**
     * Sincroniza los servicios de una habitación:
     * elimina los existentes y agrega los nuevos.
     */
    private Mono<Void> syncServices(Long roomId, java.util.List<Long> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return roomServiceRepository.deleteByRoomId(roomId);
        }
        return roomServiceRepository.deleteByRoomId(roomId)
                .thenMany(Flux.fromIterable(serviceIds)
                        .map(serviceId -> new RoomServiceEntity(roomId, serviceId))
                        .flatMap(roomServiceRepository::save))
                .then();
    }
}