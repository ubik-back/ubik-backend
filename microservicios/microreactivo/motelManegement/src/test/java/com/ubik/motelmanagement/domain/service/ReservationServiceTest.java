package com.ubik.motelmanagement.domain.service;

import com.ubik.motelmanagement.domain.exception.NotFoundException;
import com.ubik.motelmanagement.domain.exception.RoomNotAvailableException;
import com.ubik.motelmanagement.domain.model.Reservation;
import com.ubik.motelmanagement.domain.model.UserSummary;
import com.ubik.motelmanagement.domain.port.out.NotificationPort;
import com.ubik.motelmanagement.domain.port.out.ReservationRepositoryPort;
import com.ubik.motelmanagement.domain.port.out.RoomRepositoryPort;
import com.ubik.motelmanagement.domain.port.out.UserPort;
import com.ubik.motelmanagement.infrastructure.client.NotificationClient;
import com.ubik.motelmanagement.infrastructure.service.ConfirmationCodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock NotificationPort notificationPort;
    @Mock ReservationRepositoryPort reservationRepositoryPort;
    @Mock RoomRepositoryPort roomRepositoryPort;
    @Mock ConfirmationCodeService confirmationCodeService;
    @Mock NotificationClient notificationClient;
    @Mock UserPort userPort;

    @InjectMocks
    ReservationService reservationService;

    @Test
    void createReservation_whenRoomNotFound_shouldThrowNotFoundException() {
        // ARRANGE
        Reservation request = createValidReservation();
        when(confirmationCodeService.generateCode()).thenReturn(Mono.just("CODE-123"));
        when(roomRepositoryPort.existsById(any())).thenReturn(Mono.just(false));

        // ACT & ASSERT
        StepVerifier.create(reservationService.createReservation(request))
                .expectError(NotFoundException.class)
                .verify();

        verify(reservationRepositoryPort, never()).save(any());
    }

    @Test
    void createReservation_whenRoomNotAvailable_shouldThrowRoomNotAvailableException() {
        // ARRANGE
        Reservation request = createValidReservation();
        when(confirmationCodeService.generateCode()).thenReturn(Mono.just("CODE-123"));
        when(roomRepositoryPort.existsById(any())).thenReturn(Mono.just(true));
        // Simular que hay una reserva solapada activa
        Reservation existing = mock(Reservation.class);
        when(existing.isActive()).thenReturn(true);
        when(reservationRepositoryPort.findOverlappingReservations(any(), any(), any()))
                .thenReturn(Flux.just(existing));

        // ACT & ASSERT
        StepVerifier.create(reservationService.createReservation(request))
                .expectError(RoomNotAvailableException.class)
                .verify();
    }

    @Test
    void createReservation_whenNotificationFails_shouldStillSaveReservation() {
        // ARRANGE
        Reservation request = createValidReservation();
        UserSummary user = new UserSummary(1L, "test@example.com");
        
        when(confirmationCodeService.generateCode()).thenReturn(Mono.just("CODE-123"));
        when(roomRepositoryPort.existsById(any())).thenReturn(Mono.just(true));
        when(reservationRepositoryPort.findOverlappingReservations(any(), any(), any())).thenReturn(Flux.empty());
        when(reservationRepositoryPort.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(userPort.getUserById(any())).thenReturn(Mono.just(user));
        
        // Simular fallo en notificación
        when(notificationPort.sendReservationConfirmation(any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.error(new RuntimeException("Email service down")));

        // ACT & ASSERT
        StepVerifier.create(reservationService.createReservation(request))
                .expectNextCount(1)
                .verifyComplete();

        verify(reservationRepositoryPort, times(1)).save(any());
    }

    @Test
    void cancelReservation_whenCheckedIn_shouldThrowIllegalArgumentException() {
        // ARRANGE
        Reservation reservation = mock(Reservation.class);
        when(reservation.canBeCancelled()).thenReturn(false);
        when(reservation.status()).thenReturn(Reservation.ReservationStatus.CHECKED_IN);
        when(reservationRepositoryPort.findById(anyLong())).thenReturn(Mono.just(reservation));

        // ACT & ASSERT
        StepVerifier.create(reservationService.cancelReservation(1L))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    private Reservation createValidReservation() {
        return new Reservation(
                null,
                1L,
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                Reservation.ReservationStatus.PENDING,
                100.0,
                "Requests",
                null,
                null,
                null
        );
    }
}
