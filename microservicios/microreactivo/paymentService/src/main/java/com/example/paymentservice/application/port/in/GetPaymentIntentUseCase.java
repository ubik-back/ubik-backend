package com.example.paymentservice.application.port.in;


import com.example.paymentservice.application.result.PaymentIntentView;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetPaymentIntentUseCase {
    Mono<PaymentIntentView> execute(UUID id);
}
