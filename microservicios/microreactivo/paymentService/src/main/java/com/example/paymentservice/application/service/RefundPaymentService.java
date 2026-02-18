package com.example.paymentservice.application.service;

import com.acme.payments.application.command.RefundPaymentCommand;
import com.acme.payments.application.port.in.RefundPaymentUseCase;
import com.acme.payments.application.port.out.IdempotencyStorePort;
import com.acme.payments.application.port.out.IdempotencyStorePort.IdemRecord;
import com.acme.payments.application.port.out.MercadoPagoPort;
import com.acme.payments.application.port.out.PaymentIntentRepositoryPort;
import com.acme.payments.application.result.RefundResult;
import com.acme.payments.domain.exception.NotFoundException;
import com.acme.payments.domain.model.PaymentIntent;
import com.acme.payments.domain.model.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefundPaymentService implements RefundPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefundPaymentService.class);
    private static final String OPERATION = "REFUND_PAYMENT";

    private final PaymentIntentRepositoryPort intentRepo;
    private final MercadoPagoPort mpPort;
    private final IdempotencyStorePort idemStore;

    public RefundPaymentService(PaymentIntentRepositoryPort intentRepo,
                                MercadoPagoPort mpPort,
                                IdempotencyStorePort idemStore) {
        this.intentRepo = intentRepo;
        this.mpPort = mpPort;
        this.idemStore = idemStore;
    }

    @Override
    public Mono<RefundResult> execute(RefundPaymentCommand cmd) {
        return idemStore.findValid(cmd.idempotencyKey(), OPERATION, Instant.now())
                .flatMap(idem -> {
                    log.info("Idempotency hit for refund key={}", cmd.idempotencyKey());
                    return intentRepo.findById(idem.resourceId())
                            .map(intent -> new RefundResult(intent.getId(), null, cmd.amount(), "REFUNDED"));
                })
                .switchIfEmpty(doRefund(cmd));
    }

    private Mono<RefundResult> doRefund(RefundPaymentCommand cmd) {
        return intentRepo.findById(cmd.paymentIntentId())
                .switchIfEmpty(Mono.error(new NotFoundException("PaymentIntent not found: " + cmd.paymentIntentId())))
                .flatMap(intent -> {
                    if (intent.getMpPaymentId() == null) {
                        return Mono.error(new IllegalStateException("No MP payment ID on intent: " + intent.getId()));
                    }
                    return mpPort.refundPayment(intent.getMpPaymentId(), cmd.amount(), cmd.idempotencyKey())
                            .flatMap(refund -> {
                                intent.markRefunded();
                                return intentRepo.update(intent)
                                        .then(saveIdempotencyRecord(cmd.idempotencyKey(), intent.getId()))
                                        .thenReturn(new RefundResult(
                                                intent.getId(),
                                                refund.refundId(),
                                                refund.amount(),
                                                refund.status()
                                        ));
                            });
                });
    }

    private Mono<Void> saveIdempotencyRecord(String key, UUID resourceId) {
        IdemRecord rec = new IdemRecord(UUID.randomUUID(), key, OPERATION, resourceId,
                key, Instant.now(), Instant.now().plus(24, ChronoUnit.HOURS));
        return idemStore.save(rec);
    }
}