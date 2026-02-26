package com.example.paymentservice.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.preference.Preference;
import com.example.paymentservice.client.MotelManagementClient;
import com.example.paymentservice.client.NotificationClient;
import com.example.paymentservice.domain.Payment;
import com.example.paymentservice.domain.PaymentStatus;
import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.entity.PaymentEntity;
import com.example.paymentservice.repository.PaymentRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final MotelManagementClient motelManagementClient;
    private final NotificationClient notificationClient;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    public PaymentService(
            PaymentRepository paymentRepository,
            MotelManagementClient motelManagementClient,
            NotificationClient notificationClient) {
        this.paymentRepository = paymentRepository;
        this.motelManagementClient = motelManagementClient;
        this.notificationClient = notificationClient;
    }

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    // ─── Crear preferencia de pago ────────────────────────────────────────────

    @Value("${mercadopago.marketplace-fee-percent:10}")
    private double marketplaceFeePercent;

    public Mono<PaymentResponse> createPayment(
            CreatePaymentRequest request,
            Long userId,
            Long motelId) {

        log.info("Creando pago marketplace - reserva:{} motel:{} usuario:{}",
                request.reservationId(), motelId, userId);

        // 1. Obtener access_token vigente del motel
        return oAuthService.getValidAccessToken(motelId)
                .flatMap(motelAccessToken -> {

                    // 2. Calcular comisión
                    double fee = request.amount() * (marketplaceFeePercent / 100.0);
                    String currency = request.currency() != null ? request.currency() : "COP";

                    // 3. Crear preferencia usando el token DEL MOTEL
                    return Mono.fromCallable(() -> {
                                // Configurar SDK con el token del MOTEL
                                MercadoPagoConfig.setAccessToken(motelAccessToken);

                                PreferenceClient client = new PreferenceClient();

                                PreferenceItemRequest item = PreferenceItemRequest.builder()
                                        .title("Reserva UBIK #" + request.reservationId())
                                        .quantity(1)
                                        .unitPrice(BigDecimal.valueOf(request.amount()))
                                        .currencyId(currency)
                                        .build();

                                PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                                        .success("https://ubik.app/payment/success")
                                        .failure("https://ubik.app/payment/failure")
                                        .pending("https://ubik.app/payment/pending")
                                        .build();

                                PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                                        .items(List.of(item))
                                        .backUrls(backUrls)
                                        .autoReturn("approved")
                                        .externalReference(request.reservationId().toString())
                                        .marketplaceFee(BigDecimal.valueOf(fee)) // Tu comisión
                                        .build();

                                return client.create(preferenceRequest);
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(preference -> {
                                PaymentEntity entity = new PaymentEntity(
                                        null,
                                        request.reservationId(),
                                        userId,
                                        request.amount(),
                                        currency,
                                        PaymentStatus.PENDING.name(),
                                        null,
                                        preference.getId(),
                                        preference.getInitPoint(),
                                        null,
                                        fee,        // marketplace_fee
                                        motelId,    // motel_id
                                        null,       // mp_collector_id (llega por webhook)
                                        LocalDateTime.now(),
                                        LocalDateTime.now()
                                );
                                return paymentRepository.save(entity);
                            });
                })
                .map(this::toResponse)
                .doOnError(e -> log.error("Error creando pago marketplace: {}", e.getMessage()));
    }

    // ─── Consultar estado ────────────────────────────────────────────────────

    public Mono<PaymentResponse> getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Pago no encontrado: " + id)))
                .map(this::toResponse);
    }

    public Flux<PaymentResponse> getPaymentsByReservation(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .map(this::toResponse);
    }

    public Flux<PaymentResponse> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId)
                .map(this::toResponse);
    }

    // ─── Procesar webhook de MercadoPago ─────────────────────────────────────

    public Mono<Void> processWebhook(String mpPaymentId) {
        log.info("Procesando webhook para mpPaymentId: {}", mpPaymentId);

        return Mono.fromCallable(() -> {
                    PaymentClient client = new PaymentClient();
                    return client.get(Long.parseLong(mpPaymentId));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(mpPayment -> {
                    String mpStatus = mpPayment.getStatus(); // "approved", "rejected", "pending"
                    Long reservationId = Long.parseLong(mpPayment.getExternalReference());

                    return paymentRepository.findByMercadoPagoPreferenceId(mpPayment.getOrder().getId().toString())
                            .switchIfEmpty(paymentRepository.findByReservationId(reservationId).next())
                            .flatMap(entity -> {
                                PaymentStatus newStatus = mapMpStatus(mpStatus);

                                PaymentEntity updated = new PaymentEntity(
                                        entity.id(),
                                        entity.reservationId(),
                                        entity.userId(),
                                        entity.amount(),
                                        entity.currency(),
                                        newStatus.name(),
                                        mpPaymentId,
                                        entity.mercadoPagoPreferenceId(),
                                        entity.initPoint(),
                                        mpStatus.equals("rejected") ? mpPayment.getStatusDetail() : null,
                                        entity.createdAt(),
                                        LocalDateTime.now()
                                );

                                return paymentRepository.save(updated)
                                        .flatMap(saved -> handlePostPayment(saved, newStatus));
                            });
                })
                .then()
                .doOnError(e -> log.error("Error procesando webhook {}: {}", mpPaymentId, e.getMessage()));
    }

    // ─── Acciones post-pago ───────────────────────────────────────────────────

    private Mono<Void> handlePostPayment(PaymentEntity payment, PaymentStatus status) {
        if (status == PaymentStatus.APPROVED) {
            return motelManagementClient.confirmReservation(payment.reservationId())
                    .then(notificationClient.sendPaymentApprovedEmail(
                            // El email se obtendría del userManagement; por ahora placeholder
                            "usuario@email.com",
                            payment.reservationId(),
                            payment.amount()
                    ));
        } else if (status == PaymentStatus.REJECTED) {
            return motelManagementClient.cancelReservation(payment.reservationId())
                    .then(notificationClient.sendPaymentRejectedEmail(
                            "usuario@email.com",
                            payment.reservationId()
                    ));
        }
        return Mono.empty();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private PaymentStatus mapMpStatus(String mpStatus) {
        return switch (mpStatus) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "refunded" -> PaymentStatus.REFUNDED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.PENDING;
        };
    }

    private PaymentResponse toResponse(PaymentEntity e) {
        return new PaymentResponse(
                e.id(),
                e.reservationId(),
                e.userId(),
                e.amount(),
                e.currency(),
                e.status(),
                e.initPoint(),
                e.mercadoPagoPaymentId(),
                e.failureReason(),
                e.createdAt(),
                e.updatedAt()
        );
    }
    public Mono<PaymentResponse> refundPayment(Long paymentId) {
        log.info("Procesando reembolso para pago {}", paymentId);

        return paymentRepository.findById(paymentId)
                .switchIfEmpty(Mono.error(new RuntimeException("Pago no encontrado: " + paymentId)))
                .flatMap(entity -> {
                    if (!PaymentStatus.APPROVED.name().equals(entity.status())) {
                        return Mono.error(new IllegalStateException(
                                "Solo se pueden reembolsar pagos aprobados. Estado actual: " + entity.status()));
                    }

                    return Mono.fromCallable(() -> {
                                com.mercadopago.client.payment.PaymentClient client =
                                        new com.mercadopago.client.payment.PaymentClient();
                                return client.refund(Long.parseLong(entity.mercadoPagoPaymentId()));
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(refund -> {
                                PaymentEntity updated = new PaymentEntity(
                                        entity.id(),
                                        entity.reservationId(),
                                        entity.userId(),
                                        entity.amount(),
                                        entity.currency(),
                                        PaymentStatus.REFUNDED.name(),
                                        entity.mercadoPagoPaymentId(),
                                        entity.mercadoPagoPreferenceId(),
                                        entity.initPoint(),
                                        null,
                                        entity.createdAt(),
                                        LocalDateTime.now()
                                );
                                return paymentRepository.save(updated);
                            })
                            .flatMap(saved ->
                                    motelManagementClient.cancelReservation(saved.reservationId())
                                            .thenReturn(saved)
                            )
                            .map(this::toResponse);
                })
                .doOnError(e -> log.error("Error en reembolso {}: {}", paymentId, e.getMessage()));
    }
}