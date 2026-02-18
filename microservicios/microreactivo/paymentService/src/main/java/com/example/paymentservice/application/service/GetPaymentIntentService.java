package com.example.paymentservice.application.service;

import com.acme.payments.application.port.in.GetPaymentIntentUseCase;
import com.acme.payments.application.port.out.PaymentIntentRepositoryPort;
import com.acme.payments.application.result.PaymentIntentView;
import com.acme.payments.domain.exception.NotFoundException;
import com.acme.payments.domain.model.PaymentIntent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GetPaymentIntentService implements GetPaymentIntentUseCase {

    private final PaymentIntentRepositoryPort intentRepo;

    public GetPaymentIntentService(PaymentIntentRepositoryPort intentRepo) {
        this.intentRepo = intentRepo;
    }

    @Override
    public Mono<PaymentIntentView> execute(UUID id) {
        return intentRepo.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("PaymentIntent not found: " + id)))
                .map(this::toView);
    }

    private PaymentIntentView toView(PaymentIntent intent) {
        return new PaymentIntentView(
                intent.getId(),
                intent.getExternalReference(),
                intent.getMoney().amount(),
                intent.getMoney().currency(),
                intent.getStatus(),
                intent.getProvider().name(),
                intent.getMpPreferenceId(),
                intent.getMpPaymentId(),
                intent.getRedirectUrl(),
                intent.getLastProviderStatus(),
                intent.getCreatedAt(),
                intent.getUpdatedAt()
        );
    }
}
