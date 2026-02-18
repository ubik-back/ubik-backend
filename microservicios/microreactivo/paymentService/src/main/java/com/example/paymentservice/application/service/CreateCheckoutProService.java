package com.example.paymentservice.application.service;

import com.example.paymentservice.application.command.CreateCheckoutProCommand;
import com.example.paymentservice.application.port.in.CreateCheckoutProPreferenceUseCase;
import com.example.paymentservice.application.port.out.IdempotencyStorePort;
import com.example.paymentservice.application.port.out.IdempotencyStorePort.IdemRecord;
import com.example.paymentservice.application.port.out.MercadoPagoPort;
import com.example.paymentservice.application.port.out.MercadoPagoPort.CreatePreferenceRequest;
import com.example.paymentservice.application.port.out.PaymentIntentRepositoryPort;
import com.example.paymentservice.application.result.CreateCheckoutProResult;
import com.example.paymentservice.config.MercadoPagoProperties;
import com.example.paymentservice.domain.model.Money;
import com.example.paymentservice.domain.model.PaymentIntent;
import com.example.paymentservice.domain.model.PaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class CreateCheckoutProService implements CreateCheckoutProPreferenceUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateCheckoutProService.class);
    private static final String OPERATION = "CREATE_CHECKOUT_PRO";

    private final PaymentIntentRepositoryPort intentRepo;
    private final IdempotencyStorePort idemStore;
    private final MercadoPagoPort mpPort;
    private final MercadoPagoProperties mpProps;

    public CreateCheckoutProService(PaymentIntentRepositoryPort intentRepo,
                                    IdempotencyStorePort idemStore,
                                    MercadoPagoPort mpPort,
                                    MercadoPagoProperties mpProps) {
        this.intentRepo = intentRepo;
        this.idemStore = idemStore;
        this.mpPort = mpPort;
        this.mpProps = mpProps;
    }

    @Override
    public Mono<CreateCheckoutProResult> execute(CreateCheckoutProCommand cmd) {
        return idemStore.findValid(cmd.idempotencyKey(), OPERATION, Instant.now())
                .flatMap(idem -> {
                    log.info("Idempotency hit for key={}, returning previous result", cmd.idempotencyKey());
                    return intentRepo.findById(idem.resourceId())
                            .map(intent -> new CreateCheckoutProResult(
                                    intent.getId(),
                                    intent.getExternalReference(),
                                    intent.getRedirectUrl(),
                                    intent.getStatus().name()
                            ));
                })
                .switchIfEmpty(createNewPreference(cmd));
    }

    private Mono<CreateCheckoutProResult> createNewPreference(CreateCheckoutProCommand cmd) {
        UUID intentId = UUID.randomUUID();
        PaymentIntent intent = new PaymentIntent(
                intentId,
                cmd.externalReference(),
                Money.of(cmd.amount(), cmd.currency()),
                PaymentProvider.MERCADO_PAGO
        );

        CreatePreferenceRequest req = new CreatePreferenceRequest(
                cmd.externalReference(),
                cmd.title(),
                cmd.amount(),
                cmd.currency(),
                mpProps.notificationUrl(),
                mpProps.backUrls().success(),
                mpProps.backUrls().pending(),
                mpProps.backUrls().failure()
        );

        return intentRepo.save(intent)
                .then(saveIdempotencyRecord(cmd.idempotencyKey(), intentId))
                .then(mpPort.createPreference(req, cmd.idempotencyKey()))
                .flatMap(pref -> {
                    intent.applyPreference(pref.preferenceId(), pref.initPoint());
                    return intentRepo.update(intent)
                            .thenReturn(new CreateCheckoutProResult(
                                    intentId,
                                    cmd.externalReference(),
                                    pref.initPoint(),
                                    intent.getStatus().name()
                            ));
                });
    }

    private Mono<Void> saveIdempotencyRecord(String key, UUID resourceId) {
        IdemRecord rec = new IdemRecord(
                UUID.randomUUID(),
                key,
                OPERATION,
                resourceId,
                key,
                Instant.now(),
                Instant.now().plus(24, ChronoUnit.HOURS)
        );
        return idemStore.save(rec);
    }
}
