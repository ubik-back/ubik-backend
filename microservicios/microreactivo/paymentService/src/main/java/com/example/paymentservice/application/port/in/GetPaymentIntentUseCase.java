package com.example.paymentservice.application.port.in;


import com.acme.payments.application.result.PaymentIntentView;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetPaymentIntentUseCase {
    Mono<PaymentIntentView> execute(UUID id);
}
