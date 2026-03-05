package com.ubik.paymentservice.application.service;

import com.ubik.paymentservice.domain.model.Payment;
import com.ubik.paymentservice.domain.model.PaymentStatus;
import com.ubik.paymentservice.domain.port.in.PaymentUseCasePort;
import com.ubik.paymentservice.domain.port.out.PaymentRepositoryPort;
import com.ubik.paymentservice.domain.port.out.ReservationConfirmationPort;
import com.ubik.paymentservice.domain.port.out.StripePort;
import com.ubik.paymentservice.infrastructure.adapter.in.web.dto.CreatePaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de aplicación que orquesta la lógica de pagos con Stripe.
 */
@Service
public class PaymentService implements PaymentUseCasePort {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final StripePort stripePort;
    private final PaymentRepositoryPort paymentRepository;
    private final ReservationConfirmationPort reservationConfirmationPort;

    public PaymentService(
            StripePort stripePort,
            PaymentRepositoryPort paymentRepository,
            ReservationConfirmationPort reservationConfirmationPort
    ) {
        this.stripePort = stripePort;
        this.paymentRepository = paymentRepository;
        this.reservationConfirmationPort = reservationConfirmationPort;
    }

    @Override
    public Mono<CreatePaymentResponse> createPayment(Long reservationId, Long userId, Long amountCents) {
        log.info("Creando PaymentIntent en Stripe para reserva {} - monto: {} cop", reservationId, amountCents);

        String description = "Reserva #" + reservationId + " - UBIK";

        return stripePort.createPaymentIntent(amountCents, "cop", description)
                .flatMap(clientSecret -> {
                    String paymentIntentId = extractIntentId(clientSecret);

                    Payment payment = Payment.createPending(
                            reservationId,
                            userId,
                            paymentIntentId,
                            amountCents,
                            "cop"
                    );

                    return paymentRepository.save(payment)
                            .map(saved -> {
                                log.info("Pago {} creado en BD para reserva {}", saved.id(), reservationId);
                                return new CreatePaymentResponse(saved.id(), clientSecret);
                            });
                })
                .doOnError(e -> log.error("Error creando PaymentIntent para reserva {}: {}", reservationId, e.getMessage()));
    }

    @Override
    public Mono<Void> handleWebhook(String payload, String stripeSignatureHeader) {
        log.info("Recibiendo evento webhook de Stripe");

        return stripePort.validateWebhookSignature(payload, stripeSignatureHeader)
                .then(stripePort.parseEventType(payload))
                .flatMap(eventType -> {
                    log.info("Tipo de evento Stripe: {}", eventType);

                    return switch (eventType) {
                        case "payment_intent.succeeded" -> handlePaymentSucceeded(payload);
                        case "payment_intent.payment_failed" -> handlePaymentFailed(payload);
                        default -> {
                            log.debug("Evento no manejado: {}", eventType);
                            yield Mono.empty();
                        }
                    };
                });
    }

    @Override
    public Mono<String> getPublishableKey() {
        return stripePort.getPublishableKey();
    }

    @Override
    public Flux<Payment> findByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    @Override
    public Flux<Payment> findByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId);
    }

    // ─── Handlers de eventos Stripe ───────────────────────────────────────────

    private Mono<Void> handlePaymentSucceeded(String payload) {
        return stripePort.parsePaymentIntentId(payload)
                .flatMap(intentId -> {
                    log.info("Pago exitoso para PaymentIntent: {}", intentId);
                    return paymentRepository.updateStatus(intentId, PaymentStatus.SUCCEEDED, null)
                            .flatMap(payment -> {
                                log.info("Pago {} actualizado a SUCCEEDED. Confirmando reserva {}",
                                        payment.id(), payment.reservationId());
                                return reservationConfirmationPort.confirmReservation(payment.reservationId())
                                        .onErrorResume(e -> {
                                            // No fallar el webhook si la confirmación falla —
                                            // el pago ya está registrado como exitoso
                                            log.error("Error confirmando reserva {}: {}",
                                                    payment.reservationId(), e.getMessage());
                                            return Mono.empty();
                                        });
                            });
                });
    }

    private Mono<Void> handlePaymentFailed(String payload) {
        return Mono.zip(
                stripePort.parsePaymentIntentId(payload),
                stripePort.parseFailureMessage(payload).defaultIfEmpty("Error desconocido")
        ).flatMap(tuple -> {
            String intentId = tuple.getT1();
            String failureMessage = tuple.getT2();
            log.warn("Pago fallido para PaymentIntent {}: {}", intentId, failureMessage);
            return paymentRepository.updateStatus(intentId, PaymentStatus.FAILED, failureMessage).then();
        });
    }

    /**
     * Extrae el ID del PaymentIntent del clientSecret.
     * Formato del clientSecret: "pi_xxxxx_secret_yyyyy" → devuelve "pi_xxxxx"
     */
    private String extractIntentId(String clientSecret) {
        return clientSecret.split("_secret_")[0];
    }
}
