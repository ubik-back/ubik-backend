package com.example.paymentservice.application.service;

import com.acme.payments.adapters.in.web.security.MercadoPagoWebhookSignatureVerifier;
import com.acme.payments.application.command.HandleWebhookCommand;
import com.acme.payments.application.port.in.HandleMercadoPagoWebhookUseCase;
import com.acme.payments.application.port.out.MercadoPagoPort;
import com.acme.payments.application.port.out.PaymentIntentRepositoryPort;
import com.acme.payments.application.port.out.WebhookEventStorePort;
import com.acme.payments.application.port.out.WebhookEventStorePort.WebhookEventRecord;
import com.acme.payments.domain.model.PaymentIntent;
import com.acme.payments.domain.service.MercadoPagoStatusTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class HandleMercadoPagoWebhookService implements HandleMercadoPagoWebhookUseCase {

    private static final Logger log = LoggerFactory.getLogger(HandleMercadoPagoWebhookService.class);

    private final WebhookEventStorePort webhookStore;
    private final MercadoPagoPort mpPort;
    private final PaymentIntentRepositoryPort intentRepo;
    private final MercadoPagoStatusTranslator statusTranslator;
    private final MercadoPagoWebhookSignatureVerifier signatureVerifier;

    public HandleMercadoPagoWebhookService(WebhookEventStorePort webhookStore,
                                           MercadoPagoPort mpPort,
                                           PaymentIntentRepositoryPort intentRepo,
                                           MercadoPagoStatusTranslator statusTranslator,
                                           MercadoPagoWebhookSignatureVerifier signatureVerifier) {
        this.webhookStore = webhookStore;
        this.mpPort = mpPort;
        this.intentRepo = intentRepo;
        this.statusTranslator = statusTranslator;
        this.signatureVerifier = signatureVerifier;
    }

    @Override
    public Mono<Void> execute(HandleWebhookCommand cmd) {
        boolean signatureValid = false;
        try {
            signatureValid = signatureVerifier.verify(cmd.xSignature(), cmd.xRequestId(), cmd.dataId());
        } catch (Exception e) {
            log.warn("Webhook signature verification failed: {}", e.getMessage());
        }

        if (!signatureValid) {
            log.warn("Webhook received with invalid signature for xRequestId={}", cmd.xRequestId());
        }

        String dedupKey = cmd.xRequestId() + ":" + cmd.dataId();
        final boolean finalSignatureValid = signatureValid;

        return webhookStore.findByDedupKey(dedupKey)
                .flatMap(existing -> {
                    log.info("Duplicate webhook event: dedupKey={}", dedupKey);
                    return Mono.<Void>empty();
                })
                .switchIfEmpty(
                        saveAndProcess(cmd, dedupKey, finalSignatureValid)
                );
    }

    private Mono<Void> saveAndProcess(HandleWebhookCommand cmd, String dedupKey, boolean signatureValid) {
        WebhookEventRecord event = new WebhookEventRecord(
                UUID.randomUUID(),
                dedupKey,
                cmd.type(),
                null,
                cmd.dataId(),
                cmd.xRequestId(),
                signatureValid,
                Instant.now(),
                null,
                cmd.rawBody(),
                null
        );

        return webhookStore.save(event)
                .then(processPaymentEvent(cmd, dedupKey))
                .onErrorResume(ex -> {
                    log.error("Error processing webhook dedupKey={}: {}", dedupKey, ex.getMessage(), ex);
                    return Mono.empty();
                });
    }

    private Mono<Void> processPaymentEvent(HandleWebhookCommand cmd, String dedupKey) {
        if (!"payment".equalsIgnoreCase(cmd.type())) {
            log.debug("Ignoring non-payment webhook type={}", cmd.type());
            return webhookStore.markProcessed(dedupKey, Instant.now());
        }

        Long paymentId;
        try {
            paymentId = Long.parseLong(cmd.dataId());
        } catch (NumberFormatException e) {
            log.warn("Invalid payment id in webhook: {}", cmd.dataId());
            return webhookStore.markProcessed(dedupKey, Instant.now());
        }

        return mpPort.getPayment(paymentId)
                .flatMap(snapshot -> {
                    String extRef = String.valueOf(paymentId); // fallback
                    return intentRepo.findByExternalReference(snapshot.currency() != null
                                    ? String.valueOf(paymentId) : String.valueOf(paymentId))
                            // Try to find by externalReference from metadata, use paymentId as fallback
                            .switchIfEmpty(Mono.empty())
                            .flatMap(intent -> updateIntent(intent, snapshot, paymentId))
                            .then(webhookStore.markProcessed(dedupKey, Instant.now()));
                })
                .onErrorResume(ex -> {
                    log.error("Failed to fetch payment {} from MP: {}", paymentId, ex.getMessage());
                    return webhookStore.markProcessed(dedupKey, Instant.now());
                });
    }

    private Mono<Void> updateIntent(PaymentIntent intent, MercadoPagoPort.PaymentSnapshot snapshot, Long paymentId) {
        var newStatus = statusTranslator.translate(snapshot.status(), snapshot.statusDetail());
        intent.applyPaymentUpdate(paymentId, newStatus, snapshot.status());
        return intentRepo.update(intent);
    }
}